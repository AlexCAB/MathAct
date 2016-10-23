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

package mathact.core.plumbing.infrastructure.drive

import akka.actor.ActorRef
import mathact.core.model.data.pipes.{InletData, OutletData}
import mathact.core.model.data.visualisation.{InletInfo, OutletInfo}
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting._
import scala.collection.mutable.{Map ⇒ MutMap}

import scala.util.Try


/** Handling of connections and disconnections
  * Created by CAB on 22.08.2016.
  */

private [mathact] trait DriveConnectivity { _: DriveActor ⇒ import Drive._
  //Variables
  private val pendingConnections = MutMap[Int, M.ConnectPipes]()
  //Methods
  /** Adding of ConnectPipes to pending connections
    * Can be called only from blocks constructor on sketch construction.
    * @param message = ConnectPipes */
  def connectPipesAsk(message: M.ConnectPipes, state: Drive.State): Either[Throwable,Int] = state match{
    case Drive.State.Init ⇒
      //On create store to pending connections
      val connectionId = nextIntId
      pendingConnections += (connectionId → message)
      log.debug(s"[DriveConnectivity.connectPipes] Connection added to pending list, connectionId: $connectionId")
      Right(connectionId)
    case s ⇒
      //Incorrect state
      val msg =
        s"[DriveConnectivity.connectPipes | blockName: ${pump.blockName}, message: $message] " +
        s"Incorrect state $s, required DriveInit"
      log.error(msg)
      //User logging
      val outPipe = Try{message.out()}.toOption
      val inPipe = Try{message.in()}.toOption
      val userMsg =
        s"Pipes ${outPipe.getOrElse("---")} and ${inPipe.getOrElse("---")} can be connected only " +
        s"on block construction, current state $s"
      userLogging ! M.LogWarning(Some(blockId), pump.blockName, userMsg)
      //Return error
      Left(new IllegalStateException(msg))}
  /** Connecting of pipes on build of drive
    * Sends M.AddConnection to all inlets drive from pendingConnections list */
  def doConnectivity(): Unit = pendingConnections.foreach{
    case (connectionId, M.ConnectPipes(out, in)) ⇒ (out(),in()) match{
      case (outlet: OutPipe[_], inlet: InPipe[_]) ⇒
        inlet.pipeData.blockDrive ! M.AddConnection(connectionId, self, inlet.pipeData.pipeId, outlet.pipeData)
      case (o, i) ⇒
        log.error(
          s"[DriveConnectivity.doConnectivity] Plug or Socket is not an instance " +
            s"of Outlet[_] or Inlet[_], out: $o, in: $i.")
         throw new IllegalArgumentException(
            s"Plug or Socket is not an instance of Outlet[_] or Inlet[_], out: $o, in: $i.")}}
  /** Adding of connection to given inlet (inletId), send  M.ConnectTo to outlet
    * @param connectionId - Int
    * @param initiator - ActorRef
    * @param outletId - Int
    * @param outlet - OutletData */
  def addConnection(connectionId: Int, initiator: ActorRef, outletId: Int, outlet: OutletData): Unit = inlets
    .get(outletId) match{
      case Some(inState) ⇒
        inState.publishers += ((outlet.blockDrive, outlet.pipeId) → outlet)
        outlet.blockDrive ! M.ConnectTo(connectionId, initiator, outlet.pipeId, inState.pipe.pipeData)
        log.debug(s"[DriveConnectivity.addConnection] Connection added from $outlet to $inState.")
      case None ⇒
        log.error(s"[DriveConnectivity.addConnection] Inlet with ID $outletId, not in inlets list.")
        throw new IllegalArgumentException(s"Inlet with ID $outletId, not in inlets list.")}
  /** Adding of connection to given outlet (outletId), and send M.PipesConnected
    * @param connectionId - Int
    * @param initiator - ActorRef
    * @param outletId - Int
    * @param inlet - PipeData */
  def connectTo(connectionId: Int, initiator: ActorRef, outletId: Int, inlet: InletData): Unit = outlets
    .get(outletId) match{
      case Some(outlet) ⇒
        val inDrive = inlet.blockDrive
        outlet.subscribers += ((inDrive, inlet.pipeId) → SubscriberData((inDrive, inlet.pipeId), inlet))
        initiator ! M.PipesConnected(connectionId, outlet.pipe.pipeData, inlet)
        log.debug(s"[ConnectTo] Connection added, from: $outlet, to: $inlet")
      case None ⇒
        log.error(s"[DriveConnectivity.connectTo] Outlet with outletId: $outletId, not exist.")
        throw new IllegalArgumentException(s"Outlet with outletId: $outletId, not in inlets list.")}
  /** Remove connected connection from pendingConnections list
    * @param connectionId - Int
    * @param outlet - Int
    * @param inlet - Int */
  def pipesConnected(connectionId: Int, outlet: OutletData, inlet: InletData): Unit = pendingConnections
    .contains(connectionId) match{
      case true ⇒
        log.debug(s"[DriveConnectivity.pipesConnected] Connected, connectionId: $connectionId.")
        //Remove from list
        pendingConnections -= connectionId
        //Send BlocksConnectedInfo
        visualization ! M.BlocksConnectedInfo(
          outletInfo = OutletInfo(
            outlet.blockId,
            Some(outlet.blockName),
            outlet.pipeId,
            outlet.pipeName),
          inletInfo = InletInfo(
            inlet.blockId,
            Some(inlet.blockName),
            inlet.pipeId,
            inlet.pipeName))
      case false ⇒
        log.error(s"[DriveConnectivity.pipesConnected] Unknown connection with connectionId: $connectionId.")
        throw new IllegalArgumentException(s"Unknown connection with connectionId: $connectionId.")}
  /** Check if all connections connected
    * @return - true if all connected */
  def isPendingConListEmpty: Boolean = pendingConnections.isEmpty match{
    case true ⇒
      log.debug(s"[DriveActor.isPendingConListEmpty] All pipes connected: $pendingConnections")
      true
    case false ⇒
      log.debug(s"[DriveActor.isPendingConListEmpty] Not all pipes connected:  $pendingConnections")
      false}
  /** Get of pending list, used in test
    * @return -  Map[Int, M.ConnectPipes] */
  def getConnectionsPendingList: Map[Int, M.ConnectPipes] = pendingConnections.toMap}
