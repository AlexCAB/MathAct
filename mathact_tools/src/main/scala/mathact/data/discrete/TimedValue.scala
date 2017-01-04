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

package mathact.data.discrete

import mathact.data.basic.SingleValue
import mathact.data.{Timed, Value}

/** Represent of one double value with time-stamp
  * Created by CAB on 13.11.2016.
  */

case class TimedValue(time: Long, value: Double) extends Timed[TimedValue] with Value[TimedValue]{
  def time(t: Long) = copy(time = t)
  def value(v: Double) = copy(value = v)
  def toSingleValue = SingleValue(value)
  override def toString = s"TimedEvent(time = $time, value = $value)"}
