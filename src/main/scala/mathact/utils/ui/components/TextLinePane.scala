package mathact.utils.ui.components
import java.awt.{Color, Dimension, Font}
import javax.swing.{SwingUtilities, JTextPane}
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.text.{StyleConstants, StyleContext, DefaultStyledDocument}
import mathact.utils.ui.UIParams
import scala.swing.{BorderPanel,Component,ScrollPane}
import javax.swing.text.Style
import scala.collection.mutable.{Map ⇒ MutMap}


/**
 * Not editable text pane of lines
 * Created by CAB on 15.03.2015.
 */

class TextLinePane(
  uiParams:UIParams.TextLinePane,
  maxSize:Int,            
  width:Int,
  height:Int)
extends BorderPanel with UIComponent{
  //Variables
  private val stiles = MutMap[Color, Style]()
  //Construction
  preferredSize = new Dimension(width, height)
  private val textPane = new JTextPane
  textPane.setEditable(false)
  textPane.setBackground(uiParams.textBackgroundColor)
  textPane.setFont(uiParams.textFont)
  private val document = textPane.getStyledDocument
  //First line deleting
  private val remover = new Runnable() {
    override def run() = {
      val root = document.getDefaultRootElement
      if(root.getElementCount > maxSize){
        val end = root.getElement(0).getEndOffset
        document.remove(0, end)}}}
  document.addDocumentListener(new DocumentListener{
    def insertUpdate(e:DocumentEvent) = {
      SwingUtilities.invokeLater(remover)}
    def removeUpdate(e:DocumentEvent) = {}
    def changedUpdate(e:DocumentEvent) = {}})
  //Add
  layout(new ScrollPane{contents = Component.wrap(textPane)}) = BorderPanel.Position.Center
  //Methods
  def addLine(text:String, color:Color):Unit = {
    //Get or create stile
    val style = stiles.getOrElse(color, {
      val s = textPane.addStyle("s1", null)
      StyleConstants.setForeground(s, color)
      stiles += color → s
      s})
    //Add text
    document.insertString(document.getLength, text + "\n", style)}}
