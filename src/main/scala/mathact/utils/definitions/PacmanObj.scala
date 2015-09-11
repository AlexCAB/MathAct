package mathact.utils.definitions


/**
 * Objects used in Pacman maze.
 * x and y in columns and lines of maze
 * Created by CAB on 09.09.2015.
 */

abstract class PacmanObj(ox:Double, oy:Double)

object PacmanObj {
  case class Pellet(x:Double, y:Double) extends PacmanObj(x,y)
  case class PowerPellet(x:Double, y:Double) extends PacmanObj(x,y)
  object ImgObj{def unapply(o:ImgObj):Option[(Double,Double,Move,PacmanImg)] = Some((o.mx,o.my,o.mMove,o.mImg))}
  abstract class ImgObj(val mx:Double, val my:Double, val mMove:Move,val mImg:PacmanImg) extends PacmanObj(mx,my)}
