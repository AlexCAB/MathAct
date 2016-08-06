package examples.mathact.tools.values
import mathact.tools.Workbench
import mathact.tools.doers.Doer
import mathact.tools.pots.{PotBoard, SwitchBoard}
import mathact.tools.values.ValuesBoard


/**
 * Example of using ValuesBoard tool.
 * Created by CAB on 17.03.2015.
 */

object ValuesBoardExample extends Workbench{
  //Creating PotBoard
  val variables = new PotBoard("Variables"){
    val a = zero
    val b = init(1)
  }
  //Creating SwitchBoard
  val options = new SwitchBoard("Options"){
    val bool      =  init(true)
    val int       =  init(1)      in(1, 3)
    val double    =  init(1.0)    in(.5, 1.5, .5)
    val string    =  init("one")  options ("one","two","three","four","five")
  }
  //Creating ValuesBoard
  val values = new ValuesBoard("MyVals"){
    //From defined
    var bool      = true
    var int       = 0
    var double    = 0.0
    var string    = "a"
    //From variables
    value(name = "a", color = red)   of{variables.a}
    value(name = "b", color = green) of{variables.b}
    //From options
    value(name = "bool")   ofBoolean{options.bool}
    value(name = "int")    ofInt{options.int}
    value(name = "double") ofDouble{options.double}
    value(name = "string") ofString{options.string}
  }
  //Doer
  new Doer{make{
    //Update values
    values.bool = ! values.bool
    values.int += 1
    values.double += .5
    values.string  = values.string match{case "a" ⇒ "b"; case "b" ⇒ "c";case "c" ⇒ "a"}
  }}
}
