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

import akka.actor._
import mathact.core.model.config.PlumbingConfigLike
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.drive.DriveActor
import mathact.core.ControllerBase


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
//TODO   в BlockUI трайте.
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
//TODO Так же не стоит забывать о сообщениях ShowBlockUi и HideBlockUi
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
  def createDriveActor(blockId: Int, blockPump: PumpLike): ActorRef = newController(
    new DriveActor(config.drive, blockId, blockPump, self, userLogging, visualization),
    "DriveOf_" + blockPump.blockName + "_" + UUID.randomUUID)
  //Message handling
  def reaction: PartialFunction[(Msg, State), State] = {
    //Creating of drive for new block instance
    case (M.NewDrive(blockPump), _) ⇒
      newDrive(blockPump, state, createDriveActor)
      state
    //Run block constructing, connectivity and turning on, on sketch start
    case (M.BuildPlumbing, Init) ⇒
      sendToEachDrive(M.ConstructDrive)
      Constructing
    //Check if all drives constructed, and run connecting if so
    case (M.DriveConstructed, Constructing) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveConstructed) match{
      case true ⇒
        sendToEachDrive(M.ConnectingDrive)
        Connecting
      case false ⇒
        state}
    //Check if all drives connected, and turning on if so
    case (M.DriveConnected, Connecting) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveConnected) match{
      case true ⇒
        sendToEachDrive(M.TurnOnDrive)
        TurningOn
      case false ⇒
        state}
    //Check if all drives turned on, and if so switch to TurnedOn and wait for start command
    case (M.DriveTurnedOn, TurningOn) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveTurnedOn) match{
      case true ⇒
        allDrivesBuilt()
        TurnedOn
      case false ⇒
        state}
    //Run user start functions on hit UI "START" of by auto-run
    case (M.StartPlumbing, TurnedOn) ⇒
      sendToEachDrive(M.StartDrive)
      Starting
    //Check if all drives started, and if so switch to Working
    case (M.DriveStarted, Starting) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveWorking) match{
      case true ⇒
        allDrivesStarted()
        Working
      case false ⇒
        state}
    //Run user stop functions and turning off, on hit UI "STOP"
    case (M.StopPlumbing, Working) ⇒
      sendToEachDrive(M.StopDrive)
      Stopping
    //Check if all drives stopped and run turning off if so
    case (M.DriveStopped, Stopping) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveStopped) match{
      case true ⇒
        sendToEachDrive(M.TurnOffDrive)
        TurningOff
      case false ⇒
        state}
    //Check if all drives turned off and if so switch to TurnedOff and wait for termination
    case (M.DriveTurnedOff, TurningOff) ⇒ setDriveStateAndCheckIfAllIn(sender, DriveTurnedOff) match{
      case true ⇒
        allDrivesStopped()
        TurnedOff
      case false ⇒
        state}
    //Skip timeout task for all drives
    case (M.SkipAllTimeoutTask, _) ⇒
      sendToEachDrive(M.SkipTimeoutTask)
      state
    //Show block UI for all drives
    case (M.ShowAllBlockUi, _) ⇒
      sendToEachDrive(M.ShowBlockUi)
      state
    //Hide block UI for all drives
    case (M.HideAllBlockUi, _) ⇒
      sendToEachDrive(M.HideBlockUi)
      state}
  //Cleanup
  //TODO Очистка ресурсов, (пока вероятно зедсь ничего не будет, но проверить)
  def cleanup(): Unit = {}}
