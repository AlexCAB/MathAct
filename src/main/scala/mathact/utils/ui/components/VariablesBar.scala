package mathact.utils.ui.components
import java.awt.{Dimension, Color}
import mathact.utils.ui.UIParams
import scala.swing.FlowPanel.Alignment.Left
import scala.swing.{Swing, Label, FlowPanel}


/**
 * Show variables values in line
 * Created by CAB on 16.03.2015.
 */

class VariablesBar(uiParams:UIParams.VariablesBar) extends FlowPanel(Left)() with UIComponent{
  //Parameters
  hGap = 1
  vGap = 1
  //Variables
  private var valueLabels = List[NumberLabel]()
  //Construction
  background = uiParams.backgroundColor
  border = Swing.LineBorder(uiParams.borderColor, uiParams.borderSize)
  //Methods
  def setVars(vars:List[(String,Color)]):Unit = {
    //Create labels
    val labels = vars.map { case (name, color) ⇒ (
      new NameLabel(uiParams, name, color),
      new SeparatorLabel(uiParams, " = ", color),
      new NumberLabel(uiParams, color))}
     //Calc size
    val width = labels.flatMap{case (n,s,v) ⇒ List(
      n.preferredSize.getWidth.toInt + vGap,
      s.preferredSize.getWidth.toInt + vGap,
      v.preferredSize.getWidth.toInt + vGap)}.sum + vGap * 2
    val height = labels.flatMap{case (n,s,v) ⇒ List(
      n.preferredSize.getHeight.toInt,
      s.preferredSize.getHeight.toInt,
      v.preferredSize.getHeight.toInt)}.max + hGap * 2
    preferredSize = new Dimension(width,height)
    //Add
    valueLabels =  labels.map{case (_,_,v) ⇒ v}
    contents ++= labels.flatMap{case (n,s,v) ⇒ List(n,s,v)}}
  def update(values:List[Double]):Unit = {
    values.zip(valueLabels).foreach{case(v,l) ⇒ l.setNumber(v)}}}
