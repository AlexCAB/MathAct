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


/** Set of actor messages
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
  case object MainUITerminated extends Msg
  //MainController - SketchController
  case object StartSketchController extends StateMsg
  case class GetSketchContext(sender: ActorRef) extends Msg
  case object ShutdownSketchController extends StateMsg
  case class SketchBuilt(className: String) extends Msg
  case class SketchDone(className: String) extends Msg
  case class SketchError(className: String, error: Throwable) extends Msg
  case class SketchControllerTerminated(className: String) extends StateMsg
  //SketchController - SketchUI
  case object ShowSketchUI extends Msg
  case object HideSketchUI extends Msg
  case class SketchUIChanged(isShow: Boolean) extends Msg
  case class UpdateSketchUIState(state: Map[SketchUIElement, SketchUiElemState]) extends Msg
  case class SketchUIActionTriggered(element: SketchUIElement, action: Any) extends Msg
  case class SetSketchUIStatusString(message: String, color: Color) extends Msg
  case object TerminateSketchUI extends Msg
  case object SketchUITerminated extends Msg
  //SketchController - UserLogging
  case object ShowUserLoggingUI extends Msg
  case object HideUserLoggingUI extends Msg
  case class UserLoggingUIChanged(isShow: Boolean) extends Msg
  case object TerminateUserLogging extends Msg
  case object UserLoggingTerminated extends Msg
  //SketchController - Visualization
  case object ShowVisualizationUI extends Msg
  case object HideVisualizationUI extends Msg
  case class VisualizationUIChanged(isShow: Boolean) extends Msg
  case object TerminateVisualization extends Msg
  case object VisualizationTerminated extends Msg
  //SketchController - PumpingActor
  case object BuildPumping extends StateMsg
  case object PumpingBuilt extends Msg
  case object PumpingBuildingError extends Msg
  case object PumpingBuildingAbort extends Msg
  case object StartPumping extends StateMsg
  case object PumpingStarted extends Msg
  case object PumpingStartingAbort extends Msg
  case object StopAndTerminatePumping extends StateMsg
  case object PumpingTerminated extends Msg
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
  //PumpingActor - DriveActor
  case object BuildDrive extends StateMsg //Creating of connections from pending list
  case object DriveBuilt extends Msg
  case object DriveBuildingError extends Msg
  case object StartDrive extends StateMsg //Run init user code
  case object DriveStarted extends Msg
  case object StopDrive extends StateMsg  //Run sopping user code
  case object DriveStopped extends Msg
  case object TerminateDrive extends StateMsg //Disconnect all connection and terminate
  case object DriveTerminated extends Msg
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
  case class LogError(toolId: Option[Int], toolName: String, error: Option[Throwable], message: String) extends Msg
  //Visualization - DriveActor
  case class ToolBuilt(builtInfo: ToolBuiltInfo) extends Msg   //Send to Visualization from DriveActor after tool built
  case object AllToolBuilt
  case class SetVisualisationLaval(laval: VisualisationLaval) extends Msg //Send to DriveActor from Visualization
  case object SkipTimeoutTask extends Msg
  case object ShowToolUi extends Msg  //Send to DriveActor from Visualization to show it's UI
  case object HideToolUi extends Msg  //Send to DriveActor from Visualization to hide it's UI

  //TODO Add more

}