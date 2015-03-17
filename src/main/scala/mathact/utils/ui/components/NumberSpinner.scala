package mathact.utils.ui.components
import java.awt.Dimension
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.{JSpinner, SpinnerNumberModel}
import mathact.utils.ui.UIParams
import scala.swing.{Component, BorderPanel}


/**
 * Spinner for double numbers
 * Created by CAB on 10.03.2015.
 */

abstract class NumberSpinner(uiParams:UIParams.NumberSpinner, min:Double, max:Double, init:Double, step:Double = .1)
extends BorderPanel with UIComponent{
  //Variables
  private var callChanged = true
  //Construction
  val spinner = new JSpinner
  preferredSize = new Dimension({
      val w = calcDoubleWidth(init, uiParams.valueFont)
      if(w < 100) 100 else w},
    uiParams.valueHeight)
  val model = new SpinnerNumberModel(init, min, max, step)
  spinner.setModel(model)
  spinner.setFont(uiParams.valueFont)
  spinner.setBackground(uiParams.backgroundColor)
  spinner.setOpaque(true)
  layout(Component.wrap(spinner)) = BorderPanel.Position.Center
  //Listeners
  model.addChangeListener(new ChangeListener{def stateChanged(e:ChangeEvent) = {
    if(callChanged){valueChanged(model.getValue.asInstanceOf[Double])}}})
  //Abstract methods
  def valueChanged(v:Double)
  //Methods
  def getCurrentValue:Double = model.getValue.asInstanceOf[Double]
  def setCurrentValue(v:Double) = {
    callChanged = false
    model.setValue(v)
    callChanged = true}}



