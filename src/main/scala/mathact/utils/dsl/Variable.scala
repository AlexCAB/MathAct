package mathact.utils.dsl
import java.lang.reflect.Field


/**
 * Variable parameters holder
 * Created by CAB on 09.03.2015.
 */

case class Variable[T](
  name:String,
  field:Field,
  min:Double,
  max:Double,
  value:T,
  changedFun:Option[T⇒Boolean])