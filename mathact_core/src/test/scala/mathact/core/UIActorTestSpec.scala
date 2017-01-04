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

package mathact.core

import javafx.application.Application

import scala.concurrent.Future
import scalafx.application.Platform

import scala.concurrent.ExecutionContext.Implicits.global


/** Base class for testing of UI actors
  * Created by CAB on 21.09.2016.
  */

class UIActorTestSpec extends ActorTestSpec {
  //Starting JFX Application
  Platform.implicitExit = false
  Future{
    println("[UIActorTestSpec] Try to start TestJFXApplication.")
    Application.launch(classOf[TestJFXApplication])
    println("[UIActorTestSpec] TestJFXApplication stopped.")}
  Thread.sleep(500)
  //Stopping JFX Application
  override def afterAll = {
    Platform.exit()
    Thread.sleep(500)
    super.afterAll}}
