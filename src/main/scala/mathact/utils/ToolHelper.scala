package mathact.utils


/**
 * Contain general helpers for tools
 * Created by CAB on 15.03.2015.
 */

class ToolHelper[T <: Tool](val thisTool:T, givenName:String, defaultName:String) {
  //Fields
  val toolName = givenName match{
    case n if n != "" ⇒ n
    case _ ⇒ thisTool.getClass.getCanonicalName match{
      case null ⇒ defaultName
      case n ⇒ n.split("[.]").last.replace("$","")}}}
