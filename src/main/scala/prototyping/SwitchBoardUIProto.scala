package prototyping
import java.awt.{Color, Font, Dimension}
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.{JLabel, SwingConstants, JSlider}
import java.util.Hashtable
import mathact.utils.ui.UIParams
import mathact.utils.ui.components.UIComponent
import scala.swing.{BorderPanel, Component, Panel, Frame}


/**
 * SwitchBoard prototyping.
 * Created by CAB on 13.09.2015.
 */

object SwitchBoardUIProto extends App{
  println("==== SwitchBoardUIProto ====")
  //
  object Params extends UIParams.DiscreteHorizontalSlider{
    val discreteSliderFont:Font = new Font(Font.SERIF, Font.ITALIC, 13)
    val discreteSliderTextColor:Color = Color.BLACK
    val discreteSliderHeight:Int = 50
    val backgroundColor:Color = Color.WHITE}
  //
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
    def indexChanged(i:Int)
    //Methods
    def getCurrentIndex:Int = slider.getValue
    def setCurrentIndex(v:Int) = {
      callChanged = false
      slider.setValue(v)
      callChanged = true}}
  //
  class MyFrame extends Frame{
    //
    val switch = new DiscreteHorizontalSlider(Params, List("one","two","three","four","five"), 1) {
      def indexChanged(i:Int) = {
        println("index = " + i)
      }
    }
    //
    contents = switch
    override def closeOperation() = System.exit(0)
    //
  }
  val m = new MyFrame
  m.visible = true
}
