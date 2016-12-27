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

package examples.tools.math

import mathact.tools.indicators.BoolIndicator
import mathact.tools.math.logic.bool._
import mathact.tools.pots.BoolSwitch
import mathact.tools.workbenches.SimpleWorkbench


/** Example of using boolean logic operators
  * Created by CAB on 26.12.2016.
  */

class BooleanLogicExample extends SimpleWorkbench {
  //Sketch parameters
  heading = "Boolean logic example"
  //Helpers
  val potX = new BoolSwitch{ name = "In X" }
  val potY = new BoolSwitch{ name = "In Y" }
  val indicator = new BoolIndicator{ name = "Out" }
  //Operators
  val not = new Not
  val nor = new Nor
  val and = new And
  val or = new Or
  //Connecting
  potX ~> not ~> indicator.in("NOT")
  potX ~> nor
  potX ~> nor ~> indicator.in("NOR")
  potX ~> and
  potY ~> and ~> indicator.in("AND")
  potX ~> or
  potY ~> or ~> indicator.in("OR")}