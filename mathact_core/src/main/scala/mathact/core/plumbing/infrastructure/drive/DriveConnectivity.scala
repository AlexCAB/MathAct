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

package mathact.core.plumbing.infrastructure.drive

import mathact.core.model.data.pipes.{InletData, OutletData}
import mathact.core.model.data.visualisation.{InletInfo, OutletInfo}
import mathact.core.model.holders.DriveRef
import mathact.core.model.messages.M
import mathact.core.plumbing.fitting.pipes.{InPipe, OutPipe}
import scala.collection.mutable.{Map ⇒ MutMap}


/** Handling of connections and disconnections
  * Created by CAB on 22.08.2016.
  */

private[core] trait DriveConnectivity { _: DriveActor ⇒ import Drive._
  //Variables
  private val pendingConnections = MutMap[Int, (OutPipe[_], InPipe[_])]()
  //Methods
  /** Adding of ConnectPipes to pending connections
    * Can be called only from blocks constructor on sketch construction.
    * @param message = ConnectPipes */
  def connectPipesAsk(message: M.ConnectPipes, state: Drive.State): Either[Throwable,Int] = {
    log.debug(s"[DriveConnectivity.connectPipesAsk] Try to check and add connection $message, state: $state")
    (state, message.out, message.in) match{
      case (Drive.State.Init, outlet: OutPipe[_], inlet: InPipe[_])  ⇒
        //On create store to pending connections
        val connectionId = nextIntId
        pendingConnections += (connectionId → Tuple2(outlet, inlet))
        log.debug(s"[DriveConnectivity.connectPipes] Connection added to pending list, connectionId: $connectionId")
        Right(connectionId)
      case  (Drive.State.Init, outlet, inlet)  ⇒
        //Incorrect outlet or inlet type
        val msg =
          s"[DriveConnectivity.doConnectivity] Plug or Socket is not an instance of OutPipe[_] " +
          s"or InPipe[_], outlet: $outlet, inlet: $inlet."
        log.error(msg)
        //User logging
        userLogging ! M.LogWarning(
          Some(blockId),
          blockName.getOrElse(blockClassName),
          s"Plug or Socket is not an instance of OutPipe[_] or InPipe[_], outlet: $outlet, inlet: $inlet.")
        //Return error
        Left(new IllegalStateException(msg))
      case (s, _, _) ⇒
        //Incorrect state
        val msg =
          s"[DriveConnectivity.connectPipes | blockName: $blockClassName, message: $message] " +
          s"Incorrect state $s, required DriveInit"
        log.error(msg)
        //User logging
        val userMsg =
          s"Pipes ${message.out} and ${message.in} can be connected only " +
          s"on block construction, current state $s"
        userLogging ! M.LogWarning(Some(blockId), blockName.getOrElse(blockClassName), userMsg)
        //Return error
        Left(new IllegalStateException(msg))}}
  /** Connecting of pipes on build of drive
    * Sends M.AddConnection to all inlets drive from pendingConnections list */
  def doConnectivity(): Unit = pendingConnections.foreach{ case (connectionId, (outlet, inlet)) ⇒
    log.debug(s"[DriveConnectivity.doConnectivity] Start connecting from $outlet to $inlet, connectionId: $connectionId")
    inlet.pump.drive ! M.AddConnection(connectionId, DriveRef(self), outlet, inlet)}
  /** Adding of connection to given inlet (inletId), send  M.ConnectTo to outlet
    * @param connectionId - Int
    * @param initiator - ActorRef
    * @param outlet - OutPipe[_]
    * @param inlet - InPipe[_] */
  def addConnection(connectionId: Int, initiator: DriveRef, outlet: OutPipe[_], inlet: InPipe[_]): Unit = {
    //Check data
    assume(
      inlet.pump.drive.ref == self,
      s"[DriveConnectivity.addConnection] inlet.pump.drive != self, inlet: $inlet, self: $self")
    assume(
      inlets.contains(inlet.inletId),
      s"[DriveConnectivity.addConnection] inlets not contain inletId, inlet: $inlet, inlets: $inlets")
    //Add to publishers
    val inState = inlets(inlet.inletId)
    val publisher = PublisherData(
      (outlet.pump.drive, outlet.outletId),
      outlet.pump.drive,
      outlet.blockId,
      outlet.outletId)
    inState.publishers += (publisher.id → publisher)
    //Send ConnectTo to outlet drive
    outlet.pump.drive ! M.ConnectTo(
      connectionId,
      initiator,
      outlet,
      InletData(DriveRef(self), blockId, blockName, inState.inletId, inState.name))
    log.debug(s"[DriveConnectivity.addConnection] Connection added from $outlet to $inState.")}
  /** Adding of connection to given outlet (inletId), and send M.PipesConnected
    * @param connectionId - Int
    * @param initiator - ActorRef
    * @param outlet - OutPipe[_]
    * @param inlet - InletData */
  def connectTo(connectionId: Int, initiator: DriveRef, outlet: OutPipe[_], inlet: InletData): Unit = {
    //Check data
    assume(
      outlet.pump.drive.ref == self,
      s"[DriveConnectivity.connectTo] outlet.pump.drive != self, outlet: $outlet, self: $self")
    assume(
      outlets.contains(outlet.outletId),
      s"[DriveConnectivity.connectTo] outlets not contain outletId, outlet: $outlet, outlets: $outlets")
    //Add to subscribers
    val outState = outlets(outlet.outletId)
    val subscriber = SubscriberData(
      (inlet.blockDrive, inlet.inletId),
      inlet.blockDrive,
      inlet.blockId,
      inlet.inletId)
    outState.subscribers += (subscriber.id → subscriber)
    //Send PipesConnected to initiator
    initiator ! M.PipesConnected(
      connectionId,
      initiator,
      OutletData(DriveRef(self), blockId, blockName, outState.outletId, outState.name),
      inlet)
    log.debug(s"[DriveConnectivity.connectTo] Connection added, from: $outlet, to: $inlet")}
  /** Remove connected connection from pendingConnections list
    * @param connectionId - Int
    * @param outlet - Int
    * @param inlet - Int */
  def pipesConnected(connectionId: Int, initiator: DriveRef, outlet: OutletData, inlet: InletData): Unit = {
    //Check data
    assume(
      initiator.ref == self,
      s"[DriveConnectivity.pipesConnected] initiator == self, initiator: $initiator, self: $self")
    assume(
      pendingConnections.contains(connectionId),
      s"[DriveConnectivity.pipesConnected] pendingConnections not contain connectionId, " +
      s"connectionId: $connectionId, pendingConnections: $pendingConnections")
    //Remove from list
    pendingConnections -= connectionId
    //Send BlocksConnectedInfo
    visualization ! M.BlocksConnectedInfo(
      outletInfo = OutletInfo(outlet.blockId, outlet.blockName, outlet.outletId, outlet.outletName),
      inletInfo = InletInfo(inlet.blockId, inlet.blockName, inlet.inletId, inlet.inletName))
    log.debug(
      s"[DriveConnectivity.pipesConnected] Connected, connectionId: $connectionId, outlet: $outlet, inlet: $inlet.")}
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
  def getConnectionsPendingList: Map[Int, (OutPipe[_], InPipe[_])] = pendingConnections.toMap}
