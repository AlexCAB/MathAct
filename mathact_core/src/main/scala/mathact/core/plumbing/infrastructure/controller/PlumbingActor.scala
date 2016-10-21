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

package mathact.core.plumbing.infrastructure.controller

import java.util.UUID

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import mathact.core.model.config.PlumbingConfigLike
import mathact.core.model.data.visualisation.ToolBuiltInfo
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.drive.DriveActor
import mathact.core.{ControllerBase, IdGenerator}

import scala.collection.mutable.{ListBuffer => MutList, Map => MutMap}


/** Supervisor for all Pumps
  * Created by CAB on 15.05.2016.
  */

//TODO 1.Перенести верификацию структуры из визуализации сюда и проверять по завершении постройки.
//TODO 2.Добавить льгинг придупереждения в лог пользователя на на попытке добавить инлет или
//TODO   оутлет или сыполнить соединение после старта приложения.
//TODO 3.Реализовать актор размещения UI.
//TODO
//TODO
//TODO
//TODO
//TODO
//TODO
//TODO ---
//TODO 0.Постройка должна выполнятся сразу на старте, и только если оно успешно кнопка "старт", должна активироватся.
//TODO 1.Далее: По BuildPlumbing должен построить все драйвы, если какой то драйв вернул DriveBuildingError,
//TODO   то isDoStopping = true, что по завершении приведёт к разрушению остальных построеных драйвов,
//TODO   и завершению работы драйва (в этом случае нужно ответить контроллеру PlumbingBuildingError и
//TODO   затем PlumbingTerminated).
//TODO 2.Переименовать: StopAndTerminatePlumbing -> StopAndTerminatePlumbing
//TODO                   PlumbingTerminated-> PlumbingTerminated
//TODO 3.По PlumbingTerminated контроллер скетча должен перводить UI в неактивное состояние.
//TODO   Кроме показать/скрыть логинг и визуализацию.
//TODO 4.Проверить чтобы в случае исключения в конструкторе в пользователском коде (воркбенча и/или) инструментов,
//TODO   ошибка логировалясь и UI переходило в неактивное состояние.
//TODO 5.Добавить в лог сообщения об успешной постройке инструмента или ошибке,
//TODO   и об его успешном запуске или ошибке.
//TODO   Так же сообщения о постройке всех инструментов (удачной или не удачной).
//TODO 6.Собственно исправить ошибку с подключением после завершения постройка (на старте).
//TODO 7.Проверить чтобы чтобы подвисшые пользовательские функции корректно логировались, и
//TODO   завершались в ручьную (автоматически они не должны завершаются).
//TODO 8.Подумать о жазненом цикле UI, его нужно конструировать и разрушать, возможно сделать для этого спец методы
//TODO   в ToolUI трайте.
//TODO ---
//TODO 1.Реализовать форсированое завершение по Shutdown и Fail
//TODO 2.Исправить и добавить тесты (для Shutdown и Fail)
//TODO 3.Исправить и бобавить далее по ииерархии
//TODO 4.Добавить terminationHandling в WorkerBase и исправть кода терминации и тесты в остальных акторвх
//TODO
//TODO
//TODO Добавить трайт UI, для котрого сдесь реализовать:
//TODO Если инструмент имеет трайт UI то при постройке вызвать метод "показать UI", а при терминировании "закрыть UI".
//TODO Эти мотоды можно вызывать в контексте потока актора (а ни импелера), та как там не будут кода пользователя,
//TODO и он только отправить собщение потоку UI но фактически ничего не будет делать.
//TODO В IU трайте должен быть флаг "показать UI" на старте или нет.
//TODO Так же не стоит забывать о сообщениях ShowToolUi и HideToolUi
//TODO
private [mathact] class PlumbingActor(
  val config: PlumbingConfigLike,
  val controller: ActorRef,
  val sketchName: String,
  val userLogging: ActorRef,
  val visualization: ActorRef)
