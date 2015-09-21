package examples.mathact.tools.games.pacman
import mathact.tools.Workbench
import mathact.tools.games.pacman.{PacmanControlMethods, Pacman}
import mathact.tools.input.KeyboardInput
import mathact.tools.loggers.Logger


/**
 * Example of using Pacman with PacmanControlMethods
 * Created by CAB on 21.09.2015.
 */

object PacmanWithControlMethodsExample extends Workbench{
  //Logger
  val log = new Logger
  //Keyboard input for the pacman control.
  new KeyboardInput{
    //Functions
    private def logDirectionAndLook():Unit = {
      log.red("Current direction = " + pacman.currentDirection)
      log.white("Current look:")
      pacman.currentLook.foreach(l ⇒ log.white("  " + l))
    }
    //Handlers
    keyPressed{
      case UP    ⇒ {
        pacman.forward(1)
        logDirectionAndLook()
      }
      case DOWN  ⇒ {
        pacman.backward(1)
        logDirectionAndLook()
      }
      case LEFT  ⇒ {
        pacman.left(1)
        logDirectionAndLook()
      }
      case RIGHT ⇒ {
        pacman.right(1)
        logDirectionAndLook()
      }
    }
  }
  //Game instance
  val pacman = new Pacman("Pacman example") with PacmanControlMethods{
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
  }
}
