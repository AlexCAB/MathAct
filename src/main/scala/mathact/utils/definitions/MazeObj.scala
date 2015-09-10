package mathact.utils.definitions


/**
 * Objects used in Pacman maze.
 * x and y in columns and lines of maze
 * Created by CAB on 09.09.2015.
 */

trait MazeObj

object MazeObj {
  case class Pacman0(x:Double, y:Double, move:Move) extends MazeObj
  case class Pacman1(x:Double, y:Double, move:Move) extends MazeObj
  case class Pacman2(x:Double, y:Double, move:Move) extends MazeObj
  case class Pellet(x:Double, y:Double) extends MazeObj
  case class PowerPellet(x:Double, y:Double) extends MazeObj
  case class BlueGhost(x:Double, y:Double, move:Move) extends MazeObj
  case class RedGhost(x:Double, y:Double, move:Move) extends MazeObj
  case class WhiteGhost(x:Double, y:Double, move:Move) extends MazeObj
  case object Empty extends MazeObj
}
