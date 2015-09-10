package mathact.tools.games
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.definitions.{Moves, MazeObj}
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components._
import mathact.utils.{ToolHelper, Tool, Environment}


/**
 * Simple implementation of classic Pacman game.
 * Pacman time outn is always 5.
 * Created by CAB on 09.09.2015.
 */

abstract class SimpleClassicPacman(
  name:String = "",
  speedMin:Double = .1,
  speedMax:Double = 100,
  speedInit:Double = 20,               //Speed of Pacman time (in hertz)
  animation:Boolean = true,            //ON/OFF animation.
  started:Boolean = false,             //Start right after creation.
  blinkyTimeout:Int = 6,
  inkyTimeout:Int = 7,
  initScore:Int = 100,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = Int.MaxValue,
  screenH:Int = Int.MaxValue)
(implicit environment:Environment)
extends Tool with Moves{
  //Parameters


  //Definitions
  type AgentFun = (Int,Int,Long,Set[Move],Move)⇒Move
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
  //Private definitions


  //Variables
  private var gameTime:Long = 0
  private var walls:List[List[Boolean]] = List()
  private var pacman:Option[(MazeObj,Int)] = None
  private var pacmanFun:Option[AgentFun] = None
  private var blinky:Option[(MazeObj,Int)] = None
  private var blinkyFun:Option[AgentFun] = None
  private var inky:Option[(MazeObj,Int)] = None
  private var inkyFun:Option[AgentFun] = None
  private var pellets:Set[MazeObj] = Set()
  private var powerPellets:Set[MazeObj] = Set()
  private var score:Int = initScore















  //DSL
  def maze(objs:MazeDefObj*):Unit = buildInitState(prepareAndCheckMaze(objs.toSeq))
  def pacmanFunction(f:(Int,Int,Long,Set[Move],Move)⇒Move) = {pacmanFun = Some(f)}
  def blinkyFunction(f:(Int,Int,Long,Set[Move],Move)⇒Move) = {blinkyFun = Some(f)}
  def inkyFunction(f:(Int,Int,Long,Set[Move],Move)⇒Move) = {inkyFun = Some(f)}
  def updated() = {}
  //Functions
  private def buildMazeObjects():List[MazeObj] =
    pellets.toList ++ powerPellets.toList ++ List(blinky, inky, pacman).flatMap(_.map{case (o,_) ⇒ o})
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
    pacman = Some((findObj(C, (x,y) ⇒ MazeObj.Pacman0(x,y,Move.Right)), 0))
    blinky = Some((findObj(B, (x,y) ⇒ MazeObj.RedGhost(x,y,Move.Stay)), 0))
    inky = Some((findObj(I, (x,y) ⇒ MazeObj.BlueGhost(x,y,Move.Stay)), 0))
    pellets = filterObjs(o, (x,y) ⇒ MazeObj.Pellet(x,y))
    powerPellets = filterObjs(P, (x,y) ⇒ MazeObj.PowerPellet(x,y))}
  private def checkAgentFunctions():Unit = {
    def msg(n:String):String = s"When $n defined in maze, they function should be defined too."
    if(pacman.nonEmpty && pacmanFun.isEmpty){throw new SyntaxException(msg("Pacman"))}
    if(blinky.nonEmpty && blinkyFun.isEmpty){throw new SyntaxException(msg("Blinky"))}
    if(inky.nonEmpty && inkyFun.isEmpty){throw new SyntaxException(msg("Inky"))}}
  private def getSetOfAvailableMoves(x:Int, y:Int):Set[Move] = {
    Set((Move.Up, x ,y - 1),(Move.Down, x, y + 1),(Move.Left,x - 1 ,y),(Move.Right,x + 1 ,y),(Move.Stay, x, y)).flatMap{
      case (m,nx,ny) if nx < 0 || ny < 0 || nx >= walls.head.size || ny >= walls.size ⇒ None
      case (m,nx,ny) if walls(ny)(nx) ⇒ None
      case (m,_,_) ⇒ Some(m)}}
  private def eRounding(x:Double):Double = x match{
    case x if (1 - (x - x.toInt)) < .0001 ⇒ x.toInt + 1
    case x if (x - x.toInt) < .0001       ⇒ x.toInt
    case x                                ⇒ x}
  private def calcPartPositionByMove(cx:Double, cy:Double, m:Move, timeout:Int):(Double,Double) = {
    val p = 1.0 / timeout
    val (nx,ny) = m match{
      case Move.Up ⇒ (cx,cy - p)
      case Move.Down ⇒ (cx,cy + p)
      case Move.Left ⇒ (cx - p,cy)
      case Move.Right ⇒ (cx + p,cy)
      case _ ⇒ (cx,cy)}
    (eRounding(nx), eRounding(ny))}
  private def doStep():Unit = {
    println("step")
    //Game logic
    if(score < 0){

      //!!Game over


    }



    //Object interaction logic
    val pPellets = pellets
    val pPowerPellets = powerPellets
    val pScope = score
    pacman match{
      case Some((MazeObj.Pacman0(xd,yd,m), t)) if t == 0 ⇒ {
        val (x,y) = (xd.toInt,yd.toInt)
        //Check for ghost collision
        val ghostPos = Seq(blinky,inky).flatMap{
          case Some((MazeObj.RedGhost(bx,by,_), _)) ⇒ Some((bx.toInt,by.toInt))
          case Some((MazeObj.BlueGhost(ix,iy,_), _)) ⇒ Some((ix.toInt,iy.toInt))
          case _ ⇒ None}

        //!!If ghost is scerry +200 else -100

        if(ghostPos.contains((x,y))){  //!!!Error: fire several times
          score -= 100
          uiScore.setNumber(score)}


        //Check for pallets
        pellets.foreach{
          case p:MazeObj.Pellet if p.x.toInt == x  && p.y.toInt == y ⇒ {
            pellets = pellets.filter(_ != p)
            score += 9
            uiScore.setNumber(score)}
          case _ ⇒}
        //Check for power pellet
        powerPellets.foreach{
          case p:MazeObj.PowerPellet if p.x.toInt == x  && p.y.toInt == y ⇒ {
          powerPellets = powerPellets.filter(_ != p)
            score += 100
            uiScore.setNumber(score)




          }
          case _ ⇒}}
      case _ ⇒}
    //Pacman logic
    val pPacman = pacman
    pacman = (pacman, pacmanFun) match{
      case (Some((MazeObj.Pacman0(x,y,m), t)), Some(f)) if t == 0 ⇒ {
        //Get next move
        val availableMoves = getSetOfAvailableMoves(x.toInt, y.toInt)
        val nextMove = f(x.toInt, y.toInt, gameTime, availableMoves, m)
        //Apply move, set Timer
        (nextMove, availableMoves.contains(nextMove)) match{
          case (nm, true) if nm != Move.Stay ⇒ {
            val (px,py) = calcPartPositionByMove(x, y, nm, if(animation) 5 else 1)
            Some(MazeObj.Pacman0(px,py,nm), 4)}
          case (nm, false) if nm != Move.Stay ⇒ {
            Some(MazeObj.Pacman0(x,y,nm), 0)}
          case _ ⇒ {
            pacman}}}
      case (Some((MazeObj.Pacman0(x,y,m), t)), _) if animation && t == 4 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, m, 5)
        Some((MazeObj.Pacman1(px,py, m), 3))}
      case (Some((MazeObj.Pacman1(x,y,m), t)), _) if animation && t == 3 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, m, 5)
        Some((MazeObj.Pacman2(px,py, m), 2))}
      case (Some((MazeObj.Pacman2(x,y,m), t)), _) if animation && t == 2 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, m, 5)
        Some((MazeObj.Pacman1(px,py, m), 1))}
      case (Some((MazeObj.Pacman1(x,y,m), t)), _) if animation && t == 1 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, m, 5)
        Some((MazeObj.Pacman0(px,py, m), 0))}
      case _ ⇒ pacman}
    //Blinky logic
    val pBlinky = blinky
    blinky = (blinky, blinkyFun) match{
      case (Some((MazeObj.RedGhost(x,y,m), t)), Some(f)) if t == 0 ⇒ {
        //Get next move
        val availableMoves = getSetOfAvailableMoves(x.toInt, y.toInt)
        val nextMove = f(x.toInt, y.toInt, gameTime, availableMoves, m) match{
          case nm if availableMoves.contains(nm) ⇒ nm
          case _ ⇒ Move.Stay}
        //Apply move, set Timer
        val (px,py) = calcPartPositionByMove(x, y, nextMove, if(animation) blinkyTimeout else 1)
        Some((MazeObj.RedGhost(px,py,nextMove), if(nextMove != Move.Stay) blinkyTimeout - 1 else 0))}
      case (Some((MazeObj.RedGhost(x,y,m), t)), _) if animation && t != 0 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, m, blinkyTimeout)
        Some((MazeObj.RedGhost(px,py,m), t - 1))}
      case _ ⇒ blinky}
    //Inky logic
    val pInky = inky
    inky = (inky, inkyFun) match{
      case (Some((MazeObj.BlueGhost(x,y,m), t)), Some(f)) if t == 0 ⇒ {
        //Get next move
        val availableMoves = getSetOfAvailableMoves(x.toInt, y.toInt)
        val nextMove = f(x.toInt, y.toInt, gameTime, availableMoves, m) match{
          case nm if availableMoves.contains(nm) ⇒ nm
          case _ ⇒ Move.Stay}
        //Apply move, set Timer
        val (px,py) = calcPartPositionByMove(x, y, nextMove, if(animation) inkyTimeout else 1)
        Some((MazeObj.BlueGhost(px,py,nextMove), if(nextMove != Move.Stay) inkyTimeout - 1 else 0))}
      case (Some((MazeObj.BlueGhost(x,y,m), t)), _) if animation && t != 0 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, m, inkyTimeout)
        Some((MazeObj.BlueGhost(px,py,m), t - 1))}
      case _ ⇒ inky}
    //Decrease scope if not change and 5 tics gone
    if(pScope == score && (gameTime % 5) == 0){
      score -= 1
      uiScore.setNumber(score)}
    //Update maze and
    val update =
      pPacman != pacman ||
      pBlinky != blinky ||
      pInky != inky ||
      pPellets != pellets ||
      pPowerPellets != powerPellets
    if(update){
      uiMaze.update(buildMazeObjects())}
    //Increment time
    gameTime += 1}
  //Timer
  private val timer = new Timer((1000 / speedInit).toInt, new ActionListener() {
    def actionPerformed(evt:ActionEvent) = {doStep()}})
  //Helpers
  private val helper = new ToolHelper(this, name, "Simple Classic Pacman")
  //UI
  private val uiParams = environment.params.SimpleClassicPacman
  private val uiMaze = new PacmanMaze(uiParams, screenW, screenH)
  private val uiSlider = new HorizontalSlider(uiParams, speedMin, speedMax, speedInit){
    def valueChanged(v:Double) = {
      frame.setTitleAdd(s" - $v/second")
      timer.setDelay((1000 / v).toInt)}}
  private val uiReset = new ResetButton(uiParams){
    def reset() = {


    }
  }
  private val uiLabel = new NameLabel(uiParams, "SCORE: ",  uiParams.textColor)
  private val uiScore = new NumberLabel(uiParams,  uiParams.textColor)
  private val uiExecBtn = new ExecuteButtons(uiParams){
    def start() = {timer.start()}
    def stop() = {timer.stop()}
    def step() = {doStep()}}
  private val frame:TVFrame = new TVFrame(
    environment.layout, uiParams, helper.toolName, center = Some(uiMaze), bottom = List(uiSlider,uiReset,uiLabel,uiScore,uiExecBtn)){
    def closing() = {gear.endWork()}}
  frame.setTitleAdd(s" - $speedInit/second")
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Construct maze
      checkAgentFunctions()
      uiMaze.setWalls(walls)
      uiMaze.update(buildMazeObjects())



//      //Prepare plot
//      val ls = lines.zipWithIndex.map{
//        case (Line(Some(n), c, _), _) ⇒ (n, c)
//        case (Line(None, c, _), i) ⇒ ("L" + i, c)}
//      plot.setLines(ls, minRange, maxRange,autoRange)
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)
      //Start
      if(started){
        uiExecBtn.setStarted(true)
        timer.start()}
    }
    def update() = {
//      val xys = lines.map{case Line(_,_,line) ⇒ {val (xs,ys) = line(); (xs.toList,ys.toList)}}
//      plot.update(xys)
//      minMaxAvg.update(xys.map(_._2).flatMap(e ⇒ e))
//      updated()
    }
    def stop() = {
      frame.hide()}}}