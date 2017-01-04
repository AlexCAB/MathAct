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

package mathact.core.bricks.data

import mathact.core.model.data.sketch.SketchInfo
import mathact.core.model.enums.SketchStatus


/** SketchData model
  * Created by CAB on 19.06.2016.
  */

case class SketchData(
  clazz: Class[_],
  className: String,
  sketchName: Option[String],
  sketchDescription: Option[String],
  autorun: Boolean, //false - manual run
  showUserLogUiAtStart: Boolean,
  showVisualisationUiAtStart: Boolean)
{
  //Converters
  def toSketchInfo(status: SketchStatus) = SketchInfo(className, sketchName, sketchDescription, status)}
