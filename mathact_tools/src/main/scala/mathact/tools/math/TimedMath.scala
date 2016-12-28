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

package mathact.tools.math

import mathact.core.bricks.blocks.BlockContext
import mathact.core.bricks.linking.LinkThrough2
import mathact.core.bricks.plumbing.wiring.obj.ObjWiring
import mathact.data.basic.SingleValue
import mathact.data.discrete.TimedValue
import mathact.tools.Tool


/** Base class for timed math operators
  * Created by CAB on 05.12.2016.
  */

abstract class TimedMath(context: BlockContext, name: String, imgPath: String)
extends Tool(context, name, imgPath) with ObjWiring
with LinkThrough2[TimedValue, SingleValue, TimedValue]{
  //Abstract evaluation function
  protected def eval(timedInput: Vector[Double], singleInput: Vector[Double]): Double
  //Definitions
  private class TimedInflow(default: Option[TimedValue], processor: Processor) extends Inflow[TimedValue]{
    //Variables
    private var lastReceived: Option[TimedValue] = default
    //Methods
    def getLast: Option[TimedValue] = lastReceived
    def checkTimeWith(standard: Option[TimedValue]): Unit = (standard, lastReceived) match{
      case (Some(last), Some(stn)) if last.time != stn.time ⇒
        lastReceived = None
        logger.warn(s"Received timed values have inconsistent time. last: $last, stn: $stn")
      case _ ⇒} //Do nothing if no last or no standard
    def clean(): Unit = {lastReceived = None}
    protected def drain(v: TimedValue): Unit = {
      lastReceived = Some(v)
      processor.tryToEval()}}
  private class SingleInflow(default: Option[SingleValue]) extends Inflow[SingleValue]{
    //Variables
    private var lastReceived: Option[SingleValue] = default
    //Methods
    def getLast: Option[SingleValue] = lastReceived
    protected def drain(v: SingleValue): Unit = { lastReceived = Some(v) }}
  private class Processor(timeds: ⇒Vector[TimedInflow], singles: ⇒Vector[SingleInflow]) extends Outflow[TimedValue]{
    //Methods
    def tryToEval(): Unit = {
      //Check time of timed
      val firstNonEmpty = timeds.find(_.getLast.nonEmpty).flatMap(_.getLast)
      timeds.foreach(_.checkTimeWith(firstNonEmpty))
      //Get and check last timed
      timeds.map(_.getLast) match{
        case lastTimed if lastTimed.forall(_.nonEmpty) ⇒
          //Get and check last single
          singles.map(_.getLast) match{
            case lastSingles if lastSingles.forall(_.nonEmpty) ⇒
              //Eval and
              val nextVal = TimedValue(
                firstNonEmpty.get.time,
                eval(
                  lastTimed.map(_.get.value),
                  lastSingles.map(_.get.value)))
              //Send next
              pour(nextVal)
              //Clean
              timeds.foreach(_.clean())
            case lastSingles ⇒
              logger.warn(
                s"Cannot evaluate since not all single inlets receive value or have " +
                s"default value. lastSingles: $lastSingles")}
        case _ ⇒}}} //Do nothing if not all timed received (wait for all)
  //Variables
  private var timedInflows = Vector[TimedInflow]()
  private var singleInflows = Vector[SingleInflow]()
  //Processor
  private val processor = new Processor(timedInflows, singleInflows)
  //Functions
  private def buildTimedInflow(default: Option[TimedValue]): TimedInflow = {
    val inflow = new TimedInflow(default, processor)
    timedInflows +:= inflow
    inflow}
  private def buildSingleInflow(default: Option[SingleValue]): SingleInflow = {
    val inflow = new SingleInflow(None)
    singleInflows +:= inflow
    inflow}
  //Connection points
  def inF = Inlet(buildTimedInflow(None))
  def timed(default: Option[TimedValue]) = Inlet(buildTimedInflow(default))
  def inS = Inlet(buildSingleInflow(None))
  def single(default: Option[SingleValue]) = Inlet(buildSingleInflow(default))
  val out = Outlet(processor)}
