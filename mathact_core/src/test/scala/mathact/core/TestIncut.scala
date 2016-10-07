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

package mathact.core

import mathact.core.plumbing.fitting.{Inlet, Outlet}

import scala.concurrent.duration.Duration


/** Pipe used in tests
  * Created by CAB on 19.08.2016.
  */

class TestIncut[T] extends Outlet[T] with Inlet[T]{
  //Variables
  private var receivedValues = List[T]()
  private var procTimeout: Option[Duration] = None
  private var procError: Option[Throwable] = None
  //Receive user message
  protected def drain(value: T): Unit = synchronized{
    println(
      s"[TestIncut] do drain, value: $value, procTimeout: $procTimeout, " +
      s"procError: $procError, receivedValues: $receivedValues")
    receivedValues :+= value
    procTimeout.foreach(d ⇒ Thread.sleep(d.toMillis))
    procError.foreach(e ⇒ throw e)}
  //Test methods
  override def toString = s"TestIncut(receivedValues.size: ${receivedValues.size})"
  def setProcTimeout(d: Duration): Unit = synchronized{ procTimeout = Some(d) }
  def setProcError(err: Option[Throwable]): Unit = synchronized{ procError = err }
  def getReceivedValues: List[T] = synchronized{ receivedValues }
  def sendValue(value: T): Unit = pour(value)}