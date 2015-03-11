package mathact.utils.dsl
import java.lang.reflect.Field


/**
 * DSL helper class for VariableBoard like gridRow,
 * Created by CAB on 09.03.2015.
 */

abstract class VariableBoard[T](component:AnyRef, defMin:Double, defMax:Double){
  //Constants
  trait Operator
  case object Value extends Operator
  case object Minimum extends Operator
  case object Maximum extends Operator
  case object MinMax extends Operator
  //Classes
  case class VarParam(
    id:Int,
    min:Option[Double],
    max:Option[Double],
    value:Option[T],
    size:Option[Int],
    isFirst:Boolean, //Or next
    operator:Operator)
  //Variables
  private var idCounter = 0
  private var varsParams = List[VarParam]()
  //Function
  private def newerValue[V](f:Option[V], s:Option[V]):Option[V] = (f,s) match{
    case (_,Some(s)) ⇒ Some(s)
    case (Some(f),None) ⇒ Some(f)
    case _ ⇒ None}
  //Abstract methods
  def createIdValue(id:Int):T
  def getId(value:T):Int
  def checkField(f:Field):Boolean
  def fillDefValue(min:Double, max:Double, size:Option[Int]):T
  def checkParameters(min:Double, max:Double, value:T):(Boolean,String)
  //Methods
  def addVarParameter(min:Option[Double], max:Option[Double], value:Option[T], size:Option[Int], operator:Operator)
  :T = {
    idCounter += 1
    varsParams :+= VarParam(idCounter, min, max, value, size, isFirst = true, operator)
    createIdValue(idCounter)}
  def addNextVarParameter(min:Option[Double], max:Option[Double], value:Option[T], size:Option[Int], operator:Operator)
  :T = {
    varsParams match{
      case l if l.nonEmpty ⇒ {
        val VarParam(pId, pMin, pMax, pValue, pSize, pIsFirst, pOperator) = l.last
        //Check operators sequence
        val isCorrect = (pIsFirst, pOperator, operator) match{
          case (true, Value, Minimum)|(true, Value, Maximum)|(true, Value, MinMax) ⇒ true
          case (true, Minimum, Maximum)|(true, Maximum, Minimum) ⇒ true
          case (false, Minimum, Maximum)|(false, Maximum, Minimum) ⇒ true
          case _ ⇒ false}
        if(! isCorrect){throw new SyntaxException(s"""
          |Incorrect operator sequence: $pOperator($pMin,$pMax,$pValue), $operator($min,$max,$value)
          |in ${component.getClass.getCanonicalName}, definition number $pId.
          |Use: Value, Minimum | Value, Maximum | Value, MinMax |
          |     Minimum, Maximum | Maximum, Minimum |
          |     Value, Minimum, Maximum | Value, Maximum, Minimum.
          |""".stripMargin)}
        //Replacing var param
        varsParams = varsParams.dropRight(1)
        varsParams :+= VarParam(
          pId,
          newerValue(pMin,min),
          newerValue(pMax,max),
          newerValue(pValue,value),
          newerValue(pSize,size),
          isFirst = false,
          operator)
        createIdValue(pId)}
      case _ ⇒ {  //No last
        addVarParameter(min, max, value, size, operator)}}}
  def getVars:List[Variable[T]] = {
    //Get double fields
    val doubleFields = component.getClass.getDeclaredFields.toList.filter(checkField(_))
    if(doubleFields.size != varsParams.size){throw new SyntaxException(s"""
      |Look like in ${component.getClass.getCanonicalName} defined excess double fields.
      |Defined(${varsParams.size}):
      |  ${varsParams.mkString("\n  ")}
      |Found(${doubleFields.size}):
      |  ${doubleFields.mkString("\n  ")}
      |""".stripMargin)}
    //Mapping in parameters
    doubleFields.map(field ⇒ {
      //Prepare Field
      field.setAccessible(true)
      val id = getId(field.get(component).asInstanceOf[T])
      //Get field parameters
      val params = varsParams.find(_.id == id) match{
        case Some(p) ⇒ p
        case _ ⇒ throw new Exception(s"""
          |Internal error in ${component.getClass.getCanonicalName}:
          |For field '$field' with id $id not found parameters in:
          |  ${varsParams.mkString("\n  ")}
          |""".stripMargin)}
      //Calc params
      val min = params.min.getOrElse(defMin)
      val max = params.max.getOrElse(defMax)
      val value = params.value.getOrElse(fillDefValue(max, min, params.size))
      checkParameters(min,max,value) match{
        case (false, msg) ⇒ {throw new SyntaxException(s"""
          |Incorrect initialization parameters in ${component.getClass.getCanonicalName}, definition number ${params.id}.
          |Message: $msg
          |""".stripMargin)}
        case _ ⇒}
      //Check parameters
      //Set field value
      field.set(component, value)
      //Construct Variable
      Variable(field.getName, field, min, max, value)})}}
