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
  new YChartRecorder(autoUpdate = true){trace(color =green) of(x)}
  //Creating Stepper
  new Stepper{
    onStart{println("Stepper started")}
    step(name = "a") make{x = 0}
    step() make{x = .5}
    step(name = "c") make{x = 1}
    onStop{println("Stepper stopped")}
  }
}
