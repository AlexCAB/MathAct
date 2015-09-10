package mathact.utils.definitions


/**
 * Moves.
 * Created by CAB on 09.09.2015.
 */

trait Move

object Move {
  case object Up extends Move
  case object Down extends Move
  case object Left extends Move
  case object Right extends Move
  case object Stay extends Move}



