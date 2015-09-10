package mathact.utils.definitions

/**
 * Makes Move constants locally accessible.
 * Created by CAB on 10.09.2015.
 */

trait Moves {
  type Move = mathact.utils.definitions.Move
  object Move{
    val Up = mathact.utils.definitions.Move.Up
    val Down = mathact.utils.definitions.Move.Down
    val Left = mathact.utils.definitions.Move.Left
    val Right = mathact.utils.definitions.Move.Right
    val Stay = mathact.utils.definitions.Move.Stay}}
