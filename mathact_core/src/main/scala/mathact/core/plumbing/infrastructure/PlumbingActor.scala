///* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
// * @                                                                             @ *
// *           #          # #                                 #    (c) 2016 CAB      *
// *          # #      # #                                  #  #                     *
// *         #  #    #  #           # #     # #           #     #              # #   *
// *        #   #  #   #             #       #          #        #              #    *
// *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
// *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
// *     #          #   # # # #   #       #      #  #           #  #         #       *
// *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
// *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
// * @                                                                             @ *
//\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */
//
//package mathact.core.plumbing.infrastructure
//
//import java.util.UUID
//
//import akka.actor.SupervisorStrategy.Resume
//import akka.actor._
//import mathact.core.model.config.PlumbingConfigLike
//import mathact.core.model.data.visualisation.ToolBuiltInfo
//import mathact.core.model.messages.{M, Msg, StateMsg}
//import mathact.core.plumbing.PumpLike
//import mathact.core.plumbing.infrastructure.drive.DriveActor
//import mathact.core.{IdGenerator, ControllerBase}
//
//import scala.collection.mutable.{Map ⇒ MutMap, ListBuffer ⇒ MutList}
//
//
///** Supervisor for all Pumps
//  * Created by CAB on 15.05.2016.
//  */
//
////TODO 1.Перенести верификацию структуры из визуализации сюда и проверять по завершении постройки.
////TODO 2.Добавить льгинг придупереждения в лог пользователя на на попытке добавить инлет или
////TODO   оутлет или сыполнить соединение после старта приложения.
////TODO 3.Реализовать актор размещения UI.
////TODO
////TODO
////TODO
////TODO
////TODO
////TODO
////TODO ---
////TODO 0.Постройка должна выполнятся сразу на старте, и только если оно успешно кнопка "старт", должна активироватся.
////TODO 1.Далее: По BuildPlumbing должен построить все драйвы, если какой то драйв вернул DriveBuildingError,
////TODO   то isDoStopping = true, что по завершении приведёт к разрушению остальных построеных драйвов,
////TODO   и завершению работы драйва (в этом случае нужно ответить контроллеру PlumbingBuildingError и
////TODO   затем PlumbingTerminated).
////TODO 2.Переименовать: StopAndTerminatePlumbing -> StopAndTerminatePlumbing
////TODO                   PlumbingTerminated-> PlumbingTerminated
////TODO 3.По PlumbingTerminated контроллер скетча должен перводить UI в неактивное состояние.
////TODO   Кроме показать/скрыть логинг и визуализацию.
////TODO 4.Проверить чтобы в случае исключения в конструкторе в пользователском коде (воркбенча и/или) инструментов,
////TODO   ошибка логировалясь и UI переходило в неактивное состояние.
////TODO 5.Добавить в лог сообщения об успешной постройке инструмента или ошибке,
////TODO   и об его успешном запуске или ошибке.
////TODO   Так же сообщения о постройке всех инструментов (удачной или не удачной).
////TODO 6.Собственно исправить ошибку с подключением после завершения постройка (на старте).
////TODO 7.Проверить чтобы чтобы подвисшые пользовательские функции корректно логировались, и
////TODO   завершались в ручьную (автоматически они не должны завершаются).
////TODO 8.Подумать о жазненом цикле UI, его нужно конструировать и разрушать, возможно сделать для этого спец методы
////TODO   в ToolUI трайте.
////TODO ---
////TODO 1.Реализовать форсированое завершение по Shutdown и Fail
////TODO 2.Исправить и добавить тесты (для Shutdown и Fail)
////TODO 3.Исправить и бобавить далее по ииерархии
////TODO 4.Добавить terminationHandling в WorkerBase и исправть кода терминации и тесты в остальных акторвх
////TODO
////TODO
////TODO
//private [mathact] class PlumbingActor(
//  config: PlumbingConfigLike,
//  controller: ActorRef,
//  sketchName: String,
//  userLogging: ActorRef,
//  visualization: ActorRef)
//extends ControllerBase(Plumbing.State.Init) with IdGenerator{ import Plumbing.State._, Plumbing.DriveState._
// import Plumbing._
//  //Supervisor strategy
//  override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Resume}
//  //Definitions
//  case class DriveData(
//    drive: ActorRef,
//    toolId: Int,
//    builtInfo: Option[ToolBuiltInfo] = None,
//    driveState: DriveState = DriveInit)
//  //Variables
//  val drives = MutMap[ActorRef, DriveData]()
//  var isShutdown = false
//  val drivesErrors = MutList[Throwable]()
//  //Functions
//  def createDriveActor(toolPump: PumpLike): (ActorRef, Int) = {
//    val toolId = nextIntId
//    val drive = context.actorOf(
//      Props(new DriveActor(config.drive, toolId, toolPump, self, userLogging, visualization)),
//      "DriveOf_" + toolPump.toolName + "_" + UUID.randomUUID)
//    context.watch(drive)
//    (drive, toolId)}
//  def newDrive(toolPump: PumpLike, state: Plumbing.State, actor: PumpLike⇒(ActorRef, Int))
//  :Unit = state match{
//    case Init | Building ⇒
//      //New drive
//      val (drive, toolId) = createDriveActor(toolPump)
//      log.debug(s"[newDrive] New drive created, toolName: ${toolPump.toolName}, drive: $drive")
//      drives += (drive → DriveData(drive, toolId))
//      //Response
//      sender ! Right(drive)
//    case s ⇒
//      //Incorrect state
//      val msg =
//        s"[PlumbingActor.newDrive] Creating of drive not in Building state step, " +
//        s"toolName: ${toolPump.toolName}, state: $s"
//      log.error(msg)
//      sender ! Left(new Exception(msg))}
//  def setDriveState(actor: ActorRef, state: DriveState): Unit = drives.get(actor) match{
//    case Some(driveData) ⇒
//      log.debug(s"[PlumbingActor.setDriveState] Set $state state for drive $actor.")
//      drives += actor → driveData.copy(driveState = state)
//    case None ⇒
//      log.error(s"[PlumbingActor.setDriveState] Unknown drive $actor, registered drives: ${drives.values}.")}
//  def callIfAllDrivesInState(state: DriveState)(proc: ⇒ Unit): Unit = drives
//    .values.exists(_.driveState != state) match{
//      case false ⇒
//        log.debug(s"[PlumbingActor.callIfAllDrivesInState] All drives in $state state, run proc.")
//        proc
//      case true ⇒
//        log.debug(s"[PlumbingActor.callIfAllDrivesInState] Not all drives in $state state, drives: ${drives.values}")}
//  def setAndSendToDrives(state: DriveState, msg: Msg): Unit = drives.foreach{ case (key, driveData) ⇒
//    log.debug(s"[PlumbingActor.setAndSendToDrives] Set $state state and send $msg to drive: $driveData")
//    drives += key → driveData.copy(driveState = state)
//    driveData.drive ! msg}
//  def allDrivesBuilt(): Unit = {
//    log.debug(s"[PlumbingActor.allDrivesBuilt] Report to controller, userLogging and visualization.")
//    controller ! M.PlumbingBuilt
//    userLogging ! M.LogInfo(None, "Plumbing", s"All tool successful built, ready to start!")
//    visualization ! M.AllToolBuilt}
//  def allDrivesStarted(): Unit = {
//    log.debug(s"[PlumbingActor.allDrivesStarted] All drives started, send M.PlumbingStarted, drives: ${drives.values}.")
//    drives.foreach{case (key, driveData) ⇒ drives += key → driveData.copy(driveState = DriveWorking)}
//    controller ! M.PlumbingStarted
//    userLogging ! M.LogInfo(None, "Plumbing", s"All tools started, working!")}
//  def allDrivesStopped(): Unit = {
//    log.debug(s"[PlumbingActor.allDrivesStopped] All drives stopped, send M.PlumbingStopped, drives: ${drives.values}.")
//    controller ! M.PlumbingStopped
//    userLogging ! M.LogInfo(None, "Plumbing", s"All tools stopped, ready to shutdown.")}
//  def doShutdown(state: Plumbing.State): Plumbing.State = state match{
//    case Creating | Building | Starting | Stopping | Terminating ⇒
//      log.debug(s"[PlumbingActor.doShutdown] In the sate $state do nothing, wait for end of operation.")
//      state
//    case Init | Built | Stopped ⇒
//      log.debug(s"[PlumbingActor.doShutdown] In the sate $state just run all drive termination.")
//      setAndSendToDrives(DriveTerminating,  M.TerminateDrive)
//      Terminating
//    case Working ⇒
//      log.debug(s"[PlumbingActor.doShutdown] In the sate $state run all drive stopping.")
//      setAndSendToDrives(DriveStopping,  M.StopDrive)
//      Stopping
//    case _ ⇒
//     log.error(s"[PlumbingActor.doShutdown] Unknown state: $state.")
//     state}
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
//  def reaction: PartialFunction[(Msg, Plumbing.State), Unit] = {
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
//    case (M.HideAllToolUi, _) ⇒ drives.values.foreach(_.drive ! M.HideToolUi)}}
