/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.core.plumbing

import java.util.concurrent.ExecutionException

import akka.actor.ActorRef
import akka.event.Logging
import akka.pattern.ask
import mathact.core.bricks.blocks.SketchContext
import mathact.core.bricks.plumbing.fitting.{Socket, Plug}
import mathact.core.bricks.plumbing.wiring.obj.{ObjOnStop, ObjOnStart}
import mathact.core.bricks.ui.UIEvent
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting._
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}
import mathact.core.sketch.blocks.BlockLike
import scala.concurrent.Await


/** Process of blocks communications
  * Created by CAB on 09.05.2016.
  */

private[core] class Pump(
  context: SketchContext,
  val block: BlockLike)
extends PumpLike{
  //Fields
  val blockClassName =  block.getClass.getTypeName
  //Logging
  private val akkaLog = Logging.getLogger(context.system, this)
  akkaLog.info(s"[Pump.<init>] DriveCreating of block: $blockClassName")
  private[core] object log {
    def debug(msg: String): Unit = akkaLog.debug(s"[$blockClassName] $msg")
    def info(msg: String): Unit = akkaLog.info(s"[$blockClassName] $msg")
    def warning(msg: String): Unit = akkaLog.warning(s"[$blockClassName] $msg")
    def error(msg: String): Unit = akkaLog.error(s"[$blockClassName] $msg")  }
  //Actors
  private[core] val drive: ActorRef = Await
    .result(ask(context.plumbing, M.NewDrive(this))(context.pumpConfig.askTimeout)
      .mapTo[Either[Throwable,ActorRef]], context.pumpConfig.askTimeout.duration)
    .fold(t ⇒ throw new ExecutionException(t), d ⇒ d)
  //Functions
  private def addPipe(msg: Any): (Int, Int) = Await //Return: (block ID, pipe ID)
    .result(
      ask(drive, msg)(context.pumpConfig.askTimeout).mapTo[Either[Throwable,(Int, Int)]],
      context.pumpConfig.askTimeout.duration)
    .fold(
      t ⇒ {
        akkaLog.error(s"[Pump.addPipe] Error on adding of pipe, msg: $msg, error: $t")
        throw new ExecutionException(t)},
      d ⇒ {
        akkaLog.debug(s"[Pump.addPipe] Pipe added, inletId: $d")
        d})
  //Overridden methods
  override def toString: String = s"Pump(context: $context, block: $blockClassName)"
  //Methods
  private[core] def addOutlet(pipe: OutPipe[_], name: Option[String]): (Int, Int) = addPipe(M.AddOutlet(pipe, name))
  private[core] def addInlet(pipe: InPipe[_], name: Option[String]): (Int, Int) = addPipe(M.AddInlet(pipe, name))
  private[core] def connect(out: ()⇒Plug[_], in: ()⇒Socket[_]): Int = Await //Return: connection ID
    .result(
      ask(drive,  M.ConnectPipes(out, in))(context.pumpConfig.askTimeout).mapTo[Either[Throwable,Int]],
      context.pumpConfig.askTimeout.duration)
    .fold(
      t ⇒ {
        akkaLog.error(s"[Pump.connect] Error on connecting of pipes: $t")
        throw new ExecutionException(t)},
      d ⇒ {
        akkaLog.debug(s"[Pump.connect] Pipes connected: $d")
        d})
  private[core] def blockStart(): Unit = block match{
    case os: ObjOnStart ⇒ os.doStart()
    case _ ⇒ akkaLog.debug(s"[Pump.blockStart] Block $blockClassName not have doStart method.")}
  private[core] def blockStop(): Unit = block match{
    case os: ObjOnStop ⇒  os.doStop()
    case _ ⇒ akkaLog.debug(s"[Pump.blockStop] Block $blockClassName not have doStop method.")}
  private[core] def pushUserMessage(msg: M.UserData[_]): Unit = Await
    .result(
      ask(drive, msg)(context.pumpConfig.askTimeout).mapTo[Either[Throwable, Option[Long]]],  //Either(error,  Option[sleep timeout])
      context.pumpConfig.askTimeout.duration)
    .fold(
      error ⇒ {
        akkaLog.error(s"[Pump.pushUserMessage] Error on ask of drive, msg: $msg, error: $error, block $blockClassName")
        throw new ExecutionException(error)},
      timeout ⇒ {
        akkaLog.debug(s"[Pump.pushUserMessage] Message pushed, msg: $msg, timeout, $timeout, block $blockClassName")
        timeout.foreach{ d ⇒
          try{
            Thread.sleep(d)}
          catch {case e: InterruptedException ⇒
            akkaLog.error(s"[Pump.pushUserMessage] Error on Thread.sleep, msg: $msg, error: $e, block $blockClassName")
            Thread.currentThread().interrupt()}}})
  private[core] def userLogInfo(message: String): Unit = drive ! M.UserLogInfo(message)
  private[core] def userLogWarn(message: String): Unit = drive ! M.UserLogWarn(message)
  private[core] def userLogError(error: Option[Throwable], message: String): Unit = drive ! M.UserLogError(error, message)
  private[core] def sendUiEvent(event: UIEvent): Unit = drive ! M.UserUIEvent(event)}
