package mathact.utils.ui.components
import java.awt.Dimension
import javax.swing.{JSlider, SwingConstants}
import javax.swing.event.{ChangeEvent, ChangeListener}
import mathact.utils.Environment
import mathact.utils.ui.Alignment
import scala.swing.{Component, BorderPanel}


/**
 * HorizontalSlider UI component
 * Created by CAB on 10.03.2015.
 */

abstract class HorizontalSlider
(environment:Environment, min:Double, max:Double, init:Double, sliderWidth:Int, sliderScale:Double = 1000)
extends BorderPanel with Alignment{
  //Variables
  private var callChanged = true
  //Construction
  val slider = new JSlider
  val initWidth = sliderWidth
  val initHeight = environment.skin.horizontalSliderHeight
  slider.setOrientation(SwingConstants.HORIZONTAL)
  slider.setMinimum((min * sliderScale).toInt)
  slider.setMaximum((max * sliderScale).toInt)
  slider.setValue((init * sliderScale).toInt)
  slider.setPreferredSize(new Dimension(sliderWidth, initHeight))
  layout(Component.wrap(slider)) = BorderPanel.Position.Center
  //Listeners
  slider.addChangeListener(new ChangeListener {def stateChanged(e: ChangeEvent) = {
    if(callChanged){valueChanged(slider.getValue.toDouble / sliderScale)}}})
  //Abstract methods
  def valueChanged(v:Double)
  //Methods
  def getCurrentValue:Double = slider.getValue / sliderScale
  def setCurrentValue(v:Double) = {
    callChanged = false
    slider.setValue((v * sliderScale).toInt)
    callChanged = true}
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}
