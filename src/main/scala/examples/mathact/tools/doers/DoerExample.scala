package examples.mathact.tools.doers
import mathact.tools.Workbench
import mathact.tools.doers.Doer
import mathact.tools.plots.YChartRecorder
import scala.math._


/**
 * Example of using Doer tool.
 * Created by CAB on 17.03.2015.
 */

object DoerExample extends Workbench{
  //Variables
  var x = .0
  var y = .0
  //Creating helpers tools
  new YChartRecorder(autoUpdate = true){green(y)}      //<-- Use for recording of y value
  //Creating Doer
  new Doer{make{
    y = sin(x)                                         //<-- Will recalculate N times peer second
    x += .1
  }}
}