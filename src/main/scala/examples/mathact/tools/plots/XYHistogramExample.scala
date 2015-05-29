package examples.mathact.tools.plots
import mathact.tools.Workbench
import mathact.tools.plots.XYHistogram
import mathact.tools.pots.PotBoard


/**
 * Example of using XYHistogram tool.
 * Created by CAB on 17.03.2015.
 */

object XYHistogramExample extends Workbench{
  //Creating PotBoard with arrays xs and ys
  val variables = new PotBoard{
    val x = init(-.5)
    val y = init(-.2)
    val xs = array(-1,.0,.5,1)
    val ys = array(.8,.6,.4,.2)
  }
  //Creating  XYHistogram
  new XYHistogram{
    import variables._
    data(name = "a", color = red) of{(x, y)}
    data(name = "b", color = red) ofArray{(xs.take(2), ys.take(2))}
    data(color = green) ofArray{(xs.drop(2), ys.drop(2))}
  }
}