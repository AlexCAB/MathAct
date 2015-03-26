package examples.mathact.tools.doers
import mathact.tools.Workbench
import mathact.tools.doers.Stepper
import mathact.tools.plots.YChartRecorder

/**
 * Example of using Stepper
 * Created by CAB on 26.03.2015.
 */

object StepperExample extends Workbench{
  //Variables
  var x = .0
  //Creating helpers tools
  new YChartRecorder(autoUpdate = true){green(x)}
  //Creating Doer
  new Stepper{
    "a" step{x = 0}
    step{x = .5}
    "c" step{x = 1}
  }
}
