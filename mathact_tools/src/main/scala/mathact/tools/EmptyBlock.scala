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

package mathact.tools

import mathact.core.bricks.blocks.{Block, SketchContext}


/** Empty block
  * Created by CAB on 24.10.2016.
  */

class EmptyBlock(implicit context: SketchContext) extends Block(context){
  //Variables
  private var _title: Option[String] = None
  private var _imagePath: Option[String] = None
  //DSL
  def title_=(v: String) { _title = v match{case "" ⇒ None; case s ⇒ Some(s)} }
  def imagePath_=(v: String) { _imagePath =  v match{case "" ⇒ None; case s ⇒ Some(s)} }
  //Abstract callbacks (will called by system after sketch will constructed)
  private[mathact] def blockName: Option[String] = _title
  private[mathact] def blockImagePath: Option[String] = _imagePath

  //TODO Add more

}
