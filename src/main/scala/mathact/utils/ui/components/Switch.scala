package mathact.utils.ui.components
import mathact.utils.clockwork.ExecutionException
import mathact.utils.ui.UIParams

/**
 *
 * Created by CAB on 13.09.2015.
 */

abstract class Switch(
  uiParams:UIParams.Switch,
  varName:String,
  options:List[String],
  initIndex:Int)
extends GridComponent {
  //Variables
  private var index = initIndex
  //Components
  private val nameView = new NameLabel(uiParams, varName)
  private val sliderBar:DiscreteHorizontalSlider = new DiscreteHorizontalSlider(uiParams, options, initIndex){
    def indexChanged(i:Int):Unit = if(index != i){
      index = i
      switchIndexChanged(i)}}
  val gridRow = List(nameView, sliderBar)
  //Abstract methods
  def switchIndexChanged(i:Int)
  def getCurrentIndex:Int
  //Methods
  def setCurrentIndex(i:Int):Unit = if(index != i){
    index = i
    sliderBar.setCurrentIndex(i)}
  def update() = setCurrentIndex(getCurrentIndex)}