package mathact.tools.plots
import mathact.tools.Workbench
import mathact.tools.pots.PotBoard


/**
 * Example of using YHistogram tool.
 * Created by CAB on 17.03.2015.
 */

object YHistogramExample extends Workbench{
 //Creating PotBoard
  val variables = new PotBoard{
    val a = init(.1)
    val b = init(-.2)
    val ys = array(.8,.6,.4,.2)
  }
  //Creating  YHistogram
  new YHistogram{
    import variables._
    red{a}
    green{b}
    blackArray{ys}
  }
}
