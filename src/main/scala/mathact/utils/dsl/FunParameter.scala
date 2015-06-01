package mathact.utils.dsl

/**
 * Function parameter holder.
 * Created by CAB on 31.05.2015.
 */

case class FunParameter[T](name:Option[String], fun:()⇒T){
  private var lastValue:Option[T] = None
  def getValue:Option[T] = {  //Return None if value not chanced
    val v = fun()
    if(lastValue.contains(v)){None}else{lastValue = Some(v); lastValue}}
  def getValueWithName:Option[(Option[String], T)] = {  //Return None if value not chanced
  val v = fun()
    if(lastValue.contains(v)){None}else{lastValue = Some(v); lastValue.map(v ⇒ (name, v))}}}
