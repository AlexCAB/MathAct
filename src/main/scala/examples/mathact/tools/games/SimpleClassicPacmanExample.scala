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

  var direction = 0



  new KeyboardInput{
    keyPressed{
      case UP    ⇒ {direction = 1}
      case DOWN  ⇒ {direction = 2}
      case LEFT  ⇒ {direction = 3}
      case RIGHT ⇒ {direction = 4}
    }

  }


  new SimpleClassicPacman("Pacman example"){

    maze(█,█,█,█,█,█,█,█,█,█,█,█,█,█,█,█,⏎,
         █,ᗧ,○,○,○,○,○,○,█,○,○,○,○,○,○,█,⏎,
         █,█,○,█,█,○,█,█,█,○,█,█,○,█,○,█,⏎,
         █,○,○,●,█,○,○,○,○,○,○,○,○,█,○,█,⏎,
         █,○,█,█,█,█,█,○,█,█,○,█,○,█,○,█,⏎,
         █,○,○,○,○,█,B,▒,I,█,○,█,●,█,○,█,⏎,
         █,○,█,█,○,█,█,█,█,█,○,█,█,█,○,█,⏎,
         █,○,○,○,○,○,○,○,○,○,○,○,○,○,○,█,⏎,
         █,█,█,█,█,█,█,█,█,█,█,█,█,█,█,█)

    pacmanFunction{(x,y,t,availableMoves,prevMove) ⇒
      val m = direction match{
        case 0 ⇒ Move.Stay
        case 1 ⇒ Move.Up
        case 2 ⇒ Move.Down
        case 3 ⇒ Move.Left
        case 4 ⇒ Move.Right}
      direction = 0
      m
    }

    def ghostFun(x:Int,y:Int,t:Long,availableMoves:Set[Move],prevMove:Move):Move = {
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
          ms.filter(_ != rpm).toList((random * ms.size - 1).toInt)}}
    }

    blinkyFunction(ghostFun)

    inkyFunction(ghostFun)






  }

}
