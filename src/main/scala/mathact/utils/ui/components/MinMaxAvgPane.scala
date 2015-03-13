package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.Label
import scala.swing.FlowPanel
import scala.swing.FlowPanel.Alignment.Left
import scala.swing.Alignment._


/**
 * Display min max and average
 * Created by CAB on 12.03.2015.
 */

class MinMaxAvgPane(uiParams:UIParams.MinMaxAvgPane) extends FlowPanel(Left)() with ToyComponent{
  //Parameters
  hGap = 1
  vGap = 1
  //Constructions
  val minName = new NameLabel(uiParams, "min")
  val maxName = new NameLabel(uiParams, "max")
  val avgName = new NameLabel(uiParams, "avg")
  val minVal = new NumberLabel(uiParams)
  val maxVal = new NumberLabel(uiParams)
  val avgVal = new NumberLabel(uiParams)
  val Seq(minSep, maxSep, avgSep) = (0 to 2).map(_ ⇒ new Label with ToyComponent{
    text = " = "
    font = uiParams.separatorFont
    val initWidth = calcStringWidth(text, uiParams.separatorFont)
    val initHeight = uiParams.separatorHeight
    font = uiParams.nameFont
    horizontalAlignment = Center
    preferredSize = new Dimension(initWidth, initHeight)
    //Methods
    def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}})
  val components = List(minName,minSep,minVal,  maxName,maxSep,maxVal, avgName,avgSep,avgVal)
  val initHeight = components.map(_.initHeight).max + hGap * 2
  val initWidth = components.map(_.initWidth).sum + vGap * 9
  components.foreach(c ⇒ c.setNewSize(c.initWidth, initHeight))
  contents ++= components
  //Methods
  def update(values:List[Double]):Unit = values match{
    case Nil ⇒ {
      List(minVal,maxVal,avgVal).foreach(_.setNumber(Double.NaN))}
    case l ⇒ {
      minVal.setNumber(values.min)
      maxVal.setNumber(values.max)
      avgVal.setNumber(values.sum / values.size)}}
  def setNewSize(w:Int,h:Int):Unit = {preferredSize = new Dimension(w, h)}}