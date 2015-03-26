package mathact.utils.ui.components
import java.awt.Dimension
import javax.swing.{DefaultComboBoxModel, JComboBox}
import mathact.utils.ui.UIParams
import scala.swing.{Component, BorderPanel}
import scala.swing.event.SelectionChanged


/**
 * Drop down list base on ComboBox
 * Created by CAB on 26.03.2015.
 */

abstract class DropDownList (uiParams:UIParams.DropDownList, width:Int)
extends BorderPanel with UIComponent{
  //Construction
  preferredSize = new Dimension(width, uiParams.listHeight)
  private val comboBox = new JComboBox[String]
  comboBox.setFont(uiParams.listFont)
  layout(Component.wrap(comboBox)) = BorderPanel.Position.Center
  //Abstract methods
  def selected(item:String, index:Int)
  //Methods
  def setList(list:List[String]) = comboBox.setModel(new DefaultComboBoxModel[String](list.toArray))
  def setItem(index:Int) = comboBox.setSelectedIndex(index)
  //Reactions
  reactions += {
    case SelectionChanged(_) ⇒ selected(comboBox.getSelectedIndex.asInstanceOf[String], comboBox.getSelectedIndex)}}
