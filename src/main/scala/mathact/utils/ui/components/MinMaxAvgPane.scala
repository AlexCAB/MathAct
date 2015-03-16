package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.{Swing, Label, FlowPanel}
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
  val Seq(minName, maxName, avgName) = Seq("min","max","avg").map(text ⇒ {
    new NameLabel(uiParams, text, uiParams.textColor)})
  val Seq(minVal, maxVal, avgVal) = (0 to 2).map(_ ⇒ new NumberLabel(uiParams, uiParams.textColor))
  val Seq(minSep, maxSep, avgSep) = (0 to 2).map(_ ⇒ new SeparatorLabel(uiParams, " = ", uiParams.textColor))
  val components = List(minName,minSep,minVal,  maxName,maxSep,maxVal, avgName,avgSep,avgVal)
  preferredSize = new Dimension(
    components.map(_.preferredSize.getWidth.toInt).sum + vGap * 9,
    components.map(_.preferredSize.getHeight.toInt).max + hGap * 2)
  border = Swing.LineBorder(uiParams.borderColor, uiParams.borderSize)
  contents ++= components
  //Methods
  def update(values:List[Double]):Unit = values match{
    case Nil ⇒ {
      List(minVal,maxVal,avgVal).foreach(_.setNumber(Double.NaN))}
    case l ⇒ {
      minVal.setNumber(values.min)
      maxVal.setNumber(values.max)
      avgVal.setNumber(values.sum / values.size)}}}