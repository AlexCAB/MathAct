package mathact.clockwork.ui.components
import java.awt.Dimension
import javax.swing.{JSlider, SwingConstants}
import javax.swing.event.{ChangeEvent, ChangeListener}
import mathact.clockwork.Clockwork
import mathact.clockwork.ui.Alignment


/**
 * HorizontalSlider UI component
 * Created by CAB on 10.03.2015.
 */

abstract class HorizontalSlider
(clockwork:Clockwork, min:Double, max:Double, init:Double, sliderWidth:Int, sliderScale:Double = 1000)
extends JSlider with Alignment{
  //Variables
  private var callChanged = true
  //Construction
  val initWidth = sliderWidth
  val initHeight = clockwork.skin.horizontalSliderHeight
  setOrientation(SwingConstants.HORIZONTAL)
  setMinimum((min * sliderScale).toInt)
  setMaximum((max * sliderScale).toInt)
  setValue((init * sliderScale).toInt)
  setPreferredSize(new Dimension(sliderWidth, initHeight))
  //Listeners
  addChangeListener(new ChangeListener {def stateChanged(e: ChangeEvent) = {
    if(callChanged){valueChanged(getValue.toDouble / sliderScale)}}})
  //Abstract methods
  def valueChanged(v:Double)
  //Methods
  def getCurrentValue:Double = getValue / sliderScale
  def setCurrentValue(v:Double) = {
    callChanged = false
    setValue((v * sliderScale).toInt)
    callChanged = true}
  def setNewSize(w:Int,h:Int):Unit = {setPreferredSize(new Dimension(w, h))}}
