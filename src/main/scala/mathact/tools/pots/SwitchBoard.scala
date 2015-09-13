package mathact.tools.pots
import java.lang.reflect.Field
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.ui.components.{Switch, GridFrame}
import mathact.utils.{ToolHelper, Tool, Environment}
import mathact.utils.dsl.SyntaxException
import scala.language.implicitConversions
import scala.reflect.{ClassTag,classTag}


/**
 * Switch board for selecting of several values.
 * Created by CAB on 12.09.2015.
 */

abstract class SwitchBoard(
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
(implicit environment:Environment)
extends Tool{
  //Private definition
  private abstract class SwitchData{val changed:()⇒Boolean; val field:Field; val ui:Switch}
  private case class SwitchBool(
    init:Boolean, changed:()⇒Boolean = ()⇒false, field:Field = null, ui:Switch = null)
    extends SwitchData
  private case class SwitchInt(
    init:Int, vs:List[Int] = List(), changed:()⇒Boolean = ()⇒false, field:Field = null, ui:Switch = null)
    extends SwitchData
  private case class SwitchDouble(
    init:Double, vs:List[Double] = List(), changed:()⇒Boolean = ()⇒false, field:Field = null, ui:Switch = null)
    extends SwitchData
  private case class SwitchString(
    init:String, vs:List[String] = List(), changed:()⇒Boolean = ()⇒false, field:Field = null, ui:Switch = null)
    extends SwitchData
  //Variables
  private var switchs = List[SwitchData]()
  //Functions
  private def updateSwitch[T <: SwitchData : ClassTag](up:(T)⇒SwitchData):Unit = {
    switchs = switchs match{
      case s :: t if classTag[T].runtimeClass.isInstance(s) ⇒ up(s.asInstanceOf[T]) :: t
      case _ ⇒ throw new SyntaxException("Error: Incorrect operators sequence, 'init(<value>)' must be called first.")}}
  private def checkRange(begin:Double, end:Double, step:Double):Unit = {
    if(begin > end || step == 0){
      throw new SyntaxException(
        s"Error: Incorrect range must be begin($begin) <= end($end) and step($step) != 0.")}
    if((begin to end by step).size < 2){
      throw new SyntaxException(
        s"Error: Incorrect range size must be (begin($begin) to end($end) by step($step)).size >= 2.")}}
  private def checkOptions[T](ops:Seq[T], v:T):Unit = {
    if(! ops.contains(v)){
      throw new SyntaxException(
        s"Error: Options(${ops.toList}) must contain init value ($v).")}
    if(ops.size < 2){
      throw new SyntaxException(
        s"Error: Incorrect number of options: (${ops.size}) must be >= 2.")}}
  private def checkOnStart():Unit = {
    val existsEmpty = switchs.exists{
      case _:SwitchBool  ⇒ false
      case s:SwitchInt ⇒ s.vs.isEmpty
      case s:SwitchDouble ⇒ s.vs.isEmpty
      case s:SwitchString ⇒ s.vs.isEmpty}
    if(existsEmpty){
      throw new SyntaxException(
        "Error: All defined vars and vals must have a not empty list of options.")}}
  private def setFields():Unit = {
    val typeNames = List("boolean", "int", "double", "java.lang.String")
    val fields = getClass.getDeclaredFields
      .filter(f ⇒ typeNames.contains(f.getType.getName))
      .map(f ⇒ {f.setAccessible(true); f})
      .reverse
    switchs = switchs.zip(fields).map{
      case (s:SwitchBool,f) if f.getType.getName == typeNames(0) ⇒ SwitchBool(s.init, s.changed, f)
      case (s:SwitchInt,f) if f.getType.getName == typeNames(1) ⇒ SwitchInt(s.init, s.vs, s.changed, f)
      case (s:SwitchDouble,f) if f.getType.getName == typeNames(2) ⇒ SwitchDouble(s.init, s.vs, s.changed, f)
      case (s:SwitchString,f) if f.getType.getName == typeNames(3) ⇒ SwitchString(s.init, s.vs, s.changed, f)
      case (s,f) ⇒ throw new SyntaxException(s"Error:  Not match of field type ($f) and switch type ($s).")}}
  private def buildSwitches():Unit = {
    val params = environment.params.SwitchBoard
    switchs = switchs.map{
      case s:SwitchBool  ⇒ {
        val ui = new Switch(params, s.field.getName, List("False", "True"), s.init match{case true ⇒ 1; case _ ⇒ 0}){
          def getCurrentIndex: Int = {
            s.field.getBoolean(helper.thisTool) match{case true ⇒ 1; case _ ⇒ 0}}
          def switchIndexChanged(i: Int): Unit = {
            if(s.changed()){gear.needUpdate()}
            s.field.setBoolean(helper.thisTool, i match{case 1 ⇒ true; case _ ⇒ false})
            gear.changed()
            updated()}}
        SwitchBool(s.init, s.changed, s.field, ui)}
      case s:SwitchInt ⇒ {
        val ui = new Switch(params, s.field.getName, s.vs.map(_.toString), s.vs.indexOf(s.init)){
          def getCurrentIndex: Int = {


            //          def potValueChanged(v:Double) = {
            //            if(variable.changedFun.map(f ⇒ f(v)).getOrElse(false)){
            //              gear.needUpdate()}
            //            variable.field.setDouble(helper.thisTool,v)
            //            gear.changed()
            //            updated()}
            //          def getCurrentValue:Double = {
            //            variable.field.getDouble(helper.thisTool)}}})




            1}
          def switchIndexChanged(i: Int): Unit = {


          }}
        SwitchInt(s.init, s.vs ,s.changed, s.field, ui)}
      case s:SwitchDouble ⇒ {
        val ui = new Switch(params, s.field.getName, s.vs.map(_.toString),  s.vs.indexOf(s.init)){
          def getCurrentIndex: Int = {


            1}
          def switchIndexChanged(i: Int): Unit = {


          }}
        SwitchDouble(s.init, s.vs, s.changed, s.field, ui)}
      case s:SwitchString ⇒ {
        val ui = new Switch(params, s.field.getName, s.vs.map(_.toString),  s.vs.indexOf(s.init)){
          def getCurrentIndex: Int = {


            1}
          def switchIndexChanged(i: Int): Unit = {


          }}
        SwitchString(s.init, s.vs, s.changed, s.field, ui)}}}
  //DSL
  protected def init(v:Boolean):Boolean = {switchs +:= new SwitchBool(v); v}
  protected def init(v:Int):Int = {switchs +:= new SwitchInt(v); v}
  protected def init(v:Double):Double = {switchs +:= new SwitchDouble(v); v}
  protected def init(v:String):String = {switchs +:= new SwitchString(v); v}
  protected implicit class SwitchBoolMethods(v:Boolean){
    def changed(f: ⇒Unit):Boolean = {updateSwitch[SwitchBool]((s)⇒{SwitchBool(s.init,()⇒{f ;false})}); v}
    def changedWithUpdate(f: ⇒Unit):Boolean = {updateSwitch[SwitchBool]((s)⇒{SwitchBool(s.init,()⇒{f ;true})}); v}}
  protected implicit class SwitchIntMethods(v:Int){
    def in(begin:Int, end:Int, step:Int = 1):Int = {
      checkRange(begin, end, step)
      updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, (begin to end by step).toList, s.changed)}); v}
    def options(op1:Int, opn:Int*):Int = {
      checkOptions(op1 +: opn, v)
      updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, (op1 +: opn).toList, s.changed)}); v}
    def options(l:List[Int]):Int = {
      checkOptions(l, v)
      updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, l, s.changed)}); v}
    def changed(f: ⇒Unit):Int = {
      updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, s.vs, ()⇒{f ;false})}); v}
    def changedWithUpdate(f: ⇒Unit):Int = {
      updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, s.vs, ()⇒{f ;true})}); v}}
  protected implicit class SwitchDoubleMethods(v:Double){
    def in(begin:Double, end:Double, step:Double = 1):Double = {
      checkRange(begin, end, step)
      updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, (begin to end by step).toList, s.changed)}); v}
    def options(op1:Double, opn:Double*):Double = {
      checkOptions(op1 +: opn, v)
      updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, (op1 +: opn).toList, s.changed)}); v}
    def options(l:List[Double]):Double = {
      checkOptions(l, v)
      updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, l, s.changed)}); v}
    def changed(f: ⇒Unit):Double = {
      updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, s.vs, ()⇒{f ;false})}); v}
    def changedWithUpdate(f: ⇒Unit):Double = {
      updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, s.vs, ()⇒{f ;true})}); v}}
  protected implicit class SwitchStringMethods(v:String){
    def options(op1:String, opn:String*):String = {
      checkOptions(op1 +: opn, v)
      updateSwitch[SwitchString]((s)⇒{SwitchString(s.init, (op1 +: opn).toList, s.changed)}); v}
    def options(l:List[String]):String = {
      checkOptions(l, v)
      updateSwitch[SwitchString]((s)⇒{SwitchString(s.init, l, s.changed)}); v}
    def changed(f: ⇒Unit):String = {
      updateSwitch[SwitchString]((s)⇒{SwitchString(s.init, s.vs, ()⇒{f ;false})}); v}
    def changedWithUpdate(f: ⇒Unit):String = {
      updateSwitch[SwitchString]((s)⇒{SwitchString(s.init, s.vs, ()⇒{f ;true})}); v}}
  protected def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "SwitchBoard")
  //UI
  private val frame = new GridFrame(environment.layout, environment.params.SwitchBoard, helper.toolName){
    def closing() = gear.endWork()}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      checkOnStart()
      setFields()
      buildSwitches()
      frame.add(switchs.map(_.ui).reverse)
      //Show
      frame.show(screenX, screenY)}
    def update() = {
      switchs.foreach(_.ui.update())
      updated()}
    def stop() = {frame.hide()}}}


