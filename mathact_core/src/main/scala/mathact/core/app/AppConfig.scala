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

package mathact.core.app

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.core.model.config._
import mathact.core.model.enums._

import scala.concurrent.duration._


/** Read and hold main commonConfig
  * Created by CAB on 03.09.2016.
  */

private[core] class AppConfig extends MainConfigLike{
  //Load commonConfig
  val config = ConfigFactory.load()
  //Sketch instance
  val sketchInstance = new SketchInstanceConfigLike{
    val commonConfig = config
    val sketchBuildingTimeout = config.getInt("main.sketch.building.timeout").millis
    val pumpConfig = new PumpConfigLike{
      val askTimeout = Timeout(config.getInt("plumbing.pump.ask.timeout").millis)}}
  //Layout config
  val layout = new LayoutConfigLike{
    val initialLayoutKind = WindowsLayoutKind.withName(config.getString("view.layout.initial"))
    val screenIndent = config.getInt("view.layout.indent")
    val stairsStep = config.getInt("view.layout.algo.stairs.step")}
  //Parse plumbing config
  val plumbing = new PlumbingConfigLike{
    val drive = new DriveConfigLike{
      val pushTimeoutCoefficient = config.getInt("plumbing.drive.push.timeout.coefficient")
      val startFunctionTimeout = config.getInt("plumbing.drive.start.function.timeout").millis
      val messageProcessingTimeout = config.getInt("plumbing.drive.message.processing.timeout").millis
      val stopFunctionTimeout = config.getInt("plumbing.drive.stop.function.timeout").millis
      val impellerMaxQueueSize = config.getInt("plumbing.drive.impeller.max.queue.size")
      val uiOperationTimeout = config.getInt("plumbing.drive.ui.operation.timeout").millis
      val uiSlowdownCoefficient = config.getInt("plumbing.drive.ui.slowdown.coefficient")}}
  //Build SketchUI config
  val sketchUI = new SketchUIConfigLike{}
  //Main SketchUI config
  val mainUI = new MainUIConfigLike{}
  //Parse user logging
  val userLogging = new UserLoggingConfigLike{
    val showUIOnError = config.getBoolean("view.logging.show.ui.on.error")}
  //Visualization
  val visualization = new VisualizationConfigLike{}}
