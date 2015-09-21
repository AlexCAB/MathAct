package mathact.utils.definitions


/**
 * Direction
 * Created by CAB on 21.09.2015.
 */

trait Direction

object Direction {
  case object Up extends Direction
  case object Down extends Direction
  case object Left extends Direction
  case object Right extends Direction
  case object None extends Direction}
