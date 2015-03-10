package mathact.clockwork.ui.components
import mathact.clockwork.{ExecutionException, Clockwork}
import mathact.clockwork.ui.Alignment
import scala.swing.{Component, FlowPanel, BorderPanel}


/**
 * Potentiometer UI
 * Created by CAB on 10.03.2015.
 */

abstract class Potentiometer(clockwork:Clockwork, varName:String, min:Double, max:Double, value:Double)
extends GridComponent {
  //Components
  val nameView = new NameLabel(clockwork, varName)
  val diapasonView = new DiapasonLabel(clockwork, min, max)
  val sliderBar:HorizontalSlider = new HorizontalSlider(clockwork, min, max, value, clockwork.skin.potSliderWidth){
    def valueChanged(v:Double) = {
      editBar.setCurrentValue(v)
      potValueChanged(v)}}
  val editBar:NumberSpinner = new NumberSpinner(clockwork, min, max, value){
    def valueChanged(v:Double) = {
      sliderBar.setCurrentValue(v)
      potValueChanged(v)}}
  val gridRow = List(
    (nameView,nameView),
    (diapasonView,diapasonView),
    (Component.wrap(sliderBar),sliderBar),
    (Component.wrap(editBar),editBar))
  //Abstract methods
  def potValueChanged(v:Double)
  //Methods
  def getCurrentValue:Double = editBar.getCurrentValue
  def setCurrentValue(v:Double) = {
    //Check bounds
    if(v > max || v < min){
      throw new ExecutionException(s"(minimum($min) <= value($v) <= maximum($max)) is false, on var $varName.")}
    //Set new value
    sliderBar.setCurrentValue(v)
    editBar.setCurrentValue(v)}}
