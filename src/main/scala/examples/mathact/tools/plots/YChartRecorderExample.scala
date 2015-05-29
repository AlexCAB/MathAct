package examples.mathact.tools.plots
import mathact.tools.Workbench
import mathact.tools.doers.Doer
import mathact.tools.plots.YChartRecorder
import mathact.tools.pots.PotBoard


/**
 * Example of using YChartRecorder tool.
 * Created by CAB on 17.03.2015.
 */

object YChartRecorderExample extends Workbench{
  //Creating helpers tools
  val variables = new PotBoard{
    val a = init(.1)
    val b = init(-.2)
  }
  new Doer{make{MyChart.update()}}                     //<-- Use Doer for make update (autoUpdate = false)
  //Creating YChartRecorder
  object MyChart extends YChartRecorder{
    import variables._
    trace(name = "Red trace", color = red) of{a}
    trace(name = "Green trace", color = green) of{b}
  }
}
