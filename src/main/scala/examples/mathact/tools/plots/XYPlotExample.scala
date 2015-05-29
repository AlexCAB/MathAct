package examples.mathact.tools.plots
import mathact.tools.Workbench
import mathact.tools.plots.XYPlot
import mathact.tools.pots.PotBoard
import math._


/**
 * Example of using XYPlot tool.
 * Created by CAB on 17.03.2015.
 */

object XYPlotExample extends Workbench{
  //Creating PotBoard with arrays xs and ys
  val variables = new PotBoard{
    val xs = array of(random) len(6)
    val ys = array of(random) len(6)
  }
  //Creating XYPlot
  new XYPlot{
    import variables._
    line(name = "Red line", color = red) of{(xs.take(3), ys.take(3))}
    line(name = "Green line", color = green) of{(xs.drop(3), ys.drop(3))}
  }
}