extends ControllerBase(Plumbing.State.Init)
with PlumbingLife{ import Plumbing._, Plumbing.State._, Plumbing.DriveState._
  //Creators functions
  def createDriveActor(toolId: Int, toolPump: PumpLike): ActorRef = newWorker(
    new DriveActor(config.drive, toolId, toolPump, self, userLogging, visualization),
    "DriveOf_" + toolPump.toolName + "_" + UUID.randomUUID)
  //Message handling
  def reaction: PartialFunction[(Msg, State), State] = {
    //Creating of drive for new tool instance
    case (M.NewDrive(toolPump), _) ⇒
      newDrive(toolPump, state, createDriveActor)
      state




      


    //Run tool constructing, connectivity and turning on, on sketch start
    case (M.BuildPlumbing, Init) ⇒

      Constructing


    //Run user start functions on hit UI "START" of by auto-run
    case (M.StartPlumbing, TurnedOn) ⇒

      Starting

    //Run user stop functions and turning off, on hit UI "STOP"
    case (M.StopPlumbing, Working)  ⇒


      Stopping







  }
  //Cleanup
  def cleanup(): Unit = {

    println("[DriveActor.cleanup] TODO")  //TODO Очистка ресурсов, (пока вероятно зедсь ничего не будет, но проверить)

  }






//  //Receives
//  /** Reaction on StateMsg'es */
//  def onStateMsg: PartialFunction[(StateMsg, Plumbing.State), Unit] = {
//    //Build all to all drives, on done response with ConstructDrive
//    case (M.BuildPlumbing, Init) ⇒
//      state = Creating
//      setAndSendToDrives(DriveCreating,  M.ConstructDrive)
//    //Switch to Starting, send BuildDrive to all drives, on done response with PlumbingStarted
//    case (M.StartPlumbing, Built) ⇒
//      state = Starting
//      setAndSendToDrives(DriveStarting,  M.StartDrive)
//    //Stopping of all drives, on done response with PlumbingStopped
//    case (M.StopPlumbing, Working) ⇒
//      state = Stopping
//      setAndSendToDrives(DriveStopping,  M.StopDrive)
//    //Shutdown of all drives at any stare, on done response with PlumbingShutdown
//    case (M.ShutdownPlumbing, st) ⇒
//      isShutdown = true
//      state = doShutdown(st)
//    //Terminate of all drives, on done response with PlumbingTerminated
//    case (M.DriveError(error), st) ⇒
//      isShutdown = true
//      drivesErrors += error
//      state = doShutdown(st)}
//  /** Handling after reaction executed */
//  def postHandling: PartialFunction[(Msg, Plumbing.State), Unit] = {
//    //Drive constructed, if all ready switch to building or to termination if isShutdown = true
//    case (M.DriveConstructed, Creating) ⇒ callIfAllDrivesInState(DriveCreated){ isShutdown match {
//      case false ⇒
//        state = Building
//        setAndSendToDrives(DriveBuilding,  M.BuildDrive)
//      case true ⇒
//        state = Terminating
//        setAndSendToDrives(DriveTerminating,  M.TerminateDrive)}}
//    //Check if all drive built, if so switch to Built and send PlumbingBuilt to controller
//    case (M.DriveBuilt, Building) ⇒ callIfAllDrivesInState(DriveBuilt){ isShutdown match {
//      case false ⇒
//        state = Built
//        allDrivesBuilt()
//      case true ⇒
//        state = Terminating
//        setAndSendToDrives(DriveTerminating,  M.TerminateDrive)}}
//    //Check if all drive started, if so switch to Working and send PlumbingStarted
//    case (M.StartPlumbing | M.DriveStarted, Starting) ⇒ callIfAllDrivesInState(DriveStarted){ isShutdown match {
//      case false ⇒
//        state = Working
//        allDrivesStarted()
//      case true ⇒
//        state = Stopping
//        setAndSendToDrives(DriveStopping,  M.StopDrive)}}
//    //Check if all drive stopped, if so send TerminateDrive to all drives
//    case (M.DriveStopped, Stopping) ⇒ callIfAllDrivesInState(DriveStopped){ isShutdown match {
//      case false ⇒
//        state = Stopped
//        allDrivesStopped()
//      case true ⇒
//        state = Terminating
//        setAndSendToDrives(DriveStopped,  M.TerminateDrive)}}}
//  /** Handling of actor termination*/
//  def terminationHandling: PartialFunction[(ActorRef, Plumbing.State), Unit] = {
//    case (actor, Terminating) ⇒
//      setDriveState(actor, DriveTerminated)
//      callIfAllDrivesInState(DriveTerminated){
//        drivesErrors.isEmpty match{
//          case true ⇒ controller ! M.PlumbingShutdown
//          case false ⇒ controller ! M.PlumbingError(drivesErrors.toList)}
//        self ! PoisonPill }}
//  /** Actor reaction on messages */
//  def oldReaction: PartialFunction[(Msg, Plumbing.State), Unit] = {
//    //DriveCreating of new drive for tool (ask request)
//    case (M.NewDrive(toolPump), state) ⇒ newDrive(toolPump, state, createDriveActor)
//    //Updates of driveState
//    case (M.DriveConstructed, Creating) ⇒ setDriveState(sender, DriveCreated)
//    case (M.DriveBuilt, Building) ⇒ setDriveState(sender, DriveBuilt)
//    case (M.DriveStarted, Starting) ⇒ setDriveState(sender, DriveStarted)
//    case (M.DriveStopped, Stopping) ⇒ setDriveState(sender, DriveStopped)
//    //Re send SkipAllTimeoutTask to all drives
//    case (M.SkipAllTimeoutTask, _) ⇒ drives.values.foreach(_.drive ! M.SkipTimeoutTask)
//    case (M.ShowAllToolUi, _) ⇒ drives.values.foreach(_.drive ! M.ShowToolUi)
//    case (M.HideAllToolUi, _) ⇒ drives.values.foreach(_.drive ! M.HideToolUi)}







}
