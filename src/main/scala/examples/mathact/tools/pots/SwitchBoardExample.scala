package examples.mathact.tools.pots
import mathact.tools.Workbench
import mathact.tools.doers.{Calc, Doer}
import mathact.tools.loggers.Logger
import mathact.tools.pots.SwitchBoard


/**
 * Example of using of SwitchBoard.
 * Created by CAB on 13.09.2015.
 */

object SwitchBoardExample extends Workbench{
  //Creating SwitchBoard
  val variables = new SwitchBoard("My Switch Board"){
    //Booleans
    val bool      =  init(true)               changed{ println("variables.bool value changed") }
    var boolVar   =  init(false)
    //Integers
    val int       =  init(1)                  in(1, 3)
    var intVar    =  init(2)                  options(2,4,7)
    //Doubles
    val double    =  init(1.0)                in(.5, 1.5, .5)
    var doubleVar =  init(2.0)                options(0, 2.0, 1.0)
    //Strings
    val optionsList = List("one","two","three","four","five")
    val string    =  init("c")                options ("a", "b", "c", "d", "e","f")
    var stringVar =  init(optionsList.head)   options optionsList
  }
  //Doer
  new Doer{ import variables._
    make{
      val i = optionsList.indexOf(stringVar)
      stringVar = optionsList(if(i < optionsList.size - 1) i + 1 else 0)
    }
  }
  //Logger
  val log = new Logger("Variables changes")
  //Calc
  new Calc{ import variables._
    make{
      log.blue(s"bool = $bool, int = $int, double = $double, string = $string")
      log.red(s"boolVar = $boolVar, intVar = $intVar, doubleVar = $doubleVar, stringVar = $stringVar")
    }
  }
}