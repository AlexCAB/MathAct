package mathact.clockwork.ui.components
import java.awt.Dimension
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.{JSpinner, SpinnerNumberModel}
import mathact.clockwork.Clockwork
import mathact.clockwork.ui.Alignment


/**
 * Spinner for double numbers
 * Created by CAB on 10.03.2015.
 */

abstract class NumberSpinner(clockwork:Clockwork, min:Double, max:Double, init:Double, step:Double = .1)
extends JSpinner with Alignment{
  //Variables
  private var callChanged = true
  //Construction
  val initWidth:Int = {
    val w = clockwork.layout.calcDoubleWidth(init, clockwork.skin.valueFont)
    if(w < 30) 30 else w}
  val initHeight = clockwork.skin.valueHeight
  val model = new SpinnerNumberModel(init, min, max, step)
  setModel(model)
  setPreferredSize(new Dimension(initWidth, initHeight))
  setFont(clockwork.skin.valueFont)
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
    callChanged = true}
  def setNewSize(w:Int,h:Int):Unit = {setPreferredSize(new Dimension(w, h))}}



