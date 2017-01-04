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

package mathact.tools

import mathact.core.bricks.blocks.{Block, BlockContext}


/** Empty block
  * Created by CAB on 24.10.2016.
  */

class EmptyBlock(implicit blockContext: BlockContext) extends Block(blockContext){
  //Variables
  private var _name: Option[String] = None
  private var _imagePath: Option[String] = None
  //DSL
  def name_=(v: String) { _name = v match{case "" ⇒ None; case s ⇒ Some(s)} }
  def name = _name
  def imagePath_=(v: String) { _imagePath =  v match{case "" ⇒ None; case s ⇒ Some(s)} }
  def imagePath = _imagePath
  //Abstract callbacks (will called by system after sketch will constructed)
  private[mathact] def blockName: Option[String] = _name
  private[mathact] def blockImagePath: Option[String] = _imagePath

  //TODO Add more

}
