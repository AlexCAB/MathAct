package examples.mathact.tools.games
import mathact.tools.Workbench
import mathact.tools.games.SimpleClassicPacman


/**
 * Simple classic Pacman game.
 * Created by CAB on 09.09.2015.
 */

object SimpleClassicPacmanExample extends Workbench{



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


//    maze(█,█,█,⏎,
//         █,o,█,⏎,
//         █,█,█)






  }

}
