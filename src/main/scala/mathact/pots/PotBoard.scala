package mathact.pots
import java.awt.Font
import mathact.clockwork._
import javax.swing.JSlider
import javax.swing.SwingConstants._
import javax.swing.event.{ChangeEvent, ChangeListener}
import mathact.clockwork.dsl.{VariableBoard, SyntaxException, Variable}
import mathact.clockwork.ui.OneColumnFrame
import scala.swing._
import Alignment._
import java.lang.reflect.Modifier
import java.lang.reflect.Field


/**
 * Interactive potentiometer board
 * Created by CAB on 08.03.2015.
 */

abstract class PotBoard
(x:Int = Int.MaxValue, y:Int = Int.MaxValue, defMin:Double = -1, defMax:Double = 1)
(implicit clockwork:Clockwork) {
  //Parameters
  private val nameFont = new Font(Font.SERIF, Font.BOLD, 14)
  private val minMaxFont = new Font(Font.SERIF, Font.BOLD, 14)
  private val valueFont = new Font(Font.SERIF, Font.BOLD, 14)
  //Classes
  private class Pot(
    varName:String, min:Double, max:Double, value:Double,
    nameWidth:Int, minMaxWidth:Int, valWidth:Int,
    valueChanged:(Double)⇒Unit)
  extends FlowPanel{
    //Construction
    contents += new Label {
      font = nameFont
      horizontalAlignment = Right
      text = varName
      preferredSize = new Dimension(nameWidth, 20)}
    contents += new Label {
      font = minMaxFont
      text = "[" + min + "," + max + "]"
      preferredSize = new Dimension(minMaxWidth + 25, 20)}




//
//        val l = new Label {
//          text = p.value.toString
//          preferredSize = new Dimension(50, 20)}
//        contents += l
//
//
//        val s = new JSlider(HORIZONTAL, (p.min * sliderScale).toInt, (p.max * sliderScale).toInt, (p.value * sliderScale).toInt)
//        s.addChangeListener(new ChangeListener {
//          def stateChanged(e: ChangeEvent) = {
//            p.value = s.getValue.toDouble / sliderScale
//            l.text = p.value.toString
//            p.up(p.value)}})
//        contents += Component.wrap(s)


//        //Methods
//        def update(v:Double) = {
//          s.setValue((v * sliderScale).toInt)}}
//      (p.name,pp)})







  }
  //Helpers
  private val thisPotBoard = this
  private val doubleVarBoard = new VariableBoard[Double](thisPotBoard, defMin, defMax){
    def createIdValue(id:Int):Double = id.toDouble
    def getId(value:Double):Int = value.toInt
    def checkField(f:Field):Boolean = {f.getType.getName == "double"}
    def fillDefValue(min:Double, max:Double, size:Option[Int]):Double = {(max + max) / 2}}
  private val arrayVarBoard = new VariableBoard[Array[Double]](thisPotBoard, defMin, defMax){
    def createIdValue(id:Int):Array[Double] = Array[Double](id)
    def getId(value:Array[Double]):Int = value(0).toInt
    def checkField(f:Field):Boolean = {
      f.getType.getName match{
        case "[D" ⇒ Modifier.isFinal(f.getModifiers) match{
          case false ⇒ throw new SyntaxException(
            s"Array definition should be 'val' in ${thisPotBoard.getClass.getCanonicalName}")
          case _ ⇒ true}
        case _ ⇒ false}}
    def fillDefValue(min:Double, max:Double, size:Option[Int]):Array[Double] = {Array.fill(size.get)((max + max) / 2)}}
  //UI
  private val frame = new OneColumnFrame(clockwork){
    title = thisPotBoard.getClass.getCanonicalName match{
      case null ⇒ "PotBoard"
      case n ⇒ n.split("[.]").last.replace("$","")}
    override def closeOperation() {if(gear.work){clockwork.delGear(gear)}}}
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
  def array:ArrayMethods = new ArrayMethods(0.0)
  implicit class DoubleEx(v:Double){
    def in(min: ⇒Double, max: ⇒Double):Double =
      doubleVarBoard.addNextVarParameter(Some(min), Some(max), None, None, doubleVarBoard.MinMax)
    def minimum(min: ⇒Double):Double =
      doubleVarBoard.addNextVarParameter(Some(min), None, None, None, doubleVarBoard.Minimum)
    def maximum(max: ⇒Double):Double =
      doubleVarBoard.addNextVarParameter(None, Some(max), None, None, doubleVarBoard.Maximum)}
  class ArrayMethods(defValue:Double){
    def len(n: ⇒Int):Array[Double] =
      arrayVarBoard.addVarParameter(None, None, None, Some(n), arrayVarBoard.Value)
    def of(v: ⇒Double):ArrayMethods = new ArrayMethods(v)}
  implicit class ArrayDoubleEx(vd:Array[Double]){
    def in(min: ⇒Double, max: ⇒Double):Array[Double] =
      arrayVarBoard.addNextVarParameter(Some(min), Some(max), None, None, arrayVarBoard.MinMax)
    def minimum(min: ⇒Double):Array[Double] =
      arrayVarBoard.addNextVarParameter(Some(min), None, None, None, arrayVarBoard.Minimum)
    def maximum(max: ⇒Double):Array[Double] =
      arrayVarBoard.addNextVarParameter(None ,Some(max), None, None, arrayVarBoard.Maximum)}
  //Gear
  private val gear:Gear = new Gear{
    def start() = {
      //Get vars
      val doubleVars = doubleVarBoard.getVars
      val arrayVars = arrayVarBoard.getVars
      //Calculation of UI metrics
      val nameColWidth = List(
        clockwork.layout.calcStringColumnWidth(doubleVars.map(_.name), nameFont),
        clockwork.layout.calcStringColumnWidth(arrayVars.map(_.name), nameFont) + 20 ).max // + index
      val minColWidth = clockwork.layout.calcDoubleColumnWidth(
        doubleVars.map(_.min) ++ arrayVars.map(_.min),
        minMaxFont)
      val maxColWidth = clockwork.layout.calcDoubleColumnWidth(
        doubleVars.map(_.max) ++ arrayVars.map(_.max),
        minMaxFont)
      val valueColWidth = clockwork.layout.calcDoubleColumnWidth(
        doubleVars.map(_.value) ++ arrayVars.map(_.value.toList).fold(List[Double]())((a,b)⇒ a ++ b),
        valueFont)
      //Construct UI
      println(nameColWidth,minColWidth,maxColWidth,valueColWidth)
      val doublePots = doubleVars.map(variable ⇒ {
        new Pot(
          variable.name, variable.min, variable.max, variable.value,
          nameColWidth, minColWidth + maxColWidth, valueColWidth,
          valueChanged = (v)⇒{



          })

      })



      val arrayPots = arrayVars.flatMap(variable ⇒ {
        variable.value.zipWithIndex.map{case (value,index) ⇒ {
          new Pot(
            variable.name + s"_$index", variable.min, variable.max, value,
            nameColWidth, minColWidth + maxColWidth, valueColWidth,
            valueChanged = (v)⇒{



            })



        }}






      })





      frame.add(doublePots)
      frame.add(arrayPots)
      //Show
      frame.show(x,y)}
    def tick() = {




    }
    def stop() = {frame.hide()}}
  clockwork.addGear(gear)}
