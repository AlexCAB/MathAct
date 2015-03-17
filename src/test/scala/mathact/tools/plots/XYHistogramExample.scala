package mathact.tools.plots
import mathact.tools.Workbench
import mathact.tools.pots.PotBoard


/**
 * Example of using XYHistogram tool.
 * Created by CAB on 17.03.2015.
 */

object XYHistogramExample extends Workbench{
  //Creating PotBoard with arrays xs and ys
  val variables = new PotBoard{
    val xs = array(-1,.0,.5,1)
    val ys = array(.8,.6,.4,.2)
  }
  //Creating  XYHistogram
  new XYHistogram{
    import variables._
    red{(xs.take(2), ys.take(2))}
    green{(xs.drop(2), ys.drop(2))}
  }
}