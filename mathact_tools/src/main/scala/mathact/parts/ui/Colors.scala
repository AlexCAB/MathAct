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

package mathact.parts.ui
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import java.awt.{Color => JColor}



/** Tools for working with colors
  * Created by CAB on 24.11.2016.
  */

trait Colors {
  //Parameters
  val lineColors = List(Black, Gold, Gray, Green, Bisque, Blue, Honeydew)
  //Variables
  @volatile private var currentColor = 0
  //Methods
  protected def nextColor: Color = {
    val c = lineColors(currentColor)
    currentColor = if(currentColor >= lineColors.size) 0 else currentColor + 1
    c}
  //Helpers
  implicit class ColorEx (c: Color) {
    def toJColor: JColor = new JColor((c.red * 255).toInt, (c.green * 255).toInt, (c.blue * 255).toInt)}}
