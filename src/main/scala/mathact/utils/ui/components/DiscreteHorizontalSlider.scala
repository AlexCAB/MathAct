package mathact.utils.ui.components
import java.awt.Dimension
import java.util.Hashtable
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.{JLabel, SwingConstants, JSlider}
import mathact.utils.ui.UIParams
import scala.swing.{Component, BorderPanel}



/**
 * Horizontal slider with number of fix options.
 * Created by CAB on 13.09.2015.
 */

abstract class DiscreteHorizontalSlider(uiParam:UIParams.DiscreteHorizontalSlider, options:List[String], initIndex:Int)
extends BorderPanel with UIComponent{
  //Variables
  private var callChanged = true
  //Set size
  private val width =
    options.map(e ⇒ calcStringWidth(e, uiParam.discreteSliderFont)).max * options.size  + (options.size - 1) * 2
  preferredSize = new Dimension(width, uiParam.discreteSliderHeight)
  //Build slider
  private val slider = new JSlider
  slider.setOrientation(SwingConstants.HORIZONTAL)
  slider.setFocusable(false)
  slider.setMinimum(0)
  slider.setMaximum(options.size - 1)
  slider.setValue(initIndex)
  slider.setMinorTickSpacing(1)
  slider.setPaintTicks(true)
  private val labels = new Hashtable[Int, JLabel]
  options.zipWithIndex.foreach{case (o,i) ⇒ {
    val jl = new JLabel(o)
    jl.setFont(uiParam.discreteSliderFont)
    jl.setForeground(uiParam.discreteSliderTextColor)
    labels.put(i, jl)}}
  slider.setFont(uiParam.discreteSliderFont)
  slider.setLabelTable(labels)
  slider.setPaintLabels(true)
  slider.setBackground(uiParam.backgroundColor)
  layout(Component.wrap(slider)) = BorderPanel.Position.Center
  //Listeners
  slider.addChangeListener(new ChangeListener {def stateChanged(e: ChangeEvent) = {
    if(callChanged){indexChanged(slider.getValue)}}})
  //Abstract methods
  def indexChanged(i:Int):Unit
  //Methods
  def getCurrentIndex:Int = slider.getValue
  def setCurrentIndex(i:Int):Unit = {
    callChanged = false
    slider.setValue(i)
    callChanged = true}}