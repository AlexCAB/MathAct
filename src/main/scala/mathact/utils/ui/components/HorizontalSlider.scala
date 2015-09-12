package mathact.utils.ui.components
import java.awt.Dimension
import javax.swing.{JSlider, SwingConstants}
import javax.swing.event.{ChangeEvent, ChangeListener}
import mathact.utils.ui.UIParams
import scala.swing.{Component, BorderPanel}


/**
 * HorizontalSlider UI component
 * Created by CAB on 10.03.2015.
 */

abstract class HorizontalSlider(
  uiParam:UIParams.HorizontalSlider,
  min:Double,
  max:Double,
  init:Double)
extends BorderPanel with UIComponent{
  //Variables
  private var callChanged = true
  //Construction
  val slider = new JSlider
  slider.setFocusable(false)
//  val initWidth = sliderWidth
//  val initHeight = sliderHeight
  preferredSize = new Dimension(uiParam.sliderWidth, uiParam.sliderHeight)
  slider.setOrientation(SwingConstants.HORIZONTAL)
  slider.setMinimum((min * uiParam.sliderScale).toInt)
  slider.setMaximum((max * uiParam.sliderScale).toInt)
  slider.setValue((init * uiParam.sliderScale).toInt)
  slider.setBackground(uiParam.backgroundColor)
//  slider.setPreferredSize(new Dimension(sliderWidth, initHeight))
  layout(Component.wrap(slider)) = BorderPanel.Position.Center
  //Listeners
  slider.addChangeListener(new ChangeListener {def stateChanged(e: ChangeEvent) = {
    if(callChanged){valueChanged(slider.getValue.toDouble / uiParam.sliderScale)}}})
  //Abstract methods
  def valueChanged(v:Double)
  //Methods
  def getCurrentValue:Double = slider.getValue / uiParam.sliderScale
  def setCurrentValue(v:Double) = {
    callChanged = false
    slider.setValue((v * uiParam.sliderScale).toInt)
    callChanged = true}}
