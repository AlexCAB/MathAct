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

import akka.actor.ActorRef
import mathact.core.IdGenerator
import mathact.core.model.data.verification.BlockVerificationData
import mathact.core.model.messages.{M, Msg}
import mathact.core.plumbing.PumpLike
import mathact.core.plumbing.infrastructure.controller.Plumbing.DriveState._
import mathact.core.plumbing.infrastructure.controller.Plumbing.State._
import scala.collection.mutable.{Map ⇒ MutMap, ListBuffer ⇒ MutList}


/** Plumbing life cycle
  * Created by CAB on 21.10.2016.
  */

private [core] trait PlumbingLife extends IdGenerator{  _: PlumbingActor ⇒ import Plumbing._
  //Variables
  private val drives = MutMap[ActorRef, DriveData]()
  private val blocksVerificationData = MutList[BlockVerificationData]()
  //Functions
  private def forDriveDo(driveActor: ActorRef)(proc: DriveData⇒Unit): Unit = {
    //Check data
    assume(
      drives.contains(driveActor),
      s"[PlumbingLife.forDriveDo] Unknown drive $driveActor, registered drives: ${drives.values}")
    //Run proc
    proc(drives(driveActor))}
  //Methods
  /** Creating of tew drive
    * @param blockPump - block data received from pump
    * @param state - current state
    * @param newActor - ref of new drive actor */
  def newDrive(blockPump: PumpLike, state: State, newActor: (Int, PumpLike)⇒ActorRef)
  :Unit = state match{
    case Init | Constructing ⇒
      //New drive
      val blockId = nextIntId
      val drive = createDriveActor(blockId, blockPump)
      log.debug(s"[newDrive] New drive created, bloc: blockClassName, drive: $drive")
      drives += (drive → DriveData(drive, blockId, None, DriveInit))
      //Response
      sender ! Right(drive)
    case _ ⇒
      //Incorrect state
      val blockClassName = blockPump.block.getClass.getTypeName
      val msg =
        s"[PlumbingLife.newDrive] Creating of drive not in Building state step, " +
        s"block: $blockClassName, state: $state"
      userLogging ! M.LogError(
        None,
        "Plumbing",
        Seq(),
        s"Can not create block drive in sate $state, request from: $blockClassName")
      log.error(msg)
      sender ! Left(new Exception(msg))}
  /** Check if drives list empty */
  def isDrivesEmpty: Boolean = {
    log.debug(s"[PlumbingLife.isDrivesEmpty] isEmpty : ${drives.isEmpty}.")
    drives.isEmpty}
  /** Set new state for given drive
    * @param actor - ActorRef, drive actor reference
    * @param newState - DriveState */
  def setDriveState(actor: ActorRef, newState: DriveState): Unit = forDriveDo(actor){ driveData ⇒
    log.debug(s"[PlumbingLife.setDriveState] Set $state state for drive $actor.")
    drives += actor → driveData.copy(driveState = newState)}
  /** Send given message to all drives
    * @param message - Msg message to be send */
  def sendToEachDrive(message: Msg): Unit = drives.foreach{ case (key, driveData) ⇒
    log.debug(s"[PlumbingLife.sendToEachDrive] Send $message to drive: $driveData")
    driveData.drive ! message}
  /** Check if all drives in given state
    * @param state - DriveState, state to check
    * @return - Boolean, true if all in given state */
  def isAllDrivesIn(state: DriveState): Boolean = drives.values.exists(_.driveState != state) match{
    case false ⇒
      log.debug(s"[PlumbingLife.isAllDrivesIn] All drives in $state state, run proc.")
      true
    case true ⇒
      log.debug(s"[PlumbingLife.isAllDrivesIn] Not all drives in $state state, drives: ${drives.values}")
      false}
  /** Set drive state and check if all drives in this state
    * @param actor - ActorRef, drive actor reference
    * @param newState - DriveState
    * @return - Boolean, true if all in given state */
  def setDriveStateAndCheckIfAllIn(actor: ActorRef, newState: DriveState): Boolean = {
    setDriveState(actor, newState)
    isAllDrivesIn(newState)}
  /** No drives found on build */
  def noDrivesOnBuild(): Unit = {
    log.debug(s"[PlumbingLife.noDrivesOnBuild] Log and report to controller")
    userLogging ! M.LogWarning(None, "Plumbing", s"No blocks found on build, nothing to do.")
    controller ! M.PlumbingNoDrivesFound}
  /** Adding of verification data
    * @param verificationData - BlockVerificationData */
  def driveVerification(verificationData: BlockVerificationData): Unit = {
    log.debug(s"[PlumbingLife.blockConstructedInfo] Add to verification list: $verificationData")
    blocksVerificationData += verificationData}
  /** Verify blocks and they connections structure */
  def verifyGraphStructure(): Unit = {
    log.debug("[PlumbingLife.verifyGraphStructure] Start verification.")
    //Preparing
    val blocks: Map[Int, (Set[Int], Set[Int])] = blocksVerificationData
      .map(t ⇒ t.blockId → Tuple2(t.inlets.map(_.inletId).toSet,  t.outlets.map(_.outletId).toSet))
      .toMap
    //Test
    val testRes = blocksVerificationData.flatMap{ block ⇒
      block.inlets.flatMap{ inlet ⇒
        inlet.publishers.flatMap{ publisher ⇒
          blocks.exists{ case (blockId, (_, outletIds)) ⇒
            publisher.blockId == blockId && outletIds.contains(publisher.outletId)}
          match{
            case true ⇒
              None
            case false ⇒
              val msg = s"Not found outlet with blockId = ${publisher.blockId} and inletId = ${publisher.outletId}, " +
                s"which should be a publishers for inlet with blockId = ${block.blockId} and inletId = ${inlet.inletId}"
              Some(msg)}}} ++
        block.outlets.flatMap{ outlet ⇒
          outlet.subscribers.flatMap{ subscriber ⇒
            blocks.exists{ case (blockId, (inletIds, _)) ⇒
              subscriber.blockId == blockId && inletIds.contains(subscriber.inletId)}
            match{
              case true ⇒
                None
              case false ⇒
                val msg = s"Not found inlet with blockId = ${subscriber.blockId} and inletId = ${subscriber.inletId}, " +
                  s"which should be a subscriber for outlet with blockId = ${block.blockId} and inletId = ${outlet.outletId}"
                Some(msg)}}}}
    //Log result
    testRes match{
      case es if es.isEmpty ⇒
        log.debug("[PlumbingLife.verifyGraphStructure] Structure is valid.")
      case errors ⇒
        val verificationData = blocksVerificationData
          .map{ data ⇒
            s"\n  Block( blockId: ${data.blockId}" +
            "\n    inlets:\n       " + data.inlets
              .map{ in ⇒
                s"Inlet( inletId: ${in.inletId}\n" +
                "          publishers:\n            " + in.publishers
                  .map{ pub ⇒ s"Publisher(blockId: ${pub.blockId}, inletId: ${pub.outletId})"}
                  .mkString("\n            ")}
              .mkString("\n      ") +
            "\n    outlets:\n       " + data.outlets
              .map{ out ⇒
                s"Outlet( inletId: ${out.outletId}\n" +
                "          subscribers:\n            " + out.subscribers
                  .map{ sub ⇒ s"Subscriber(blockId: ${sub.blockId}, inletId: ${sub.inletId})"}
                  .mkString("\n            ")}
              .mkString("\n      ")}
          .mkString("\n  ")
        val msg =
          s"[PlumbingLife.verifyGraphStructure] Structure invalid.\n" +
          s"Next errors found:\n   ${errors.mkString("\n    ")}\n" +
          s"Blocks verification data: $verificationData"
        log.error(msg)
        throw new IllegalStateException(msg)}}
  /** All drives constructed */
  def allDrivesConstructed(): Unit = {
    log.debug(s"[PlumbingLife.allDrivesConstructed] Report to layout.")
    layout ! M.AllDrivesConstruct}
  /** All drives built */
  def allDrivesBuilt(): Unit = {
    log.debug(s"[PlumbingLife.allDrivesBuilt] Report to controller, userLogging and visualization.")
    controller ! M.PlumbingBuilt
    userLogging ! M.LogInfo(None, "Plumbing", s"All block successful built, ready to start!")
    visualization ! M.AllBlockBuilt}
  /** All drives started */
  def allDrivesStarted(): Unit = {
    log.debug(s"[PlumbingLife.allDrivesStarted] All drives started, send M.PlumbingStarted, drives: ${drives.values}.")
    drives.foreach{case (key, driveData) ⇒ drives += key → driveData.copy(driveState = DriveWorking)}
    controller ! M.PlumbingStarted
    userLogging ! M.LogInfo(None, "Plumbing", s"All blocks started, working!")}
  /** All drives stopped */
  def allDrivesStopped(): Unit = {
    log.debug(s"[PlumbingLife.allDrivesStopped] All drives stopped, send M.PlumbingStopped, drives: ${drives.values}.")
    controller ! M.PlumbingStopped
    userLogging ! M.LogInfo(None, "Plumbing", s"All blocks stopped, ready to shutdown.")}}
