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

package mathact.core.control.view.logging


/** User logging object
  * Created by CAB on 28.09.2016.
  */

private [mathact] object UserLogging {
  //Enums
  object LogType extends Enumeration {
    val Info = Value
    val Warn = Value
    val Error = Value}
  type LogType = LogType.Value
  //Data
  case class LogRow(msgType: LogType, toolName: String, message: String)
  //Messages
  case class DoSearch(text: String)
  case class SetLogLevel(level: LogType)
  case class SetLogAmount(amount: Int)
  case object DoClean}
