package mathact.tools.games.pacman
import mathact.utils.definitions.{PacmanObj, PacmanImg, Direction}


/**
 * Set of control methods
 * Created by CAB on 21.09.2015.
 */

trait PacmanControlMethods { _:Pacman ⇒
  //Definitions
  trait MazeObj{val d:Int}
  case class Pallet(d:Int) extends MazeObj
  case class PowerPallet(d:Int) extends MazeObj
  case class Inky(d:Int) extends MazeObj
  case class Blinky(d:Int) extends MazeObj
  case class WhiteGhost(d:Int) extends MazeObj
  case class DirectPath(d:Int) extends MazeObj
  case class LeftFace(d:Int) extends MazeObj
  case class RightFace(d:Int) extends MazeObj
  case class DeadEnd(d:Int) extends MazeObj
  //Functions
  private def buildVisibleObj(x:Int, y:Int, i:Int, isHorizontal:Boolean):List[MazeObj] = {
    List(
      mazeControl.getPallet(x, y).map(_ ⇒ Pallet(i)),
      mazeControl.getPowerPallet(x, y).map(_ ⇒ PowerPallet(i)),
      Seq(mazeControl.getInky, mazeControl.getBlinky).flatMap{
        case Some(g) if g.mx == x && g.my == y && g.mImg == PacmanImg.WhiteGhost ⇒ Some(WhiteGhost(i))
        case _ ⇒ None}.headOption,
      mazeControl.getInky match{
        case Some(g) if g.mx == x && g.my == y && g.mImg != PacmanImg.WhiteGhost ⇒ Some(Inky(i))
        case  _ ⇒ None},
      mazeControl.getBlinky match{
        case Some(g) if g.mx == x && g.my == y && g.mImg != PacmanImg.WhiteGhost ⇒ Some(Blinky(i))
        case  _ ⇒ None},
      mazeObjAt(if(isHorizontal) x else x + 1, if(isHorizontal) y + 1 else y) match{
        case H | N ⇒ None
        case _ ⇒ Some(LeftFace(i))},
      mazeObjAt(if(isHorizontal) x else x - 1, if(isHorizontal) y - 1 else y) match{
        case H | N ⇒ None
        case _ ⇒ Some(RightFace(i))}).flatMap(e ⇒ e)}
  private def transformMove(m:Move):Move = (currentDirection, m) match {
    case (Direction.Down,Move.Up)     ⇒ Move.Down
    case (Direction.Down,Move.Down)   ⇒ Move.Up
    case (Direction.Down,Move.Left)   ⇒ Move.Right
    case (Direction.Down,Move.Right)  ⇒ Move.Left
    case (Direction.Left,Move.Up)     ⇒ Move.Left
    case (Direction.Left,Move.Down)   ⇒ Move.Right
    case (Direction.Left,Move.Left)   ⇒ Move.Down
    case (Direction.Left,Move.Right)  ⇒ Move.Up
    case (Direction.Right,Move.Up)    ⇒ Move.Right
    case (Direction.Right,Move.Down)  ⇒ Move.Left
    case (Direction.Right,Move.Left)  ⇒ Move.Up
    case (Direction.Right,Move.Right) ⇒ Move.Down
    case (_, m) ⇒ m}
  private def movePacman(m:Move, n:Int):Unit = (1 to n).foreach{ _ ⇒
    val tm = transformMove(m)
    mazeControl.getPacman match{
      case Some(PacmanObj.ImgObj(mx, my, _, _)) ⇒ {
        val (ix,iy) = (mx.toInt, my.toInt)
        val (x,y) = (tm, mazeControl.availableMoves(ix, iy).contains(tm)) match{
          case (Move.Down,true)  ⇒ (ix, iy + 1)
          case (Move.Up,true)    ⇒ (ix, iy - 1)
          case (Move.Right,true) ⇒ (ix + 1, iy)
          case (Move.Left,true)  ⇒ (ix - 1, iy)
          case _ ⇒ (ix, iy)}
        mazeControl.updatePacman(x,y,tm)
        mazeControl.updateUI()}
      case _ ⇒}}
  //Methods
  /**
   * Make N step forward
   * @param n - number of step
   */
  def forward(n:Int):Unit = movePacman(Move.Up, n)
  /**
   * Make N step backward
   * @param n - number of step
   */
  def backward(n:Int):Unit = movePacman(Move.Down, n)
  /**
   * Make N step left
   * @param n - number of step
   */
  def left(n:Int):Unit = movePacman(Move.Left, n)
  /**
   * Make N step right
   * @param n - number of step
   */
  def right(n:Int):Unit = movePacman(Move.Right, n)
  /**
   * Get current Pacman direction
   * @return - direction
   */
  def currentDirection:Direction = mazeControl.getPacman match{
    case Some(iObj) ⇒ iObj.mMove match{
      case Move.Up ⇒ Direction.Up
      case Move.Down ⇒ Direction.Down
      case Move.Left ⇒ Direction.Left
      case Move.Right ⇒ Direction.Right
      case _ ⇒ Direction.None}
    case _ ⇒ Direction.None}
  /**
   * Return list of objects, observed of Pacman from current position
   * @return - List of MazeObj
   */
  def currentLook:List[MazeObj] = mazeControl.getPacman match{
    case Some(iObj) ⇒ (iObj.mMove, iObj.mx.toInt, iObj.my.toInt) match{
      case (Move.Up,x,y) ⇒ {
        val objs = (y - 1 to 0 by -1)
          .map(sy ⇒ (sy, mazeObjAt(x,sy))).takeWhile{case (sy, d) ⇒ d != H && d != N}
          .toList.zipWithIndex.map{ case((sy,d),i) ⇒
          buildVisibleObj(x, sy, i + 1, isHorizontal = false) :+ DirectPath(i + 1)}
        (objs :+ List(DeadEnd(objs.size + 1))).flatMap(e ⇒ e)}
      case (Move.Down,x,y) ⇒ {
        val objs = (y + 1 until mazeControl.mazeM)
          .map(sy ⇒ (sy, mazeObjAt(x,sy))).takeWhile{case (sy, d) ⇒ d != H && d != N}
          .toList.zipWithIndex.map{ case((sy,d),i) ⇒
          buildVisibleObj(x, sy, i + 1, isHorizontal = false) :+ DirectPath(i + 1)}
        (objs :+ List(DeadEnd(objs.size + 1))).flatMap(e ⇒ e)}
      case (Move.Left,x,y) ⇒ {
        val objs = (x - 1 to 0 by -1)
          .map(sx ⇒ (sx, mazeObjAt(sx,y))).takeWhile{case (sx, d) ⇒ d != H && d != N}
          .toList.zipWithIndex.map{ case((sx,d),i) ⇒
          buildVisibleObj(sx, y, i + 1, isHorizontal = true) :+ DirectPath(i + 1)}
        (objs :+ List(DeadEnd(objs.size + 1))).flatMap(e ⇒ e)}
      case (Move.Right,x,y) ⇒ {
        val objs = (x + 1 until mazeControl.mazeN)
          .map(sx ⇒ (sx, mazeObjAt(sx,y))).takeWhile{case (sx, d) ⇒ d != H && d != N}
          .toList.zipWithIndex.map{ case((sx,d),i) ⇒
             buildVisibleObj(sx, y, i + 1, isHorizontal = true) :+ DirectPath(i + 1)}
        (objs :+ List(DeadEnd(objs.size + 1))).flatMap(e ⇒ e)}
      case _ ⇒ List()}
    case _ ⇒ List()}}
