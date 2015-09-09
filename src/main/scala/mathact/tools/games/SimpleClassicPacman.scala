package mathact.tools.games
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.definitions.Move
import mathact.utils.definitions.MazeObj
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components._
import mathact.utils.{ToolHelper, Tool, Environment}
import mathact.utils.definitions.MazeObj._


/**
 * Simple implementation of classic Pacman game.
 * Created by CAB on 09.09.2015.
 */

abstract class SimpleClassicPacman(
  name:String = "",
  speedMin:Double = .1,
  speedMax:Double = 100,
  speedInit:Double = 1,                //Speed of Pacman time (in hertz)
  animation:Boolean = true,            //ON/OFF animation.
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = Int.MaxValue,
  screenH:Int = Int.MaxValue)
(implicit environment:Environment)
extends Tool {

  //Definitions
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
  //Variables
  private var walls:List[List[Boolean]] = List()
  private var pacman:MazeObj = MazeObj.Empty
  private var blinky:MazeObj = MazeObj.Empty
  private var inky:MazeObj = MazeObj.Empty
  private var pellets:Set[MazeObj] = Set()
  private var powerPellets:Set[MazeObj] = Set()





  //DSL
  def maze(objs:MazeDefObj*):Unit = buildInitState(prepareAndCheckMaze(objs.toSeq))












  def blinkyFunction(f:(Int,Int,Long,Set[Move],Move)⇒Move) = {

  }
  def inkyFunction(f:(Int,Int,Long,Set[Move],Move)⇒Move) = {

  }
  def updated() = {}
  //Functions
  private def collectMazeObjects:List[MazeObj] =
    (pellets.toList ++ powerPellets.toList ++ List(blinky, inky, pacman))
  private def prepareAndCheckMaze(objs:Seq[MazeDefObj]):List[List[MazeDefObj]] = {
    val maze  = objs
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
    if(maze.map(_.count(_ == C)).sum > 1){
      throw new SyntaxException("Incorrect maze: must be only not more then one pacman")}
    if(maze.map(_.count(_ == B)).sum > 1){
      throw new SyntaxException("Incorrect maze: must be only not more then one Blinky ghost")}
    if(maze.map(_.count(_ == I)).sum > 1){
      throw new SyntaxException("Incorrect maze: must be only not more then one Inky ghost")}
    maze}
  private def buildInitState(maze:List[List[MazeDefObj]]):Unit = {
    walls = maze.map(_.map(_ == H)) //Build walls
    //Build maze objects
    val iMaze = maze.zipWithIndex.flatMap{case (l,y) ⇒ l.zipWithIndex.map{case(e,x) ⇒ (x,y,e)}}
    def findObj(d:MazeDefObj, make:(Int,Int)⇒MazeObj):MazeObj =
      iMaze.find{case (_,_,e) ⇒ e == d} match{
        case Some((x,y,_)) ⇒ make(x,y)
        case _ ⇒ MazeObj.Empty}
    def filterObjs(d:MazeDefObj, make:(Int,Int)⇒MazeObj):Set[MazeObj] =
      iMaze.filter{case (_,_,e) ⇒ e == d}.map{case (x,y,_) ⇒ make(x,y)}.toSet
    pacman = findObj(C, (x,y) ⇒ MazeObj.Pacman0(x,y,Move.Right))
    blinky = findObj(B, (x,y) ⇒ MazeObj.RedGhost(x,y,Move.Stay))
    inky = findObj(I, (x,y) ⇒ MazeObj.BlueGhost(x,y,Move.Stay))
    pellets = filterObjs(o, (x,y) ⇒ MazeObj.Pellet(x,y))
    powerPellets = filterObjs(P, (x,y) ⇒ MazeObj.PowerPellet(x,y))}




  //Helpers
  private val helper = new ToolHelper(this, name, "Simple Classic Pacman")
  //UI
  private val params = environment.params.SimpleClassicPacman
  private val maze = new PacmanMaze(params, screenW, screenH)
  private val slider = new HorizontalSlider(params, speedMin, speedMax, speedInit){
    def valueChanged(v:Double) = {
//     frame.setTitleAdd(s" - $v/second")
//        timer.setDelay((1000 / v).toInt)

    }}
  private val reset = new ResetButton(params){
    def reset() = {

    }
  }
  private val label = new NameLabel(params, "SCORE: ",  params.textColor)
  private val score = new NumberLabel(params,  params.textColor)


  private val execBtn = new ExecuteButtons(params){
    def start() = {
//      timer.start()
    }
    def stop() = {
//      timer.stop()
    }
    def step() = {
//      procs.foreach(p ⇒ p())
//      gear.changed()
    }}






  private val frame = new TVFrame(
    environment.layout, params, helper.toolName, center = Some(maze), bottom = List(slider,reset,label,score,execBtn)){
    def closing() = {gear.endWork()}}
  frame.setTitleAdd(s" - $speedInit/second")







//  private val plot = new XYsPlot(environment.params.XYPlot, screenW, screenH, drawPoints)
////  private val minMaxAvg = new MinMaxAvgPane(environment.params.XYPlot)
//  private val frame = new BorderFrame(
//    environment.layout, environment.params.XYPlot, helper.toolName, center = maze){
//    def closing() = {gear.endWork()}}
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Construct maze
      maze.setWalls(walls)
      maze.update(collectMazeObjects)



//      //Prepare plot
//      val ls = lines.zipWithIndex.map{
//        case (Line(Some(n), c, _), _) ⇒ (n, c)
//        case (Line(None, c, _), i) ⇒ ("L" + i, c)}
//      plot.setLines(ls, minRange, maxRange,autoRange)
//      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)

    }
    def update() = {
//      val xys = lines.map{case Line(_,_,line) ⇒ {val (xs,ys) = line(); (xs.toList,ys.toList)}}
//      plot.update(xys)
//      minMaxAvg.update(xys.map(_._2).flatMap(e ⇒ e))
//      updated()
    }
    def stop() = {
      frame.hide()}}}