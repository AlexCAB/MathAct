package examples.mathact.tools.games
import mathact.tools.Workbench
import mathact.tools.games.SimpleClassicPacman
import mathact.tools.input.KeyboardInput
import math.random


/**
 * Simple classic Pacman game.
 * Created by CAB on 09.09.2015.
 */

object SimpleClassicPacmanExample extends Workbench{
  //Variables
  var direction = 0
  //Keyboard input for the pacman control.
  new KeyboardInput{
    keyPressed{
      case UP    ⇒ {direction = 1}
      case DOWN  ⇒ {direction = 2}
      case LEFT  ⇒ {direction = 3}
      case RIGHT ⇒ {direction = 4}
    }
  }
  //Game instance
  new SimpleClassicPacman("Pacman example"){
    //Maze
    maze(█,█,█,█,█,█,█,█,█,█,█,█,█,█,█,█,⏎,   //ᗧ | C - Pacman
         █,ᗧ,○,○,○,○,○,○,█,○,○,○,○,○,○,█,⏎,   //B     - Blinky
         █,█,○,█,█,○,█,█,█,○,█,█,○,█,○,█,⏎,   //I     - Inky
         █,○,○,●,█,○,○,○,○,○,○,○,○,█,○,█,⏎,   //○ | o - Pellet
         █,○,█,█,█,█,█,○,█,█,○,█,○,█,○,█,⏎,   //● | P - Power pellet
         █,○,○,○,○,█,B,▒,I,█,○,█,●,█,○,█,⏎,   //█ | H - Wall
         █,○,█,█,○,█,█,█,█,█,○,█,█,█,○,█,⏎,   //▒ | E - Empty space
         █,○,○,○,○,○,○,○,○,○,○,○,○,○,○,█,⏎,   //⏎ | R - New line
         █,█,█,█,█,█,█,█,█,█,█,█,█,█,█,█)
    //Pacman function
    pacmanFunction{(x, y, t, s, availableMoves, prevMove) ⇒ direction match{
      case 0 ⇒ Move.Stay
      case 1 ⇒ Move.Up
      case 2 ⇒ Move.Down
      case 3 ⇒ Move.Left
      case 4 ⇒ Move.Right
    }}
    //Ghost function (same fof both ghost)
    def ghostFun(x:Int, y:Int, t:Long, s:AgentState, availableMoves:Set[Move], prevMove:Move):Move = {
      (prevMove, availableMoves.filter(_ != Move.Stay)) match{
        case (_, ms) if ms.size == 1 ⇒ ms.head
        case (pm, ms) if ms.size == 2 && ms.contains(pm) ⇒ pm
        case (pm, ms) ⇒ {
          val rpm = pm match{
            case Move.Up ⇒ Move.Down
            case Move.Down ⇒ Move.Up
            case Move.Left ⇒ Move.Right
            case Move.Right ⇒ Move.Left
            case m ⇒ m}
          ms.filter(_ != rpm).toList((random * ms.size - 1).toInt)
        }
      }
    }
    //Set ghost function for Blinky
    blinkyFunction(ghostFun)
    //Set ghost function for Inky
    inkyFunction(ghostFun)
  }
}
