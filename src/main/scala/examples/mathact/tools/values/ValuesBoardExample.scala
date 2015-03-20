package examples.mathact.tools.values
import mathact.tools.Workbench
import mathact.tools.pots.PotBoard
import mathact.tools.values.ValuesBoard


/**
 * Example of using ValuesBoard tool.
 * Created by CAB on 17.03.2015.
 */

object ValuesBoardExample extends Workbench{
  //Creating PotBoard
  val variables = new PotBoard{
    val a = zero
    val b = init(1)
  }
  //Creating ValuesBoard
  new ValuesBoard("MyVals"){
    import variables._
    "a" red(a)
    "b" green(b)
  }
}