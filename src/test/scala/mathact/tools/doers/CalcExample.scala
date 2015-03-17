package mathact.tools.doers
import mathact.tools.Workbench
import mathact.tools.pots.PotBoard


/**
 * Example of using Calc tool.
 * Created by CAB on 17.03.2015.
 */

object CalcExample extends Workbench{
  //Creating PotBoard with variables a, b and c
  val variables = new PotBoard{
    val a = in(-1,1)
    val b = in(-1,1)
    var c = in(-2,2)
  }
  //Creating Calc
  new Calc{make{
    import variables._
    c = a + b                             //<--  Will be recalculate by each update event
  }}
}
