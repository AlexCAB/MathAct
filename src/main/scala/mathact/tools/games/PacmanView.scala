package mathact.tools.games
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.definitions.{PacmanObj, PacmanImg}
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.{BorderFrame, PacmanMaze}
import mathact.utils.{ToolHelper, Tool, Environment}


/**
 * Very simple visualisation tool, which just show of Pacman and Ghosts position in the maze.
 * Created by CAB on 12.09.2015.
 */

abstract class PacmanView(
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = Int.MaxValue,
  screenH:Int = Int.MaxValue)
  (implicit environment:Environment)
extends Tool {
  //Definitions
  val Move = mathact.utils.definitions.Move
  type Move = mathact.utils.definitions.Move
  protected trait MazeDefObj
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
  //Variables
  private var maze:List[List[MazeDefObj]] = List(List())
  private var pacman:Option[PacmanObj.ImgObj] = None
  private var pacmanFun:Option[()⇒(Double,Double)] = None
  private var blinky:Option[PacmanObj.ImgObj] = None
  private var blinkyFun:Option[()⇒(Double,Double)] = None
  private var inky:Option[PacmanObj.ImgObj] = None
  private var inkyFun:Option[()⇒(Double,Double)] = None
  private var pellets:Set[PacmanObj.Pellet] = Set()
  private var powerPellets:Set[PacmanObj.PowerPellet] = Set()
  //DSL
  protected def maze(objs:MazeDefObj*):Unit = {
    maze = prepareAndCheckMaze(objs.toSeq)
    buildInitState()}
  def mazeM:Int = maze.size        //Lines
  def mazeN:Int = maze.head.size   //Columns
  def mazeObjAt(x:Double,y:Double):MazeDefObj = (x.toInt,y.toInt) match{
      case (x,y) if x < 0 || x >= mazeN || y < 0 || y >= mazeM ⇒ E
      case (x,y) ⇒ maze(y)(x)}
  def pacmanOf(f: ⇒(Double,Double)) = {
    pacmanFun = Some(()⇒{f})}
  def blinkyOf(f: ⇒(Double,Double)) = {
    blinkyFun = Some(()⇒{f})}
  def inkyOf(f: ⇒(Double,Double)) = {
    inkyFun = Some(()⇒{f})}
  def availableMoves(x:Int, y:Int):Set[Move] = {
    Set((Move.Up, x ,y - 1),(Move.Down, x, y + 1),(Move.Left,x - 1 ,y),(Move.Right,x + 1 ,y),(Move.Stay, x, y)).flatMap{
      case (m,nx,ny) if nx < 0 || ny < 0 || nx >= maze.head.size || ny >= maze.size ⇒ None
      case (m,nx,ny) if maze(ny)(nx) == H ⇒ None
      case (m,_,_) ⇒ Some(m)}}
  //Functions
  private def buildMazeObjects():List[PacmanObj] =
    pellets.toList ++ powerPellets.toList ++ List(blinky, inky, pacman).flatMap(e ⇒ e)
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
  private def buildInitState():Unit = {
    //Build maze objects
    val iMaze = maze.zipWithIndex.map{case (l,y) ⇒ l.zipWithIndex.map{case(e,x) ⇒ (x,y,e)}}.flatMap(e ⇒ e)
    def filterObjs[T](d:MazeDefObj, make:(Int,Int)⇒T):Set[T] =
      iMaze.filter{case (_,_,e) ⇒ e == d}.map{case (x,y,_) ⇒ make(x,y)}.toSet
    pellets = filterObjs(o, (x,y) ⇒ PacmanObj.Pellet(x,y))
    powerPellets = filterObjs(P, (x,y) ⇒ PacmanObj.PowerPellet(x,y))}
  private def updateObjectPosition():Unit = {
    pacman = pacmanFun.map(f ⇒ {val (x,y) = f(); PacmanObj.ImgObj(x, y, Move.Stay, PacmanImg.Pacman0)})
    blinky = blinkyFun.map(f ⇒ {val (x,y) = f(); PacmanObj.ImgObj(x, y, Move.Stay, PacmanImg.RedGhost)})
    inky = inkyFun.map(f ⇒ {val (x,y) = f(); PacmanObj.ImgObj(x, y, Move.Stay, PacmanImg.BlueGhost)})}
  //Helpers
  private val helper = new ToolHelper(this, name, "Pacman view")
  //UI
  private val uiParams = environment.params.PacmanView
  private val uiMaze = new PacmanMaze(uiParams, screenW, screenH)
  private val frame:BorderFrame = new BorderFrame(
    environment.layout, uiParams, helper.toolName, center = Some(uiMaze)){
    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Construct maze
      uiMaze.setWalls(maze.map(_.map(_ == H)))
      updateObjectPosition()
      uiMaze.update(buildMazeObjects())
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
    def update() = {
      val po = (pacman,blinky,inky)
      updateObjectPosition()
      if(po != (pacman,blinky,inky)){uiMaze.update(buildMazeObjects())}}
    def stop() = {
      frame.hide()}}}