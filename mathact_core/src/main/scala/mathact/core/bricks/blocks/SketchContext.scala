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

package mathact.core.bricks.blocks

import akka.actor.ActorSystem
import com.typesafe.config.Config
import mathact.core.model.config.PumpConfigLike
import mathact.core.model.holders._


/** Provide support and management of Workbench
  * Created by CAB on 20.06.2016.
  */

class SketchContext(
  val system: ActorSystem,
  val controller: SketchControllerRef,
  val userLogging: UserLoggingRef,
  val layout: LayoutRef,
  val plumbing: PlumbingRef,
  val pumpConfig: PumpConfigLike,
  val commonConfig: Config)
