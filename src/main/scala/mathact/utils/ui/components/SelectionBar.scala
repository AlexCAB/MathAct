package mathact.utils.ui.components
import java.awt.{Dimension, Color}
import mathact.utils.ui.UIParams
import scala.swing.{Alignment, GridPanel, Swing, FlowPanel}
import scala.swing.FlowPanel.Alignment._


/**
 * Selection component: < Name > : < DropDownList >
 * Created by CAB on 26.03.2015.
 */

abstract class SelectionBar (uiParams:UIParams.SelectionBar, name:String, listInitWidth:Int)
extends FlowPanel(Left)() with UIComponent{
  //Parameters
  hGap = 1
  vGap = 1
  //Construction
  private val listCmp = new DropDownList(uiParams, listInitWidth){
    def selected(item:String, index:Int) = selected(item, index)}
  private val components = List(
    new NameLabel(uiParams, name, uiParams.textColor, Alignment.Left),
    listCmp)
  private val width = components.map(_.preferredSize.getWidth).sum + hGap * 3
  private val height = components.map(_.preferredSize.getHeight).max + vGap * 2
  preferredSize = new Dimension(width.toInt, height.toInt)
  contents ++= components
  background = uiParams.backgroundColor
  //Abstract methods
  def selected(item:String, index:Int)
  //Methods
  def setList(list:List[String]) = listCmp.setList(list)
  def setItem(index:Int) = listCmp.setItem(index)}
