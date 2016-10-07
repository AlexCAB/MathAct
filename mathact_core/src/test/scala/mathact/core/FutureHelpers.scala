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

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


/** Future helpers used in tests
  * Created by CAB on 12.08.2016.
  */

trait FutureHelpers {
  //Classes
  implicit class FutureEx[T](future: Future[T]){
    def get(timeout: Duration = 5.second): T = Await.result(future, timeout)
    def await(timeout: Duration = 5.second): Unit = Await.result(future, timeout)}
  //Methods
  def sleep(timeout: Duration):Unit = {
    println(s"[FutureHelpers.sleep] Start of $timeout sleep")
    Thread.sleep(timeout.toMillis)
    println(s"[FutureHelpers.sleep] Done of $timeout sleep")}}