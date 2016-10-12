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

package mathact

import akka.actor.{PoisonPill, Props, ActorRef, ActorSystem}
import akka.event.Logging
import akka.util.Timeout
import mathact.core.control.infrastructure.MainController
import mathact.core.control.view.main.MainUIActor
import mathact.core.model.config.MainConfigLike
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.enums.SketchStatus
import mathact.core.gui.JFXApplication
import mathact.core.model.messages.M
import mathact.core.bricks.{WorkbenchLike, SketchContext}
import scala.collection.mutable.{ArrayBuffer ⇒ MutList}
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
import scala.reflect._
import scalafx.application.Platform
import akka.pattern.ask
import scala.concurrent.duration._


/** Root application object and class
  * Created by CAB on 17.06.2016.
  */

private [mathact] object Application{
  //Parameters
  private val beforeTerminateTimeout = 1.seconds
  private val creatingSketchContextTimeout = 5.seconds
  //Enums
  object State extends Enumeration { val Starting, Work, Stopping = Value }
  //Variables
  private var state: State.Value = State.Starting
  //Actor system
  private val system = ActorSystem("MathActActorSystem")
  private implicit val execContext = system.dispatcher
  private val log = Logging.getLogger(system, this)
  private val config = new AppConfig
  log.info(s"[Application] Starting of program...")
  //Main controller
  private val mainController: ActorRef = system.actorOf(Props(
    new MainController(config, doStop){
      val mainUi = context.actorOf(Props(new MainUIActor(config.mainUI, self)), "MainControllerUIActor")
      def createSketchController(config: MainConfigLike, sketchData: SketchData, mainController: ActorRef): ActorRef = {

        ???

      }}),
    "MainControllerActor")
  //Stop proc
  private def doStop(exitCode: Int): Unit = Future{
    state = State.Stopping
    log.info(s"[Application.doStop] Stopping of program, before terminate timeout: $beforeTerminateTimeout milliseconds.")
    Thread.sleep(beforeTerminateTimeout.toMillis)
    Platform.exit()
    system.terminate().onComplete{_ ⇒ System.exit(exitCode)}}
  private def doTerminate(): Unit = {
    log.error(s"[Application.doStop] Application, terminated.")
    mainController ! PoisonPill
    doStop(-1)}
  //Methods
  /** Starting of application
    * @param sketches - List[(class of sketch, name of sketch)]
    * @param args - App arguments */
  def start(sketches: List[SketchData], args: Array[String]): Unit =
    try{
      //Check state
      state match{
        case State.Starting ⇒
          //Run Java FX Application
          JFXApplication.init(args, log)
          Platform.implicitExit = false
          log.debug(s"[Application.start] JFXApplication created, starting application.")
          state = State.Work
          mainController ! M.MainControllerStart(sketches)
        case st ⇒
          throw new IllegalStateException(
            s"[Application.start] This method can be called only if App in Starting state, current state: $st")}}
    catch { case e: Throwable ⇒
      log.error(s"[Application.start] Error on start: $e, terminate ActorSystem.")
      doTerminate()
      throw e}
  /** Get of SketchContext for new Workbench
    * @param workbench - Workbench
    * @return - MainController ActorRef or thrown exception */
  def getSketchContext(workbench: WorkbenchLike): SketchContext = state match{
    case State.Work ⇒
      val opClassName = Option(workbench.getClass.getCanonicalName)
      val askTimeout = Timeout(creatingSketchContextTimeout).duration
      log.debug(
        s"[Application.getSketchContext] Try to create SketchContext for workbench $workbench, " +
        s"class name: $opClassName, askTimeout: $askTimeout.")
      //Check className
      opClassName match{
        case Some(className) ⇒
          //Ask for new context
          Await
            .result(
              ask(
                mainController,
                M.NewSketchContext(workbench, className))(askTimeout).mapTo[Either[Exception,SketchContext]],
              askTimeout)
            .fold(
              e ⇒ {
                log.debug(s"[Application.getSketchContext] Error on ask for ${workbench.getClass.getName}, err: $e.")
                throw e},
              wc ⇒ {
                log.debug(s"[Application.getSketchContext] SketchContext created for ${workbench.getClass.getName}.")
                wc})
        case None ⇒
          throw new IllegalArgumentException(
            s"[Application.getSketchContext] No canonical name of workbench class $workbench")}
    case st ⇒
      throw new IllegalStateException(
        s"[Application.getSketchContext] This method can be called only if App in Work state, current state: $st")}
  //Logging methods
  object appLog {
    def debug(msg: String): Unit = log.debug(s"[Application.appLog] $msg")
    def info(msg: String): Unit = log.info(s"[Application.appLog] $msg")
    def warning(msg: String): Unit = log.warning(s"[Application.appLog] $msg")
    def error(msg: String): Unit = log.error(s"[Application.appLog] $msg")}}


class Application {
  //Variables
  private val sketchList = MutList[SketchDsl]() //Canonical class name → SketchDsl
  //Add sketch DSL
  class SketchDsl(clazz: Class[_], sName: Option[String], sDesc: Option[String], isAutorun: Boolean) {
    //Add to list
    sketchList += this
    //Methods
    def name(n: String): SketchDsl = new SketchDsl(clazz, n match{case "" ⇒ None ;case _ ⇒ Some(n)}, sDesc, isAutorun)
    def description(s: String): SketchDsl = new SketchDsl(clazz, sName, s match{case "" ⇒ None; case _ ⇒ Some(s)}, isAutorun)
    def autorun:  SketchDsl = new SketchDsl(clazz, sName, sDesc, true)
    private[mathact] def getData:(Class[_],Option[String],Option[String],Option[String],Boolean) =
      (clazz, Option(clazz.getCanonicalName), sName, sDesc, isAutorun)}
  def sketchOf[T <: WorkbenchLike : ClassTag]: SketchDsl = new SketchDsl(classTag[T].runtimeClass,None,None,false)
  //Main
  def main(arg: Array[String]):Unit = {
    //Build sketch list
    val sketches = sketchList
      .toList
      .map(_.getData)
      .foldRight(List[SketchData]()){
        case (s,l) if s._2.isEmpty ⇒
          throw new IllegalArgumentException(s"[Application.main] No canonical class name for: $s" )
        case (s,l) if l.exists(_.clazz.getCanonicalName == s._1.getCanonicalName) ⇒
          l
        case ((c, Some(cn), n, d, a), l) ⇒
          SketchData(c, cn, n, d, a, ???, ???) +: l}
    //Construct Application
    Application.start(sketches, arg)}}
