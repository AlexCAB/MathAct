package examples.mathact.tools.input
import mathact.tools.Workbench
import mathact.tools.input.KeyboardInput
import mathact.tools.values.ValuesBoard

/**
 * Example of using KeyboardInput tool.
 * Created by CAB on 09.09.2015.
 */

object KeyboardInputExample extends Workbench{
  //Variable
  var x = 0.0
  //Keyboard input
  new KeyboardInput{
    keyPressed{
      case UP   ⇒ x += 1
      case DOWN ⇒ x -= 1
    }
    keyReleased{
      case ESC ⇒ environment.clockwork.stop(0)
    }
  }
  //Values board
  new ValuesBoard("MyVal"){
    value(name = "x") of{x}
  }
}
