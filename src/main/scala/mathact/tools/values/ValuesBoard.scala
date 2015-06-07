package mathact.tools.values
import java.awt.Color
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
  //Variables
  private var values = List[Value]()
  private var measurers:List[(Measurer, ()⇒Double)] = List()
  //Classes
  protected case class Value(name:Option[String], color:Color, proc:()⇒Double){
    def of(value: ⇒Double):Unit = {values :+= Value(name, color, ()⇒value)}}
  //DSL Methods
  def value(name:String = "", color:Color = new Color(0,0,0)):Value =
    Value(name match{case s if s == "" ⇒ None; case s ⇒ Some(s)}, color, () ⇒ 0.0)
  def updated() = {}
  //Helpers
  private val helper = new ToolHelper(this, name, "ValuesBoard")
  //UI
  private val frame = new GridFrame(environment.layout, environment.params.ValuesBoard, helper.toolName){
    def closing() = gear.endWork()}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Crate measurers
      measurers = values.zipWithIndex
        .map{case (Value(Some(n), c, p), _) ⇒ (n, c, p); case (Value(None, c, p), i) ⇒ ("L" + i, c, p)}
        .map{case(name, color, proc) ⇒ (new Measurer(environment.params.ValuesBoard, name, color), proc)}
      frame.add(measurers.map(_._1))
      //Show
      frame.show(screenX, screenY)}
    def update() = {
      measurers.foreach{case (m,p) ⇒ m.update(p())}
      updated()}
    def stop() = {
      frame.hide()}}}
