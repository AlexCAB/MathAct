package mathact.tools.pots
import mathact.utils.{ToolHelper, Environment, Tool}
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.{VariableBoard, SyntaxException}
import mathact.utils.ui.components.{GridFrame, Potentiometer}
import java.lang.reflect.Modifier
import java.lang.reflect.Field


/**
 * Interactive potentiometer board
 * Created by CAB on 08.03.2015.
 */

abstract class PotBoard(
  name:String = "",
  defaultMin:Double = -1,
  defaultMax:Double = 1,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
(implicit environment:Environment)
extends Tool{
  //DSL Methods
  def in(min: ⇒Double, max: ⇒Double):Double =
    doubleVarBoard.addVarParameter(Some(min), Some(max), None, None, doubleVarBoard.MinMax)
  def init(value: ⇒Double):Double =
    doubleVarBoard.addVarParameter(None, None, Some(value), None, doubleVarBoard.Value)
  def zero:Double =
    doubleVarBoard.addVarParameter(None, None, Some(0.0), None, doubleVarBoard.Value)
  def minimum(min: ⇒Double):Double =
    doubleVarBoard.addVarParameter(Some(min), None, None, None, doubleVarBoard.Minimum)
  def maximum(max: ⇒Double):Double =
    doubleVarBoard.addVarParameter(None, Some(max), None, None, doubleVarBoard.Maximum)
  def array(first:Double, next:Double*):Array[Double] =
    arrayVarBoard.addVarParameter(None, None, Some(Array(first) ++ next), None, arrayVarBoard.Value)
  def array:ArrayMethods = new ArrayMethods(()⇒0.0)
  implicit class DoubleEx(v:Double){
    def in(min: ⇒Double, max: ⇒Double):Double =
      doubleVarBoard.addNextVarParameter(Some(min), Some(max), None, None, None, doubleVarBoard.MinMax)
    def minimum(min: ⇒Double):Double =
      doubleVarBoard.addNextVarParameter(Some(min), None, None, None, None, doubleVarBoard.Minimum)
    def maximum(max: ⇒Double):Double =
      doubleVarBoard.addNextVarParameter(None, Some(max), None, None, None, doubleVarBoard.Maximum)
    def changed(cgd:Double⇒Unit):Double =
      doubleVarBoard.addNextVarParameter(None, None, None, None, Some((v)⇒{cgd(v); false}), doubleVarBoard.Changed)
    def changedWithUpdate(cgd:Double⇒Unit):Double =
      doubleVarBoard.addNextVarParameter(None, None, None, None, Some((v)⇒{cgd(v); true}), doubleVarBoard.Changed)}
  class ArrayMethods(defValue:()⇒Double){
    def len(n: ⇒Int):Array[Double] = {
      val value = (0 until n).map(_ ⇒ defValue()).toArray
      arrayVarBoard.addVarParameter(None, None, Some(value), Some(n), arrayVarBoard.Value)}
    def of(v: ⇒Double):ArrayMethods = new ArrayMethods(()⇒{v})}
  implicit class ArrayDoubleEx(vd:Array[Double]){
    def in(min: ⇒Double, max: ⇒Double):Array[Double] =
      arrayVarBoard.addNextVarParameter(Some(min), Some(max), None, None, None, arrayVarBoard.MinMax)
    def minimum(min: ⇒Double):Array[Double] =
      arrayVarBoard.addNextVarParameter(Some(min), None, None, None, None, arrayVarBoard.Minimum)
    def maximum(max: ⇒Double):Array[Double] =
      arrayVarBoard.addNextVarParameter(None ,Some(max), None, None, None, arrayVarBoard.Maximum)
    def changed(cgd:Array[Double]⇒Unit):Array[Double] =
      arrayVarBoard.addNextVarParameter(None ,None, None, None, Some((v)⇒{cgd(v); false}), arrayVarBoard.Maximum)
    def changedWithUpdate(cgd:Array[Double]⇒Unit):Array[Double] =
      arrayVarBoard.addNextVarParameter(None ,None, None, None, Some((v)⇒{cgd(v); true}), arrayVarBoard.Maximum)}
  def updated() = {}
  //Variables
  private var pots:List[Potentiometer] = List()
  //Helpers
  private val helper = new ToolHelper(this, name, "PotBoard")
  private val doubleVarBoard = new VariableBoard[Double](helper.thisTool, defaultMin, defaultMax){
    def createIdValue(id:Int):Double = id.toDouble
    def getId(value:Double):Int = value.toInt
    def checkField(f:Field):Boolean = {f.getType.getName == "double"}
    def fillDefValue(min:Double, max:Double, size:Option[Int]):Double = {(min + max) / 2}
    def checkParameters(min:Double, max:Double, value:Double):(Boolean,String) = (min, max, value) match{
      case _ if value >= min && value <= max ⇒ (true,"")
      case _ ⇒ (false, s"(minimum($min) <= value($value) <= maximum($max)) is false")}}
  private val arrayVarBoard = new VariableBoard[Array[Double]](helper.thisTool, defaultMin, defaultMax){
    def createIdValue(id:Int):Array[Double] = Array[Double](id)
    def getId(value:Array[Double]):Int = value(0).toInt
    def checkField(f:Field):Boolean = {
      f.getType.getName match{
        case "[D" ⇒ Modifier.isFinal(f.getModifiers) match{
          case false ⇒ throw new SyntaxException(
            s"Array definition should be 'val' in ${helper.thisTool.getClass.getCanonicalName}")
          case _ ⇒ true}
        case _ ⇒ false}}
    def fillDefValue(min:Double, max:Double, size:Option[Int]):Array[Double] = {
      Array.fill(size.get)((min + max) / 2)}
    def checkParameters(min:Double, max:Double, value:Array[Double]):(Boolean,String) = {
      value.toList.zipWithIndex.map{
        case (v,_) if v >= min && v <= max ⇒ (true,"")
        case (v,i) ⇒ (false, s"  For i = $i (minimum($min) <= value($v) <= maximum($max)) is false")}
      .filter{case (f,_) ⇒ !f} match{
        case Nil ⇒ (true,"")
        case le ⇒ (false, "\n" + le.map{case (_,msg) ⇒ msg}.mkString("\n"))}}}
  //Check parameters
  if(defaultMin >= defaultMax ){throw new SyntaxException(s"""
    |Incorrect parameters for PotBoard with name ${helper.toolName}:
    | defaultMin($defaultMin) < defaultMax($defaultMax) is false
    |""".stripMargin)}
  //UI
  private val frame = new GridFrame(environment.layout, environment.params.PotBoard, helper.toolName){
     def closing() = gear.endWork()}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Get vars
      val doubleVars = doubleVarBoard.getVars
      val arrayVars = arrayVarBoard.getVars
      //Construct UI
      val doublePots = doubleVars.map(variable ⇒ {
        new Potentiometer(
          environment.params.PotBoard, variable.name, variable.min, variable.max, variable.value){
          def potValueChanged(v:Double) = {
            if(variable.changedFun.map(f ⇒ f(v)).getOrElse(false)){
              gear.needUpdate()}
            variable.field.setDouble(helper.thisTool,v)
            gear.changed()
            updated()}
          def getCurrentValue:Double = {
            variable.field.getDouble(helper.thisTool)}}})
      val arrayPots = arrayVars.flatMap(variable ⇒ {
        variable.value.zipWithIndex.map{case (value,index) ⇒ {
          new Potentiometer(
              environment.params.PotBoard, variable.name + s"_$index", variable.min, variable.max, value){
            val arr = variable.field.get(helper.thisTool).asInstanceOf[Array[Double]]
            def potValueChanged(v:Double) = {
              val makeUpdate = variable.changedFun.map(f ⇒ {
                val ca = arr.clone()
                ca(index) = v
                f(ca)})
              if(makeUpdate.getOrElse(false)){
                gear.needUpdate()}
              arr(index) = v
              gear.changed()
              updated()}
            def getCurrentValue:Double = {
              variable.field.get(helper.thisTool).asInstanceOf[Array[Double]](index)}}}}})
      //Add to frame
      frame.add(doublePots)
      frame.add(arrayPots)
      pots = doublePots ++ arrayPots
      //Show
      frame.show(screenX, screenY)}
    def update() = {
      pots.foreach(_.update())
      updated()}
    def stop() = {frame.hide()}}}
