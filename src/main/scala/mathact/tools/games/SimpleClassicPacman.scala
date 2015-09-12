package mathact.tools.games
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer
import mathact.utils.clockwork.VisualisationGear
import mathact.utils.definitions.{PacmanImg, PacmanObj, Move}
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
  funkyTimeout:Int = 200,              //Before out of Funky state
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
  //Private definitions
  private val defaultFunction:AgentFun = (_,_,_,_,_,_)⇒Move.Stay
  private case object MazeObj{
    def apply[T <: MazeObj[T] : Manifest](
      x:Double, y:Double, move:Move, img:PacmanImg, time:Int, state:AgentState, fun:AgentFun, col:Option[(Int,Int)])
    :T = manifest[T] match{
      case m if m == manifest[Pacman] ⇒ new Pacman(x, y, move, img, time, state, fun).asInstanceOf[T]
      case m if m == manifest[Blinky] ⇒ new Blinky(x, y, move, img, time, state, fun, col).asInstanceOf[T]
      case m if m == manifest[Inky]   ⇒ new Inky(x, y, move, img, time, state, fun, col).asInstanceOf[T]}
    def apply[T <: MazeObj[T] : Manifest](x:Double, y:Double):T =
      apply[T](x, y, Move.Stay, PacmanImg.EmptyImg, 0, AgentState.NotDefined, defaultFunction, None)
    def apply[T <: MazeObj[T] : Manifest](f:AgentFun):T =
      apply[T](0, 0, Move.Stay, PacmanImg.EmptyImg, 0, AgentState.NotDefined, f, None)
    def apply[T <: MazeObj[T] : Manifest]():T =
      apply[T](0, 0, Move.Stay, PacmanImg.EmptyImg, 0, AgentState.NotDefined, defaultFunction, None)}
  private abstract class MazeObj[T <: MazeObj[T] : Manifest](
    val ex:Double, val ey:Double, val eMove:Move, val eImg:PacmanImg,
    val eTime:Int, val eState:AgentState, val eFun:AgentFun, val eCol:Option[(Int,Int)])
  extends PacmanObj.ImgObj(ex, ey, eMove, eImg){
    def setXY(x:Double, y:Double):T = MazeObj[T](x, y, eMove, eImg, eTime, eState, eFun, eCol)
    def setFun(f:AgentFun):T = MazeObj[T](ex, ey, eMove, eImg, eTime, eState, f, eCol)
    def setImg(img:PacmanImg):T = MazeObj[T](ex, ey, eMove, img, eTime, eState, eFun, eCol)
    def setMove(m:Move):T = MazeObj[T](ex, ey, m, eImg, eTime, eState, eFun, eCol)
    def setState(s:AgentState):T = MazeObj[T](ex, ey, eMove, eImg, eTime, s, eFun, eCol)
    def setTime(t:Int):T = MazeObj[T](ex, ey, eMove, eImg, t, eState, eFun, eCol)
    def setCollision(c:Option[(Int,Int)]):T = MazeObj[T](ex, ey, eMove, eImg, eTime, eState, eFun, c)}
  private case class Pacman(
    x:Double, y:Double, move:Move, img:PacmanImg, time:Int, state:AgentState, fun:AgentFun)
    extends MazeObj[Pacman](x, y, move, img, time, state, fun, None)
  private case class Blinky(
    x:Double, y:Double, move:Move, img:PacmanImg, time:Int, state:AgentState, fun:AgentFun, collision:Option[(Int,Int)])
    extends MazeObj[Blinky](x, y, move, img, time, state, fun, collision)
  private case class Inky(
    x:Double, y:Double, move:Move, img:PacmanImg, time:Int, state:AgentState, fun:AgentFun, collision:Option[(Int,Int)])
    extends MazeObj[Inky](x, y, move, img, time, state, fun, collision)
  //Variables
  private var maze:List[List[MazeDefObj]] = List(List())
  private var gameTime:Long = 0
  private var gameScore:Int = initScore
  private var pacman:Pacman = MazeObj[Pacman]()
  private var blinky:Blinky = MazeObj[Blinky]()
  private var inky:Inky = MazeObj[Inky]()
  private var pellets:Set[PacmanObj.Pellet] = Set()
  private var powerPellets:Set[PacmanObj.PowerPellet] = Set()
  private var funkyTimer:Int = 0
  private var gameBegin:Boolean = false
  private var gameOver:Boolean = false
  //DSL
  def maze(objs:MazeDefObj*):Unit = {
    maze = prepareAndCheckMaze(objs.toSeq)
    buildInitState()}
  def pacmanFunction(f:AgentFun) = {pacman = pacman.setFun(f)}
  def blinkyFunction(f:AgentFun) = {blinky = blinky.setFun(f)}
  def inkyFunction(f:AgentFun) = {inky = inky.setFun(f)}
  def time:Long = gameTime
  def score:Int = gameScore
  def pacmanPos:(Int,Int) = (pacman.x.toInt, pacman.y.toInt)
  def pacmanMove:Move = pacman.move
  def pacmanState:AgentState = pacman.state
  def blinkyPos:(Int,Int) = (blinky.x.toInt, blinky.y.toInt)
  def blinkyMove:Move = blinky.move
  def blinkyState:AgentState = blinky.state
  def inkyPos:(Int,Int) = (inky.x.toInt, inky.y.toInt)
  def inkyMove:Move = inky.move
  def inkyState:AgentState = inky.state
  def mazeM:Int = maze.size        //Lines
  def mazeN:Int = maze.head.size   //Columns
  def mazeObjAt(x:Int,y:Int):MazeDefObj = (x,y) match{
    case (x,y) if x < 0 || x >= mazeN || y < 0 || y >= mazeM ⇒ E
    case (x,y) ⇒ maze(x)(y)}
  //Functions
  private def buildMazeObjects():List[PacmanObj] =
    pellets.toList ++ powerPellets.toList ++ List(blinky, inky, pacman).filter(_.eState != AgentState.NotDefined)
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
  private def buildInitState():Unit = {
    //Build maze objects
    val iMaze = maze.zipWithIndex.map{case (l,y) ⇒ l.zipWithIndex.map{case(e,x) ⇒ (x,y,e)}}.flatMap(e ⇒ e)
    def findObj[T <: MazeObj[T] : Manifest](prev:T, d:MazeDefObj, img:PacmanImg, state:AgentState):T = {
      iMaze.find{case (_,_,e) ⇒ e == d} match{
        case Some((x,y,_)) ⇒ prev.setXY(x,y).setImg(img).setState(state)
        case None ⇒ prev}}
    pacman = findObj(pacman, C, PacmanImg.Pacman0, AgentState.Live)
    blinky = findObj(blinky, B, PacmanImg.RedGhost, AgentState.Vagrant)
    inky = findObj(inky, I, PacmanImg.BlueGhost, AgentState.Vagrant)
    def filterObjs[T](d:MazeDefObj, make:(Int,Int)⇒T):Set[T] =
      iMaze.filter{case (_,_,e) ⇒ e == d}.map{case (x,y,_) ⇒ make(x,y)}.toSet
    pellets = filterObjs(o, (x,y) ⇒ PacmanObj.Pellet(x,y))
    powerPellets = filterObjs(P, (x,y) ⇒ PacmanObj.PowerPellet(x,y))}
  private def getSetOfAvailableMoves(x:Int, y:Int):Set[Move] = {
    Set((Move.Up, x ,y - 1),(Move.Down, x, y + 1),(Move.Left,x - 1 ,y),(Move.Right,x + 1 ,y),(Move.Stay, x, y)).flatMap{
      case (m,nx,ny) if nx < 0 || ny < 0 || nx >= maze.head.size || ny >= maze.size ⇒ None
      case (m,nx,ny) if maze(ny)(nx) == H ⇒ None
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
  private def ghostCollision[T <: MazeObj[T]](ghost:T):T = ghost.eState match {
    case AgentState.Funky ⇒ {
      gameScore += 30
      val gs = maze.zipWithIndex.map{case (l,y) ⇒ l.zipWithIndex.map{case(e,x) ⇒ (x,y,e)}}.flatMap(_.find{
        case (_,_,e) if ghost.isInstanceOf[Blinky] ⇒ e == B
        case (_,_,e) if ghost.isInstanceOf[Inky] ⇒ e == I
        case _ ⇒ false})
     gs.headOption match{
       case Some((x,y,_)) ⇒ ghost.setXY(x,y).setTime(0)
       case _ ⇒ ghost}}
    case AgentState.Vagrant | AgentState.Pursuer | AgentState.Live ⇒ {
      gameScore -= 20
      ghost}
    case _ ⇒ ghost}
  private def updateGhostState(state:AgentState):Unit = {
    def upImg[T <: MazeObj[T]](ghost:T):T = state match{
      case AgentState.Funky ⇒ ghost.setImg(PacmanImg.WhiteGhost)
      case _ ⇒ ghost match{
        case _:Blinky ⇒ ghost.setImg(PacmanImg.RedGhost)
        case _:Inky ⇒ ghost.setImg(PacmanImg.BlueGhost)}}
    def upMove[T <: MazeObj[T]](ghost:T):T = state match{
      case AgentState.Dead ⇒ ghost.setMove(Move.Stay)
      case _ ⇒ ghost}
    blinky = upMove(upImg(blinky)).setState(state)
    inky = upMove(upImg(inky)).setState(state)}
  private def gameStart():Unit = if(! gameBegin){
    gameBegin = true
    updateGhostState(AgentState.Vagrant)}
  private def gameReStart():Unit = if(gameBegin || gameOver){
    //Clear data
    gameTime = 0
    gameScore = initScore
    pacman = MazeObj[Pacman](pacman.fun)
    blinky = MazeObj[Blinky](blinky.fun)
    inky = MazeObj[Inky](inky.fun)
    pellets = Set()
    powerPellets = Set()
    funkyTimer = 0
    gameBegin = false
    gameOver = false
    //Rebuilg maze objects
    buildInitState()
    uiMaze.update(buildMazeObjects())
    uiScore.setNumber(Double.NaN)
    //Set buttons state and stop timer
    uiExecBtn.setEnable(true)
    timer.stop()}
  private def gameEnd():Unit = if(! gameOver){
    gameOver = true
    updateGhostState(AgentState.Dead)
    timer.stop()
    uiExecBtn.setEnable(false)}
  private def doStep():Unit = {
    //Init
    val pPellets = pellets
    val pPowerPellets = powerPellets
    val pScope = gameScore
    val (px, py) = (pacman.x.toInt, pacman.y.toInt)
    //Check for ghost collision
    def checkCol[T <: MazeObj[T]](ghost: T):T = (ghost.ex, ghost.ey, ghost.eCol) match {
      case (gx, gy, None) if (gx.toInt, gy.toInt) == (px, py) ⇒ {
        ghostCollision(ghost.setCollision(Some((px, py))))}
      case (gx, gy, Some((cx, cy))) if (gx.toInt, gy.toInt) != (cx, cy) ⇒ {
        ghost.setCollision(None)}
      case _ ⇒ ghost}
    blinky = checkCol(blinky)
    inky = checkCol(inky)
    //Check for pallets
    pellets.foreach{
      case p:PacmanObj.Pellet if p.x.toInt == px  && p.y.toInt == py ⇒ {
        pellets = pellets.filter(_ != p)
        gameScore += 9}
      case _ ⇒}
    //Check for power pellet
    powerPellets.foreach{
      case p:PacmanObj.PowerPellet if p.x.toInt == px  && p.y.toInt == py ⇒ {
      powerPellets = powerPellets.filter(_ != p)
        gameScore += 100
        updateGhostState(AgentState.Funky)
        funkyTimer = funkyTimeout}
      case _ ⇒}
    //Pacman logic
    val pPacman = pacman
    pacman = pacman match{
      case Pacman(x, y, move, img, time, state, fun) if time == 0 ⇒ {
        //Get next move
        val availableMoves = getSetOfAvailableMoves(x.toInt, y.toInt)
        val nextMove = fun(x.toInt, y.toInt, gameTime, state, availableMoves, move)
        //Apply move, set Timer
        (nextMove, availableMoves.contains(nextMove)) match{
          case (nm, true) if nm != Move.Stay ⇒ {
            val (px,py) = calcPartPositionByMove(x, y, nm, if(animation) 5 else 1)
            Pacman(px, py, nm, img, 4, state, fun)}
          case (nm, false) ⇒ {
            Pacman(x, y, nm, img, 0, state, fun)}
          case (Move.Stay, _)⇒ {
            Pacman(x, y, Move.Stay, img, 0, state, fun)}
          case _ ⇒ {
            pacman}}}
      case Pacman(x, y, move, img, time, state, fun) if (! animation) && time != 0 ⇒ {
        Pacman(x, y, move, img, time - 1, state, fun)}
      case Pacman(x, y, move, _, time, state, fun) if animation && time == 4 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, move, 5)
        Pacman(px, py, move, PacmanImg.Pacman1, 3, state, fun)}
      case Pacman(x, y, move, _, time, state, fun) if animation && time == 3 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, move, 5)
        Pacman(px, py, move, PacmanImg.Pacman2, 2, state, fun)}
      case Pacman(x, y, move, _, time, state, fun) if animation && time == 2 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, move, 5)
        Pacman(px, py, move, PacmanImg.Pacman1, 1, state, fun)}
      case Pacman(x, y, move, _, time, state, fun) if animation && time == 1 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, move, 5)
        Pacman(px, py, move, PacmanImg.Pacman0, 0, state, fun)}
      case _ ⇒ pacman}
    //Ghosts logic
    def upGhost[T <: MazeObj[T]](ghost: T, timeout:Int):T = {
      (ghost.ex, ghost.ey, ghost.eMove, ghost.eTime, ghost.eState, ghost.eFun) match{
        case (x, y, move, time, state, fun) if time == 0 ⇒ {
          //Get next move
          val availableMoves = getSetOfAvailableMoves(x.toInt, y.toInt)
          val nextMove = fun(x.toInt, y.toInt, gameTime, state, availableMoves, move) match{
            case nm if availableMoves.contains(nm) ⇒ nm
            case _ ⇒ Move.Stay}
          //Apply move, set Timer
          val (px,py) = calcPartPositionByMove(x, y, nextMove, if(animation) timeout else 1)
          ghost.setXY(px,py).setMove(nextMove).setTime(if(nextMove != Move.Stay) timeout - 1 else 0)}
      case (x, y, move, time, _, _) if (! animation) && time != 0 ⇒ {
        ghost.setTime(time - 1)}
      case (x, y, move, time, _, _) if animation && time != 0 ⇒ {
        val (px,py) = calcPartPositionByMove(x, y, move, timeout)
        ghost.setXY(px,py).setTime(time - 1)}
      case _ ⇒ ghost}}
    val pBlinky = blinky
    blinky = upGhost(blinky,blinkyTimeout)
    val pInky = inky
    inky = upGhost(inky,inkyTimeout)
    //Ghost funky timer
    funkyTimer = funkyTimer match{
      case 0 ⇒ 0
      case 1 ⇒ {
        updateGhostState(AgentState.Vagrant)
        0}
      case t ⇒ t - 1}
    //Check for game over
    if((gameScore < 0 || pellets.isEmpty) && pacman.state != AgentState.NotDefined && (gameTime % 5) == 0){
      gameEnd()}
    //Decrease scope if not change and 5 tics gone
    if(pScope == gameScore && (gameTime % 5) == 0 && pacman.state != AgentState.NotDefined && (! gameOver)){
      gameScore -= 1}
    //Update maze and
    uiScore.setNumber(gameScore)
    val update =
      (pPacman.x,pPacman.y,pPacman.img) != (pacman.x,pacman.y,pacman.img) ||
      (pBlinky.x,pBlinky.y,pBlinky.img) != (blinky.x,blinky.y,blinky.img) ||
      (pInky.x,  pInky.y,  pInky.img)   != (inky.x,  inky.y,  inky.img) ||
      pPellets != pellets ||
      pPowerPellets != powerPellets
    if(update){
      uiMaze.update(buildMazeObjects())}
    //Call gear update function each zero time
    if(pacman.time == 0 || blinky.time == 0 || inky.time == 0){gear.changed()}
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
      gameReStart()}}
  private val uiLabel = new NameLabel(uiParams, "SCORE: ",  uiParams.textColor)
  private val uiScore = new NumberLabel(uiParams,  uiParams.textColor)
  private val uiExecBtn = new ExecuteButtons(uiParams){
    def start() = {
      gameStart()
      timer.start()}
    def stop() = {
      timer.stop()}
    def step() = {
      gameStart()
      doStep()}}
  private val frame:TVFrame = new TVFrame(
    environment.layout, uiParams, helper.toolName, center = Some(uiMaze), bottom = List(uiSlider,uiReset,uiLabel,uiScore,uiExecBtn)){
    def closing() = {gear.endWork()}}
  frame.setTitleAdd(s" - $speedInit/second")
  //Gear
  private val gear:VisualisationGear = new VisualisationGear(environment.clockwork){
    def start() = {
      //Construct maze
      uiMaze.setWalls(maze.map(_.map(_ == H)))
      uiMaze.update(buildMazeObjects())
      //Show
      frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)
      //Start
      if(started){
        uiExecBtn.setStarted(true)
        gameStart()
        timer.start()}}
    def update() = {}
    def stop() = {
      frame.hide()}}}
