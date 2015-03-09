package mathact.clockwork.ui
import java.awt.{Toolkit,Canvas,Rectangle,Font}
import scala.collection.mutable.{ListBuffer => MutList}
import scala.swing.{Dimension, Point}


/**
 * Component layout algorithm
 * Created by CAB on 08.03.2015.
 */

class Layout(x:Int, y:Int, width:Int, height:Int) {
  //Parameters
  private val screenSize = {
    val monitorSize = Toolkit.getDefaultToolkit.getScreenSize
    new Dimension(
      if(width == Int.MaxValue){monitorSize.width - x}else{width},
      if(height == Int.MaxValue){monitorSize.height - y}else{height})}
  //Helpers
  private val canvas = new Canvas()
  //Variables
  private val layouts = MutList[MutList[Rectangle]](MutList[Rectangle]())
  //Functions
  private def findFreeLayout(size:Dimension):(Int,Point) = {  //Return: layer number, Point
    val lastLayer = layouts(layouts.size - 1)
    //If first component it the layer then return, else find free space
    if(lastLayer.isEmpty){
      (layouts.size - 1, new Point(x, y))}
    else{
      //Search new position
      val (mw,mh) = (screenSize.width - size.width, screenSize.height - size.height)
      var found = false
      var (cx,cy) = (x - 10, 0)
      val searchRect = new Rectangle(cx, cy, size.width, size.height)
      while(! found && cx < mw){
        cx += 10
        cy = y - 10
        while(! found && cy < mh){
          cy += 10
          searchRect.setLocation(cx, cy)
          found = ! lastLayer.exists(_.intersects(searchRect))}}
      //Return if found or new layer if not
      if(found){
        (layouts.size - 1, new Point(cx, cy))}
      else{
        (layouts.size, new Point(x, y))}}}
  private def addLayout(layer:Int, point:Point, size:Dimension) = {
    //Add new layer if need
    if(layouts.size == layer){ layouts += MutList[Rectangle]()}
    //Add layout
    layouts(layer) += new Rectangle(point.x, point.y,size.width,size.height)}
  //Methods
  def occupyLocation(size:Dimension, defX:Int, defY:Int):Point = {
    val (layer,point) = findFreeLayout(size)
    val pos = new Point(
      if(defX == Int.MaxValue){point.x}else{defX},
      if(defY == Int.MaxValue){point.y}else{defY})
    addLayout(layer, pos, size)
    pos}
  def calcStringColumnWidth(strings:List[String], font:Font):Int = {
    val metrics = canvas.getFontMetrics(font)
    strings.map(s ⇒ metrics.stringWidth(s)) match{
      case Nil ⇒ 100
      case l ⇒ l.max}}
  def calcDoubleColumnWidth(values:List[Double], font:Font):Int =
    calcStringColumnWidth(values.map(_.toString),font)}
