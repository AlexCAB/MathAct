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

/** Base class for tall tools.
  * Created by CAB on 07.05.2016.
  */

private[mathact] abstract class Tool(context: SketchContext, name: String) extends Block(context){

//TODO Переметить в UI
//  //Variables
//  private var _title: String = name
//  //DSL
//  def title_=(v: String) { _title = v match{case "" ⇒ name; case s ⇒ name + " - " + s} }
//  def title = _title


  //Abstract callbacks (will called by system after sketch will constructed)
  private[mathact] def blockName: Option[String] = Some(name)

  //TODO Add more

}
