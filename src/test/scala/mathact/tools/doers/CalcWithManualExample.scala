package mathact.tools.doers
import mathact.tools.Workbench
import mathact.tools.plots.YChartRecorder
import math._


/**
 * Example of using CalcWithManualExample tool.
 * Created by CAB on 17.03.2015.
 */

object CalcWithManualExample extends Workbench{
  //Variables
  var y = .0
  //Creating helpers tools
  new Doer{}                                           //<-- Use Doer as update event generator
  new YChartRecorder(autoUpdate = true){green(y)}      //<-- Use for recording of y value
  //Create CalcWithManual
  new CalcWithManual{
    auto{x ⇒
      y = random + x                                   //<-- Evaluate in auto mode
    }
    manual{x ⇒
      y = x                                            //<-- Evaluate in manual mode
    }
  }
}
