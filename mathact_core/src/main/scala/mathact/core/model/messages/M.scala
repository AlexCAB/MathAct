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

package mathact.core.model.messages

import akka.actor.ActorRef
import mathact.core.bricks.WorkbenchLike
import mathact.core.model.data.pipes.{InletData, OutletData}
import mathact.core.model.data.sketch.{SketchInfo, SketchData}
import mathact.core.model.data.visualisation.ToolBuiltInfo
import mathact.core.model.enums._
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.fitting.{Plug, Socket, InPipe, OutPipe}

import scala.concurrent.duration.FiniteDuration
import scalafx.scene.paint.Color


/** Global messages, to communicate between actors.
  * Created by CAB on 23.05.2016.
  */

private [mathact] object M {
  //Application - MainController
  case class MainControllerStart(sketches: List[SketchData]) extends Msg
  case class NewSketchContext(workbench: WorkbenchLike, sketchClassName: String) extends Msg
  //MainController - MainUI
  case class SetSketchList(sketches: List[SketchInfo]) extends Msg //Show UI
  case class RunSketch(sketch: SketchInfo) extends Msg     //MainUI sends it and Hide UI
  case object HideMainUI extends Msg
  case object MainCloseBtnHit extends Msg
  case object TerminateMainUI extends Msg
  //MainController - SketchControllerActor
  case object LaunchSketch extends StateMsg                   //Sends by main controller to, initiate sketch
  case object ShutdownSketch extends StateMsg                 //Sends by main controller stop sketch, initiate sketch
  case class SketchBuilt(className: String) extends Msg
  case class SketchDone(className: String) extends Msg
  case class SketchError(className: String, error: Throwable) extends Msg
  case class GetSketchContext(sender: ActorRef) extends Msg
  //SketchControllerActor - SketchUI
  case object ShowSketchUI extends Msg
  case object HideSketchUI extends Msg
  case class SketchUIChanged(isShow: Boolean) extends Msg
  case class UpdateSketchUIState(state: Map[SketchUIElement, SketchUiElemState]) extends Msg
  case class SketchUIActionTriggered(element: SketchUIElement, action: Any) extends Msg
  case class SetSketchUIStatusString(message: String, color: Color) extends Msg
  case object TerminateSketchUI extends Msg
  //SketchControllerActor - UserLogging
  case object ShowUserLoggingUI extends Msg
  case object HideUserLoggingUI extends Msg
  case class UserLoggingUIChanged(isShow: Boolean) extends Msg
  case object TerminateUserLogging extends Msg
  //SketchControllerActor - Visualization
  case object ShowVisualizationUI extends Msg
  case object HideVisualizationUI extends Msg
  case class VisualizationUIChanged(isShow: Boolean) extends Msg
  case object TerminateVisualization extends Msg
  //SketchControllerActor - SketchInstance
  case object CreateSketchInstance extends Msg
  case class BuildSketchContextFor(actor: ActorRef) extends Msg
  case class SketchInstanceReady(instance: WorkbenchLike) extends Msg
  case class SketchInstanceFail(error: Throwable) extends Msg
  case object TerminateSketchInstance extends Msg
  //SketchControllerActor - PumpingActor  (life cycle)
  case object BuildPumping extends StateMsg    //Run tool connectivity on sketch start
  case object PumpingBuilt extends Msg
  case object StartPumping extends StateMsg    //Run user start functions on hit UI "START" of by auto-run
  case object PumpingStarted extends Msg
  case object StopPumping extends StateMsg     //Run user stop functions on hit UI "STOP"
  case object PumpingStopped extends Msg
  case object ShutdownPumping extends StateMsg //Force stop of plumping at any state on  .
  case object PumpingShutdown extends StateMsg  //Normal stop, sends by Pumping before Terminated
  case class PumpingError(errors: Seq[Throwable]) extends StateMsg //Error stop, sends by Pumping before Terminated, this means plumping terminated by some internal error
  //SketchControllerActor - PumpingActor  (management)
  case object SkipAllTimeoutTask extends Msg
  case object ShowAllToolUi extends Msg
  case object HideAllToolUi extends Msg
  //Object Pump - PumpingActor (ask)
  case class NewDrive(toolPump: PumpLike) extends Msg     //Name and image for display in UI
  //Object Pump - DriveActor (ask)
  case class AddOutlet(pipe: OutPipe[_], name: Option[String]) extends Msg
  case class AddInlet(pipe: InPipe[_], name: Option[String]) extends Msg
  case class ConnectPipes(out: ()⇒Plug[_], in: ()⇒Socket[_]) extends Msg
  case class UserData[T](outletId: Int, value: T) extends Msg
  //PumpingActor - DriveActor (life cycle)
  case object ConstructDrive extends StateMsg //Lock creation of new inlets/outlets
  case object DriveConstructed extends Msg
  case object BuildDrive extends StateMsg //DriveCreating of connections from pending list
  case object DriveBuilt extends Msg
  case object StartDrive extends StateMsg //Run init user code
  case object DriveStarted extends Msg
  case object StopDrive extends StateMsg  //Run sopping user code
  case object DriveStopped extends Msg
  case object TerminateDrive extends StateMsg //Disconnect all connection and terminate
  case class DriveError(error: Throwable) extends StateMsg //Sends by drive at any state, this means drive terminated
  //DriveActor - DriveActor
  case class AddConnection(connectionId: Int, initiator: ActorRef, inletId: Int, outlet: OutletData) extends Msg
  case class ConnectTo(connectionId: Int, initiator: ActorRef, outletId: Int, inlet: InletData) extends Msg
  case class PipesConnected(connectionId: Int, outletId: Int, inletId: Int) extends Msg
  case class UserMessage[T](outletId: Int, inletId: Int, value: T) extends Msg
  case class DriveLoad(subscriberId: (ActorRef, Int), outletId: Int, inletQueueSize: Int) extends Msg //subscriberId: (drive, inletId)
  //DriveActor - ImpellerActor
  case class RunTask[R](kind: TaskKind, id: Int, timeout: FiniteDuration, task: ()⇒R) extends Msg
  case object SkipCurrentTask extends Msg //Makes impeller to skip the current task, but not terminate it (impeller just will not wait for this more)
  case class TaskDone(kind: TaskKind, id: Int, execTime: FiniteDuration, taskRes: Any) extends Msg
  case class TaskTimeout(kind: TaskKind, id: Int, timeFromStart: FiniteDuration) extends Msg
  case class TaskFailed(kind: TaskKind, id: Int, execTime: FiniteDuration, error: Throwable) extends Msg
  //User logging
  case class LogInfo(toolId: Option[Int], toolName: String, message: String) extends Msg
  case class LogWarning(toolId: Option[Int], toolName: String, message: String) extends Msg
  case class LogError(toolId: Option[Int], toolName: String, errors: Seq[Throwable], message: String) extends Msg
  //Visualization - DriveActor
  case class ToolBuilt(builtInfo: ToolBuiltInfo) extends Msg   //Send to Visualization from DriveActor after tool built
  case object AllToolBuilt
  case class SetVisualisationLaval(laval: VisualisationLaval) extends Msg //Send to DriveActor from Visualization
  case object SkipTimeoutTask extends Msg
  case object ShowToolUi extends Msg  //Send to DriveActor from Visualization to show it's UI
  case object HideToolUi extends Msg  //Send to DriveActor from Visualization to hide it's UI

  //TODO Add more

}