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

package mathact.core.app

import java.util.concurrent.ExecutionException

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import mathact.core.app.infrastructure.MainController
import mathact.core.app.view.MainUIActor
import mathact.core.bricks.{SketchContext, WorkbenchLike}
import mathact.core.sketch.infrastructure.controller.SketchControllerActor
import mathact.core.sketch.infrastructure.instance.SketchInstanceActor
import mathact.core.sketch.view.logging.UserLoggingActor
import mathact.core.sketch.view.sketch.SketchUIActor
import mathact.core.sketch.view.visualization.VisualizationActor
import mathact.core.gui.JFXApplication
import mathact.core.model.config.MainConfigLike
import mathact.core.model.data.sketch.SketchData
import mathact.core.model.messages.M
import mathact.core.plumbing.infrastructure.controller.PlumbingActor

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scalafx.application.Platform


/** Root application object and class
  * Created by CAB on 17.06.2016.
  */

private [mathact] object Application{
  //Parameters
  private val beforeTerminateTimeout = 1.seconds
  private val creatingSketchContextTimeout = 5.seconds
  //Variables
  private var mainController: Option[ActorRef] = None
  //Actor system
  private val config = new AppConfig
  private val system = ActorSystem("MathActActorSystem")
  private implicit val execContext = system.dispatcher
  private val log = Logging.getLogger(system, this)
  log.info(s"[Application] Starting of program...")
  //Stop proc
  private def doStop(exitCode: Int): Unit = Future{
    log.info(s"[Application.doStop] Stopping of program, before terminate timeout: $beforeTerminateTimeout milliseconds.")
    Thread.sleep(beforeTerminateTimeout.toMillis)
    Platform.exit()
    system.terminate().onComplete{_ ⇒ System.exit(exitCode)}}
  private def doTerminate(): Unit = {
    log.error(s"[Application.doStop] Application, terminated.")
    mainController.foreach(_ ! PoisonPill)
    doStop(-1)}
  //Methods
  /** Starting of application
    * @param sketches - List[(class of sketch, name of sketch)]
    * @param args - App arguments */
  def start(sketches: List[SketchData], args: Array[String]): Unit =
    try{
      //Check state
      mainController match{
        case None ⇒
          //Run Java FX Application
          JFXApplication.init(args, log)
          Platform.implicitExit = false
          log.debug(s"[Application.start] JFXApplication created, starting application.")
          //Create main controller
          val controller = system.actorOf(Props(
          new MainController(config, doStop){
            val mainUi = context.actorOf(Props(new MainUIActor(config.mainUI, self)), "MainControllerUIActor")
            context.watch(mainUi)
            def createSketchController(config: MainConfigLike, sketchData: SketchData): ActorRef = {
              context.actorOf(Props(
                new SketchControllerActor(config, sketchData, self){
                  val sketchUi = context.actorOf(Props(
                    new SketchUIActor(config.sketchUI, self)),
                    "SketchUIActor_" + sketchData.className)
                  val userLogging = context.actorOf(Props(
                    new UserLoggingActor(config.userLogging, self)),
                    "UserLoggingActor_" + sketchData.className)
                  val visualization = context.actorOf(Props(
                    new VisualizationActor(config.visualization, self)),
                    "VisualizationActor_" + sketchData.className)
                  val plumbing = context.actorOf(Props(
                    new PlumbingActor(config.plumbing, self, sketchName, userLogging, visualization)),
                    "PlumbingActor_" + sketchData.className)
                  val sketchInstance = context.actorOf(Props(
                    new SketchInstanceActor(config.sketchInstance, sketchData, self, userLogging, plumbing)),
                    "SketchInstanceActor_" + sketchData.className)}),
                "SketchControllerActor_" + sketchData.className)}}),
          "MainControllerActor")
          //Start main controller
          mainController = Some(controller)
          controller ! M.MainControllerStart(sketches)
        case Some(c) ⇒
          throw new IllegalStateException(
            s"[Application.start] This method can be called only on start, mainController: $c")}}
    catch { case e: Throwable ⇒
      log.error(s"[Application.start] Error on start: $e, terminate ActorSystem.")
      doTerminate()
      throw new ExecutionException(e)}
  /** Get of SketchContext for new Workbench
    * @param workbench - Workbench
    * @return - MainController ActorRef or thrown exception */
  def getSketchContext(workbench: WorkbenchLike): SketchContext = mainController match{
    case Some(controller) ⇒
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
                controller,
                M.NewSketchContext(workbench, className))(askTimeout).mapTo[Either[Exception,SketchContext]],
              askTimeout)
            .fold(
              e ⇒ {
                log.debug(s"[Application.getSketchContext] Error on ask for ${workbench.getClass.getName}, err: $e.")
                throw new ExecutionException(e)},
              wc ⇒ {
                log.debug(s"[Application.getSketchContext] SketchContext created for ${workbench.getClass.getName}.")
                wc})
        case None ⇒
          throw new IllegalArgumentException(
            s"[Application.getSketchContext] No canonical name of workbench class $workbench")}
    case None ⇒
      throw new IllegalStateException(
        s"[Application.getSketchContext] This method can be called only after start().")}}


private [mathact] abstract class Application {
  //Sketch list
  def sketchList: List[SketchData]
  //Main
  def main(arg: Array[String]):Unit = Application.start(sketchList, arg)}
