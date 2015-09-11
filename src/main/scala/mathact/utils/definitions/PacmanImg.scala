package mathact.utils.definitions

/**
 * Types of images/
 * Created by CAB on 11.09.2015.
 */

trait PacmanImg

object PacmanImg{
  case object EmptyImg extends PacmanImg
  case object WhiteGhost extends PacmanImg
  case object RedGhost extends PacmanImg
  case object BlueGhost extends PacmanImg
  case object Pacman0 extends PacmanImg
  case object Pacman1 extends PacmanImg
  case object Pacman2 extends PacmanImg}
