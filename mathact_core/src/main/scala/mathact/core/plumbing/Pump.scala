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

import akka.actor.{Props, ActorRef}
import akka.event.Logging
import akka.pattern.ask
import mathact.core.bricks.blocks.SketchContext
import mathact.core.bricks.plumbing.fitting.{Socket, Plug}
import mathact.core.bricks.ui.UIEvent
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.holders.DriveRef
import mathact.core.model.messages.{Msg, M}
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}
import mathact.core.sketch.blocks.BlockLike
import scala.concurrent.Await
import scala.reflect.ClassTag


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
  private[core] val drive = Await
    .result(ask(context.plumbing.ref, M.NewDrive(this))(context.pumpConfig.askTimeout)
      .mapTo[Either[Throwable,DriveRef]], context.pumpConfig.askTimeout.duration)
    .fold(t ⇒ throw new ExecutionException(t), d ⇒ d)
  //Functions
  private def addPipe(msg: Any): (Int, Int) = Await //Return: (block ID, pipe ID)
    .result(
      ask(drive.ref, msg)(context.pumpConfig.askTimeout).mapTo[Either[Throwable,(Int, Int)]],
      context.pumpConfig.askTimeout.duration)
    .fold(
      t ⇒ {
        akkaLog.error(t, s"[Pump.addPipe] Error on adding of pipe, msg: $msg")
        throw new ExecutionException(t)},
      d ⇒ {
        akkaLog.debug(s"[Pump.addPipe] Pipe added, inletId: $d")
        d})
  //Overridden methods
  override def toString: String = s"Pump(context: $context, block: $blockClassName)"
  //Functions
  private def askDriveAndHandleTimeout(msg: Msg): Unit = Await
    .result(
      ask(drive.ref, msg)(context.pumpConfig.askTimeout).mapTo[Either[Throwable, Option[Long]]],  //Either(error,  Option[sleep timeout])
      context.pumpConfig.askTimeout.duration)
    .fold(
      error ⇒ {
        akkaLog.error(
          error,
          s"[Pump.askDriveAndHandleTimeout] Error on ask of drive, msg: $msg, block $blockClassName")
        throw new ExecutionException(error)},
      timeout ⇒ {
        akkaLog.debug(
          s"[Pump.askDriveAndHandleTimeout] Message pushed, msg: $msg, timeout, $timeout, block $blockClassName")
        timeout.foreach{ d ⇒
          try{
            Thread.sleep(d)}
          catch {case error: InterruptedException ⇒
            akkaLog.error(
              error,
              s"[Pump.askDriveAndHandleTimeout] Error on Thread.sleep, msg: $msg, block $blockClassName")
            Thread.currentThread().interrupt()}}})
  def askDrive[T : ClassTag](msg: Msg): T = Await
    .result(
      ask(drive.ref,  msg)(context.pumpConfig.askTimeout).mapTo[Either[Throwable, T]],
      context.pumpConfig.askTimeout.duration)
    .fold(
      t ⇒ {
        akkaLog.error(t, s"[Pump.askDrive] Drive returned error for msg: $msg.")
        throw new ExecutionException(t)},
      r ⇒ {
        akkaLog.debug(s"[Pump.askDrive] Drive returned result: $r")
        r})
  //Methods
  private[core] def addOutlet(pipe: OutPipe[_], name: Option[String]): (Int, Int) =
    addPipe(M.AddOutlet(pipe, name))
  private[core] def addInlet(pipe: InPipe[_], name: Option[String]): (Int, Int) =
    addPipe(M.AddInlet(pipe, name))
  private[core] def connect(out: ()⇒Plug[_], in: ()⇒Socket[_]): Int =
    askDrive[Int](M.ConnectPipes(out, in))
  private[core] def pushUserMessage(msg: M.UserData[_]): Unit =
    askDriveAndHandleTimeout(msg)
  private[core] def askForNewUserActor(props: Props, name: Option[String]): ActorRef =
    askDrive[ActorRef](M.NewUserActor(props, name))
  private[core] def userLogInfo(message: String): Unit =
    drive ! M.UserLogInfo(message)
  private[core] def userLogWarn(message: String): Unit =
    drive ! M.UserLogWarn(message)
  private[core] def userLogError(error: Option[Throwable], message: String): Unit =
    drive ! M.UserLogError(error, message)
  private[core] def sendUiEvent(event: UIEvent): Unit =
    askDriveAndHandleTimeout(M.UserUIEvent(event))
  private[core] def registerWindow(id: Int, state: WindowState, prefs: WindowPreference): Unit =
    context.layout ! M.RegisterWindow(drive, id, state, prefs)
  private[core] def windowUpdated(id: Int, state: WindowState): Unit =
    context.layout ! M.WindowUpdated(drive, id, state)
  private[core] def layoutWindow(id: Int): Unit =
    context.layout ! M.LayoutWindow(drive, id)}
