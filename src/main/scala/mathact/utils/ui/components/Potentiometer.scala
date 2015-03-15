package mathact.utils.ui.components
import mathact.utils.Environment
import mathact.utils.clockwork.ExecutionException
import mathact.utils.ui.UIParams


/**
 * Potentiometer UI
 * Created by CAB on 10.03.2015.
 */

abstract class Potentiometer(
  environment:Environment,
  varName:String,
  min:Double,
  max:Double,
  value:Double,
  uiParams:UIParams.Potentiometer)
extends GridComponent {
  //Variables
  private var vlaue = 0.0
  //Components
  val nameView = new NameLabel(uiParams, varName)
  val diapasonView = new DiapasonLabel(uiParams, min, max)
  val sliderBar:HorizontalSlider = new HorizontalSlider(min, max, value, uiParams.sliderWidth, uiParams.sliderHeight){
    def valueChanged(v:Double) = {
      vlaue = v
      editBar.setCurrentValue(v)
      potValueChanged(v)}}
  val editBar:NumberSpinner = new NumberSpinner(uiParams, min, max, value){
    def valueChanged(v:Double) = {
      vlaue = v
      sliderBar.setCurrentValue(v)
      potValueChanged(v)}}
  val gridRow = List(nameView, diapasonView, sliderBar, editBar)
  //Abstract methods
  def potValueChanged(v:Double)
  def getCurrentValue:Double
  //Methods
  def setCurrentValue(v:Double) = {
    //Check bounds
    if(v > max || v < min){
      throw new ExecutionException(s"(minimum($min) <= value($v) <= maximum($max)) is false, on var $varName.")}
    //Set new value
    vlaue = v
    sliderBar.setCurrentValue(v)
    editBar.setCurrentValue(v)}
  def update() = {
    val nv = getCurrentValue
    if(nv != value){setCurrentValue(nv)}}}
