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

class MinMaxAvgPane(uiParams:UIParams.MinMaxAvgPane) extends FlowPanel(Left)() with UIComponent{
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
  val Seq(minSep, maxSep, avgSep) = (0 to 2).map(_ ⇒ new Label with UIComponent{
    text = " = "
    font = uiParams.separatorFont
    preferredSize = new Dimension(
      calcStringWidth(text, uiParams.separatorFont),
      uiParams.separatorHeight)
    font = uiParams.nameFont
    horizontalAlignment = Center})
  val components = List(minName,minSep,minVal,  maxName,maxSep,maxVal, avgName,avgSep,avgVal)
  preferredSize = new Dimension(
    components.map(_.preferredSize.getWidth.toInt).sum + vGap * 9,
    components.map(_.preferredSize.getHeight.toInt).max + hGap * 2)
  contents ++= components
  //Methods
  def update(values:List[Double]):Unit = values match{
    case Nil ⇒ {
      List(minVal,maxVal,avgVal).foreach(_.setNumber(Double.NaN))}
    case l ⇒ {
      minVal.setNumber(values.min)
      maxVal.setNumber(values.max)
      avgVal.setNumber(values.sum / values.size)}}}