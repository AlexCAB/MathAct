package mathact.tools.values
import java.awt.Color
import java.lang.reflect.Field
import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.dsl.Colors
import mathact.utils.ui.components.{Measurer, GridFrame}
import mathact.utils.{ToolHelper, Tool, Environment}


/**
 * Simple value shower.
 * Created by CAB on 16.03.2015.
 */

class ValuesBoard(
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
(implicit environment:Environment)
  extends Tool with Colors{
  //Private definitions
  private case class Value(name:Option[String], color:Color, proc:()⇒String)
  //Variables
  private var values = List[Value]()
  private var measurers:List[(Measurer, Value)] = List()
  //Helpers
  private val uiParams = environment.params.ValuesBoard
  private val helper = new ToolHelper(this, name, "ValuesBoard")
  private val decimal = NumberFormat.getNumberInstance(Locale.ENGLISH).asInstanceOf[DecimalFormat]
  decimal.applyPattern(uiParams.numberFormat)
  //Classes
  protected class ValueDef(name:Option[String], color:Option[Color]){
    def ofBoolean(value: ⇒Boolean):Unit = {
      values :+= Value(name, color.getOrElse(uiParams.booleanFieldColor), ()⇒{value.toString})}
    def ofInt(value: ⇒Int):Unit = {
      values :+= Value(name, color.getOrElse(uiParams.intFieldColor), ()⇒{value.toString})}
    def ofDouble(value: ⇒Double):Unit = {
      values :+= Value(name, color.getOrElse(uiParams.doubleFieldColor), ()⇒{decimal.format(value)})}
    def of(value: ⇒Double):Unit = ofDouble(value)
    def ofString(value: ⇒String):Unit = {
      values :+= Value(name, color.getOrElse(uiParams.stringFieldColor), ()⇒{value})}}
  //DSL Methods
  protected def value(name:String = "", color:Color = null):ValueDef =
    new ValueDef(name match{case s if s == "" ⇒ None; case s ⇒ Some(s)}, Option(color))
  protected def updated() = {}
  //Get fields
  private val typeNames = List("boolean", "int", "double", "java.lang.String")
  getClass.getDeclaredFields
    .filter(f ⇒ typeNames.contains(f.getType.getName))
    .map(f ⇒ {f.setAccessible(true); f})
    .foreach{f ⇒
      val (color, proc) = f.getType.getName match{
        case tn if tn == typeNames(0) ⇒ (uiParams.booleanFieldColor, ()⇒{f.getBoolean(helper.thisTool).toString})
        case tn if tn == typeNames(1) ⇒ (uiParams.intFieldColor, ()⇒{f.getInt(helper.thisTool).toString})
        case tn if tn == typeNames(2) ⇒ (uiParams.doubleFieldColor, ()⇒{decimal.format(f.getDouble(helper.thisTool))})
        case tn if tn == typeNames(3) ⇒ (uiParams.stringFieldColor, ()⇒{f.get(helper.thisTool).asInstanceOf[String]})}
      values :+= Value(Some(f.getName), color, proc)}
  //UI
  private val frame = new GridFrame(environment.layout, uiParams, helper.toolName){
    def closing() = gear.endWork()}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Crate measurers
      measurers = values.zipWithIndex
        .map{case (v:Value, i) if v.name.isEmpty ⇒ Value(Some("L" + i), v.color, v.proc); case (v,_) ⇒ v} //Set name
        .map{case v ⇒ {
          (new Measurer(uiParams, v.name.get, v.color, v.proc()), v)}}
      frame.add(measurers.map(_._1))
      //Show
      frame.show(screenX, screenY)}
    def update() = {
      measurers.foreach{case (m,v) ⇒ m.update(v.proc())}
      updated()}
    def stop() = {
      frame.hide()}}}
