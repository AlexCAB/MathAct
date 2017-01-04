/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2017 CAB      *
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

import akka.actor.{Props, ActorRef}
import mathact.core.bricks.data.SketchData
import mathact.core.bricks.plumbing.fitting.{Socket, Plug}
import mathact.core.bricks.ui.interaction.UIEvent
import mathact.core.model.data.layout.{WindowPreference, WindowState}
import mathact.core.model.data.pipes.{InletData, OutletData}
import mathact.core.model.data.sketch.SketchInfo
import mathact.core.model.data.verification.BlockVerificationData
import mathact.core.model.data.visualisation.{InletInfo, OutletInfo, BlockInfo}
import mathact.core.model.enums._
import mathact.core.model.holders.DriveRef
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}
import mathact.core.sketch.blocks.WorkbenchLike

import scala.concurrent.duration.FiniteDuration
import scalafx.scene.paint.Color


/** Global messages, to communicate between actors.
  * Created by CAB on 23.05.2016.
  */

private[core] object M {
  //Application - MainControllerActor
  case class MainControllerStart(sketches: List[SketchData]) extends Msg
  case class NewSketchContext(workbench: WorkbenchLike, sketchClassName: String) extends Msg
  //MainControllerActor - MainUI
  case class SetSketchList(sketches: List[SketchInfo]) extends Msg //Show UI
  case class RunSketch(sketch: SketchInfo) extends Msg     //MainUI sends it and Hide UI
  case object HideMainUI extends Msg
  case object MainCloseBtnHit extends Msg
  //MainControllerActor - SketchControllerActor
  case object LaunchSketch extends Msg                   //Sends by main controller to, initiate sketch
  case class SketchBuilt(className: String) extends Msg
  case class SketchFail(className: String) extends Msg
  case class SketchDone(className: String) extends Msg
  case class SketchError(className: String, errors: Seq[Throwable]) extends Msg
  case class GetSketchContext(sender: ActorRef) extends Msg
  //SketchControllerActor - SketchUI
  case object ShowSketchUI extends Msg
  case object HideSketchUI extends Msg
  case class SketchUIChanged(isShow: Boolean) extends Msg
  case class UpdateSketchUIState(state: Map[SketchUIElement, SketchUiElemState]) extends Msg
  case class UpdateSketchUITitle(title: String) extends Msg
  case class SketchUIActionTriggered(element: SketchUIElement, action: Any) extends Msg
  case class SetSketchUIStatusString(message: String, color: Color) extends Msg
  //SketchControllerActor - UserLogging
  case object ShowUserLoggingUI extends Msg
  case object HideUserLoggingUI extends Msg
  case class UserLoggingUIChanged(isShow: Boolean) extends Msg
  //SketchControllerActor - Visualization
  case object ShowVisualizationUI extends Msg
  case object HideVisualizationUI extends Msg
  case class VisualizationUIChanged(isShow: Boolean) extends Msg
  //SketchControllerActor - SketchInstance
  case object CreateSketchInstance extends Msg
  case class BuildSketchContextFor(actor: ActorRef) extends Msg
  case class SketchInstanceReady(instance: WorkbenchLike) extends Msg
  case class SketchInstanceError(error: Throwable) extends Msg
  //SketchControllerActor - PlumbingActor  (life cycle)
  case object BuildPlumbing extends Msg    //Run block constructing, connectivity and turning on, on sketch start
  case object PlumbingBuilt extends Msg
  case object PlumbingNoDrivesFound extends Msg //Sends instead of PlumbingBuilt in case no drives found
  case object StartPlumbing extends Msg    //Run user start functions on hit UI "START" of by auto-run
  case object PlumbingStarted extends Msg
  case object StopPlumbing extends Msg     //Run user stop functions and turning off,  on hit UI "STOP"
  case object PlumbingStopped extends Msg
  //SketchControllerActor - PlumbingActor  (management)
  case object SkipAllTimeoutTask extends Msg
  case object ShowAllBlockUi extends Msg
  case object HideAllBlockUi extends Msg
  //Object Pump - PlumbingActor (ask)
  case class NewDrive(blockPump: PumpLike) extends Msg     //Name and image for display in UI
  //Object Pump - DriveActor (ask and tell)
  case class AddOutlet(pipe: OutPipe[_], name: Option[String]) extends Msg
  case class AddInlet(pipe: InPipe[_], name: Option[String]) extends Msg
  case class ConnectPipes(out: Plug[_], in: Socket[_]) extends Msg
  case class UserData[T](outletId: Int, value: T) extends Msg
  case class UserLogInfo(message: String) extends Msg
  case class UserLogWarn(message: String) extends Msg
  case class UserLogError(error: Option[Throwable], message: String) extends Msg
  case class UserUIEvent(event: UIEvent) extends Msg
  case class NewUserActor(props: Props, name: Option[String]) extends Msg
  //Object Pump - LayoutActor (tell)
  case class RegisterWindow(drive: DriveRef, windowId: Int, state: WindowState, prefs: WindowPreference) extends Msg
  case class WindowUpdated(drive: DriveRef, windowId: Int, state: WindowState) extends Msg
  case class LayoutWindow(drive: DriveRef, windowId: Int) extends Msg
  //PlumbingActor - LayoutActor
  case object AllUiInitialized extends Msg
  case object AllUiCreated extends Msg
  //SketchControllerActor - LayoutActor
  case class DoLayout(kind: WindowsLayoutKind) extends Msg
  //LayoutActor - DriveActor
  case class SetWindowPosition(windowId: Int, x: Double, y: Double) extends Msg
  case class WindowPositionUpdated(windowId: Int) extends Msg
  //PlumbingActor - DriveActor (life cycle)
  case object ConstructDrive extends Msg //Lock creation of new inlets/outlets
  case object DriveConstructed extends Msg
  case object ConnectingDrive extends Msg //Creating of connections from pending list
  case object DriveConnected extends Msg
  case object TurnOnDrive extends Msg //Stater user message processing
  case object DriveTurnedOn extends Msg
  case object StartDrive extends Msg //Run user staring  code
  case object DriveStarted extends Msg
  case object StopDrive extends Msg  //Run user sopping  code
  case object DriveStopped extends Msg
  case object TurnOffDrive extends Msg //Stop user message processing, response witt DriveTurnedOff after all message processed
  case object DriveTurnedOff extends Msg
  case class DriveVerification(verificationData: BlockVerificationData) extends Msg
  //PlumbingActor - DriveActor (execution control)
  case object SkipTimeoutTask extends Msg
  //PlumbingActor - DriveActor (UI control)
  case object ShowBlockUi extends Msg
  case object HideBlockUi extends Msg
  //DriveActor - DriveActor
  case class AddConnection(connectionId: Int, initiator: DriveRef, outlet: OutPipe[_], inlet: InPipe[_]) extends Msg
  case class ConnectTo(connectionId: Int, initiator: DriveRef, outlet: OutPipe[_], inlet: InletData) extends Msg
  case class PipesConnected(connectionId: Int, initiator: DriveRef, outlet: OutletData, inlet: InletData) extends Msg
  case class UserMessage[T](outletId: Int, inletId: Int, value: T) extends Msg
  case class DriveLoad(subscriberId: (DriveRef, Int), outletId: Int, inletQueueSize: Int) extends Msg //subscriberId: (drive, inletId)
  //DriveActor - ImpellerActor
  case class RunTask[R](kind: TaskKind, id: Int, timeout: FiniteDuration, skipOnTimeout: Boolean, task: ()⇒R) extends Msg
  case object SkipCurrentTask extends Msg //Makes impeller to skip the current task, but not terminate it (impeller just will not wait for this more)
  case class TaskDone(kind: TaskKind, id: Int, execTime: FiniteDuration, taskRes: Any) extends Msg
  case class TaskTimeout(kind: TaskKind, id: Int, timeFromStart: FiniteDuration, skipOnTimeout: Boolean) extends Msg
  case class TaskFailed(kind: TaskKind, id: Int, execTime: FiniteDuration, error: Throwable) extends Msg
  //DriveActor - UserActorsRoot
  case class CreateUserActor(props: Props, name: Option[String], sender: ActorRef) extends Msg
  //User logging
  case class LogInfo(blockId: Option[Int], blockName: String, message: String) extends Msg
  case class LogWarning(blockId: Option[Int], blockName: String, message: String) extends Msg
  case class LogError(blockId: Option[Int], blockName: String, errors: Seq[Throwable], message: String) extends Msg
  //PlumbingActor - Visualization
  case class BlockConstructedInfo(builtInfo: BlockInfo) extends Msg   //Send to Visualization from DriveActor after block built
  case class BlocksConnectedInfo(outletInfo: OutletInfo, inletInfo: InletInfo) extends Msg   //Send to Visualization from DriveActor after pipes connected
  case object AllBlockBuilt  extends Msg
  case class SetVisualisationLaval(laval: VisualisationLaval) extends Msg //Send to DriveActor from Visualization

  //TODO Add more

}