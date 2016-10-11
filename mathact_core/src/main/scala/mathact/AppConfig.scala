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

package mathact

import javafx.scene.Parent

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.core.model.config._

import scala.concurrent.duration._
import scalafx.scene.image.Image
import scalafxml.core.{FXMLLoader, NoDependencyResolver, FXMLView}


/** Read and hold main commonConfig
  * Created by CAB on 03.09.2016.
  */

private [mathact] class AppConfig extends MainConfigLike{
  //Load commonConfig
  val config = ConfigFactory.load()
  //Main config
  val sketchBuildingTimeout = config.getInt("main.sketch.building.timeout").millis
  //Parse pumping config
  val pumping = new PumpingConfigLike{
    val pump = new PumpConfigLike{
      val askTimeout = Timeout(config.getInt("plumbing.pump.ask.timeout").millis)}
    val drive = new DriveConfigLike{
      val pushTimeoutCoefficient = config.getInt("plumbing.push.timeout.coefficient")
      val startFunctionTimeout = config.getInt("plumbing.start.function.timeout").millis
      val messageProcessingTimeout = config.getInt("plumbing.message.processing.timeout").millis
      val stopFunctionTimeout = config.getInt("plumbing.stop.function.timeout").millis
      val impellerMaxQueueSize = config.getInt("plumbing.impeller.max.queue.size")
      val uiOperationTimeout = config.getInt("plumbing.ui.operation.timeout").millis}}
  //Build SketchUI config
  val sketchUI = new SketchUIConfigLike{}
  //Main SketchUI config
  val mainUI = new MainUIConfigLike{}
  //Parse user logging
  val userLogging = new UserLoggingConfigLike{
    val showUIOnError = config.getBoolean("view.logging.show.ui.on.error")}
  //Visualization
  val visualization = new VisualizationConfigLike{}}
