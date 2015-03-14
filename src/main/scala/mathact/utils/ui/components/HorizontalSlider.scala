package mathact.utils.ui.components
import java.awt.Dimension
import javax.swing.{JSlider, SwingConstants}
import javax.swing.event.{ChangeEvent, ChangeListener}
import scala.swing.{Component, BorderPanel}


/**
 * HorizontalSlider UI component
 * Created by CAB on 10.03.2015.
 */

abstract class HorizontalSlider(
  min:Double,
  max:Double,
  init:Double,
  sliderWidth:Int,
  sliderHeight:Int,
  sliderScale:Double = 1000)
extends BorderPanel with UIComponent{
  //Variables
  private var callChanged = true
  //Construction
  val slider = new JSlider
//  val initWidth = sliderWidth
//  val initHeight = sliderHeight
  preferredSize = new Dimension(sliderWidth, sliderHeight)
  slider.setOrientation(SwingConstants.HORIZONTAL)
  slider.setMinimum((min * sliderScale).toInt)
  slider.setMaximum((max * sliderScale).toInt)
  slider.setValue((init * sliderScale).toInt)
//  slider.setPreferredSize(new Dimension(sliderWidth, initHeight))
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
    callChanged = true}}
