package examples.mathact.tools.games
import mathact.tools.Workbench
import mathact.tools.games.PacmanView
import mathact.tools.pots.PotBoard


/**
 * Example of using PacmanView.
 * Created by CAB on 12.09.2015.
 */

object PacmanViewExample  extends Workbench{
  //Pot board
  val coordinates = new PotBoard("Coordinates"){
    val pacmanX = init(4) in(1,7)
    val pacmanY = init(1) in(1,3)
    val blinkyX = init(2) in(1,7)
    val blinkyY = init(3) in(1,3)
    val inkyX   = init(6) in(1,7)
    val inkyY   = init(3) in(1,3)
  }
  //Pacman view instance
  new PacmanView("Pacman view"){
    //Maze
    maze(█,█,█,█,█,█,█,█,█,⏎, //○ | o - Pellet
         █,█,█,█,▒,█,█,█,█,⏎, //● | P - Power pellet
         █,█,█,█,▒,█,█,█,█,⏎, //█ | H - Wall
         █,○,▒,▒,▒,▒,▒,○,█,⏎, //▒ | E - Empty space
         █,█,█,█,█,█,█,█,█)    //⏎ | R - New line
    //Pacman and ghosts coordinates
    pacmanOf{(coordinates.pacmanX,coordinates.pacmanY)}
    blinkyOf{(coordinates.blinkyX,coordinates.blinkyY)}
    inkyOf{(coordinates.inkyX,coordinates.inkyY)}
  }
}