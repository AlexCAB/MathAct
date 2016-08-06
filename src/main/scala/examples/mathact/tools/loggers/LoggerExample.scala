package examples.mathact.tools.loggers
import mathact.tools.Workbench
import mathact.tools.loggers.Logger
import mathact.tools.doers.Doer


/**
 * Example of using Logger tool.
 * Created by CAB on 17.03.2015.
 */

object LoggerExample extends Workbench{
  //Variables
  var n = 0
  //Creating Logger
  object MyLogger extends Logger
  //Creating Doer
  new Doer{make{
    n % 3 match{
      case 0 ⇒ MyLogger.log(s"Step $n")
      case 1 ⇒ MyLogger.red(s"Red step $n")
      case 2 ⇒ MyLogger.white(s"White step $n")}
    n += 1
  }}
}
