package mathact.tools.games.pacman
import mathact.utils.{Tool, Environment, ToolHelper}
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.definitions.{PacmanImg, PacmanObj}
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.{UIComponent, TVFrame, BorderFrame, PacmanMaze}
import scala.swing.Component


/**
 * Base class for different Pacman implementations.
 * Created by CAB on 17.09.2015.
 */

abstract class Pacman(
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = Int.MaxValue,
  screenH:Int = Int.MaxValue)
  (implicit environment:Environment)
extends Tool {
  //Definitions
  val Move = mathact.utils.definitions.Move
  val AgentState = mathact.utils.definitions.AgentState
  type Move = mathact.utils.definitions.Move
  type AgentState = mathact.utils.definitions.AgentState
  type AgentFun = (Int,Int,Long,AgentState,Set[Move],Move)⇒Move
  protected trait MazeDefObj
  protected case object ᗧ extends MazeDefObj  //Pacman
  protected case object C extends MazeDefObj  //Pacman
  protected case object B extends MazeDefObj  //Blinky
  protected case object I extends MazeDefObj  //Inky
  protected case object ○ extends MazeDefObj  //Pellet
  protected case object o extends MazeDefObj  //Pellet
  protected case object ● extends MazeDefObj  //Power pellet
  protected case object P extends MazeDefObj  //Power pellet
  protected case object █ extends MazeDefObj  //Wall
  protected case object H extends MazeDefObj  //Wall
  protected case object ▒ extends MazeDefObj  //Empty
  protected case object E extends MazeDefObj  //Empty
  protected case object ⏎ extends MazeDefObj //New line
  protected case object R extends MazeDefObj  //New line
  protected case object N extends MazeDefObj  //None
  //Private definitions
  protected class MazeControl{
    //Variables
    private var maze:List[List[MazeDefObj]] = List(List())
    private var oldObjects:List[PacmanObj] = List()
    private var pacman:Option[PacmanObj.ImgObj] = None
    private var blinky:Option[PacmanObj.ImgObj] = None
    private var inky:Option[PacmanObj.ImgObj] = None
    private var pellets:Set[PacmanObj.Pellet] = Set()
    private var powerPellets:Set[PacmanObj.PowerPellet] = Set()
    //Functions
    private def buildUpObj(old:Option[PacmanObj.ImgObj], x:Double, y:Double, move:Move, img:PacmanImg)
    :Option[PacmanObj.ImgObj] = old match{
      case Some(PacmanObj.ImgObj(ox, oy, oMove, oImg)) ⇒ Some(PacmanObj.ImgObj(
        if(x.isNaN) ox else x,
        if(y.isNaN) oy else y,
        if(move == Move.None) oMove else move,
        if(img == PacmanImg.None) oImg else img))
      case _ if img != PacmanImg.None ⇒ Some(PacmanObj.ImgObj(
        x, y, if(move == Move.None)  Move.Stay else move, img))
      case _ ⇒ None}
    //Methods
    def setMaze(objs:Seq[MazeDefObj]):Unit = {
      //Prepare maze
      maze  = objs
        .foldRight(List(List[MazeDefObj]())){
        case (e,ll) if e == ⏎ || e == R  ⇒ List() +: ll
        case (e, l :: t) if e == ᗧ || e == C ⇒ (C +: l) +: t
        case (e, l :: t) if e == ○ || e == o ⇒ (o +: l) +: t
        case (e, l :: t) if e == ● || e == P ⇒ (P +: l) +: t
        case (e, l :: t) if e == █ || e == H ⇒ (H +: l) +: t
        case (e, l :: t) if e == ▒ || e == E ⇒ (E +: l) +: t
        case (e, l :: t) ⇒ (e +: l) +: t}
        .filter(_.nonEmpty)
      val m = maze.head.size
      if(maze.size < 3){
        throw new SyntaxException("Incorrect maze: Min height 3 row.")}
      if(m < 3){
        throw new SyntaxException("Incorrect maze: Min wight 3 column. ")}
      if(maze.exists(_.size != m)){
        throw new SyntaxException("Incorrect maze: all rows must have same size")}
      //Build maze objects
      val iMaze = maze.zipWithIndex.map{case (l,y) ⇒ l.zipWithIndex.map{case(e,x) ⇒ (x,y,e)}}.flatMap(e ⇒ e)
      def filterObjs[T](d:MazeDefObj, make:(Int,Int)⇒T):Set[T] =
        iMaze.filter{case (_,_,e) ⇒ e == d}.map{case (x,y,_) ⇒ make(x,y)}.toSet
      pellets = filterObjs(o, (x,y) ⇒ PacmanObj.Pellet(x,y))
      powerPellets = filterObjs(P, (x,y) ⇒ PacmanObj.PowerPellet(x,y))
      def findObj[T](d:MazeDefObj, make:(Int,Int)⇒T):Option[T] = iMaze.find{case (_,_,e) ⇒ e == d} match{
        case Some((x,y,_)) ⇒ Some(make(x,y))
        case _ ⇒ None}
      pacman = findObj(C, (x,y) ⇒ PacmanObj.ImgObj(x, y, Move.Right, PacmanImg.Pacman0))
      blinky = findObj(B, (x,y) ⇒ PacmanObj.ImgObj(x, y, Move.Stay, PacmanImg.RedGhost))
      inky = findObj(I, (x,y) ⇒ PacmanObj.ImgObj(x, y, Move.Stay, PacmanImg.BlueGhost))
      //Set walls
      uiMaze.setWalls(maze.map(_.map(_ == H)))}
    def mazeM:Int = maze.size
    def mazeN:Int = maze.head.size
    def mazeObjAt(x:Int,y:Int):MazeDefObj = (x,y) match{
      case (x,y) if x < 0 || x >= mazeN || y < 0 || y >= mazeM ⇒ N
      case (x,y) ⇒ maze(y)(x)}
    def availableMoves(x:Int, y:Int):Set[Move] = {
      Set((Move.Up, x ,y - 1),(Move.Down, x, y + 1),(Move.Left,x - 1 ,y),(Move.Right,x + 1 ,y),(Move.Stay, x, y)).flatMap{
        case (m,nx,ny) if nx < 0 || ny < 0 || nx >= maze.head.size || ny >= maze.size ⇒ None
        case (m,nx,ny) if maze(ny)(nx) == H ⇒ None
        case (m,_,_) ⇒ Some(m)}}
    def updateUI():Unit = {
      val objs = pellets.toList ++ powerPellets.toList ++ List(blinky, inky, pacman).flatMap(e ⇒ e)
      if(oldObjects != objs){
        uiMaze.update(objs)
        oldObjects = objs}}
    def getPacman:Option[PacmanObj.ImgObj] = pacman
    def getBlinky:Option[PacmanObj.ImgObj] = blinky
    def getInky:Option[PacmanObj.ImgObj] = inky
    def updatePacman(
      x:Double = Double.NaN, y:Double = Double.NaN, move:Move = Move.None, img:PacmanImg = PacmanImg.None)
    :Unit = {
      pacman = buildUpObj(pacman, x, y, move, img)}
    def updateBlinky(
      x:Double = Double.NaN, y:Double = Double.NaN, move:Move = Move.None, img:PacmanImg = PacmanImg.None)
    :Unit = {
      blinky = buildUpObj(blinky, x, y, move, img)}
    def updateInky(
      x:Double = Double.NaN, y:Double = Double.NaN, move:Move = Move.None, img:PacmanImg = PacmanImg.None)
    :Unit = {
      inky = buildUpObj(inky, x, y, move, img)}
    def addPallet(x:Int, y:Int):Unit = {
      pellets += PacmanObj.Pellet(x,y)}
    def delPallet(x:Int, y:Int):Unit = {
      pellets -= PacmanObj.Pellet(x,y)}
    def getPallet(x:Int, y:Int):Option[PacmanObj.Pellet] = {
      pellets.find{case PacmanObj.Pellet(px,py) ⇒ px == x && py == y}}
    def addPowerPallet(x:Int, y:Int):Unit = {
      powerPellets += PacmanObj.PowerPellet(x,y)}
    def delPowerPallet(x:Int, y:Int):Unit = {
      powerPellets -= PacmanObj.PowerPellet(x,y)}
    def getPowerPallet(x:Int, y:Int):Option[PacmanObj.PowerPellet] = {
      powerPellets.find{case PacmanObj.PowerPellet(px,py) ⇒ px == x && py == y}}}
  //Helpers
  private val helper = new ToolHelper(this, name, "Pacman")
  private val uiParams = environment.params.PacmanUI
  protected val mazeControl = new MazeControl
  //Variables
  private var uiFrame:Option[TVFrame] = None
  private var uiFrameBottom:List[Component with UIComponent] = List()
  private var starters:List[(MazeControl)⇒Unit] = List()
  private var updaters:List[((MazeControl)⇒Unit, Int)] = List()
  //Functions
  private def prepareAndCheckMaze(objs:Seq[MazeDefObj]):List[List[MazeDefObj]] = {
    val maze  = objs
      .foldRight(List(List[MazeDefObj]())){
      case (e,ll) if e == ⏎ || e == R  ⇒ List() +: ll
      case (e, l :: t) if e == ○ || e == o ⇒ (o +: l) +: t
      case (e, l :: t) if e == ● || e == P ⇒ (P +: l) +: t
      case (e, l :: t) if e == █ || e == H ⇒ (H +: l) +: t
      case (e, l :: t) if e == ▒ || e == E ⇒ (E +: l) +: t
      case (e, l :: t) ⇒ (e +: l) +: t}
      .filter(_.nonEmpty)
    val m = maze.head.size
    if(maze.size < 3){
      throw new SyntaxException("Incorrect maze: Min height 3 row.")}
    if(m < 3){
      throw new SyntaxException("Incorrect maze: Min wight 3 column. ")}
    if(maze.exists(_.size != m)){
      throw new SyntaxException("Incorrect maze: all rows must have same size")}
    maze}
  //DSL
  protected def maze(objs:MazeDefObj*):Unit = mazeControl.setMaze(objs.toSeq)
  //Methods
  /**
   * Get number of maze lines
   * @return -  number of lines
   */
  def mazeM:Int = mazeControl.mazeM
  /**
   * Get number of maze columns
   * @return -  number of columns
   */
  def mazeN:Int = mazeControl.mazeN
  /**
   * Get maze object defined at given coordinates
   * @param x - column
   * @param y - line
   * @return - MazeDefObj
   */
  def mazeObjAt(x:Int,y:Int):MazeDefObj = mazeControl.mazeObjAt(x,y)
  /**
   * Get available moves from cell at given coordinates
   * @param x - column
   * @param y - line
   * @return - set of moves
   */
  def availableMoves(x:Int, y:Int):Set[Move] = mazeControl.availableMoves(x,y)
  //Internal methods
  protected def addBottomComponent(c:Component with UIComponent):Unit = {uiFrameBottom :+= c}
  protected def addStarter(s:(MazeControl)⇒Unit):Unit = {starters :+= s}
  protected def addUpdater(u:(MazeControl)⇒Unit, priority:Int):Unit = {updaters :+= (u, priority)}
  //UI
  private val uiMaze = new PacmanMaze(uiParams, screenW, screenH)
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Construct UI
      val frame:TVFrame = new TVFrame(
        environment.layout, uiParams, helper.toolName, center = Some(uiMaze), bottom = uiFrameBottom){
        def closing() = {gear.endWork()}}
      uiFrame = Some(frame)
      //Run starters
      starters.foreach(_(mazeControl))
      mazeControl.updateUI()
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      //Run updaters
      updaters.sortBy(_._2).foreach(_._1(mazeControl))
      mazeControl.updateUI()}
    def stop() = {
      uiFrame.foreach(_.hide())}}}