package examples.general
import mathact.tools.Workbench
import mathact.tools.doers.Doer
import mathact.tools.plots.YChartRecorder
import mathact.tools.pots.PotBoard
import scala.math._


/**
 * Oja's rule (http://en.wikipedia.org/wiki/Oja's_rule) example.
 * Created by CAB on 17.03.2015.
 */

object OjaRuleExample extends Workbench{
  //Variables
  val potBoard = new PotBoard{
    val xs = array(.1, .2)   //Input
    val η = in(0,1)}         //Learning rate
  import potBoard._
  var ws = Array(.1, .1)     //Weights
  var y = .0                 //Output
  //Calculation
  def calcOutput() = {
    y = 1 / (1 + exp( - xs.zip(ws).map{case(x,w) ⇒ x * w}.sum))
  }
  def calcWeights() = {
    ws = xs.zip(ws).map{case(x,w) ⇒ {w + η * (x * y - y * y * w)}}
  }
  //Chart
  val chart = new YChartRecorder{
    "Input_0" green(xs(0))
    "Input_1" navy(xs(1))
    "Weights_0" maroon(ws(0))
    "Weights_1" red(ws(1))
    "Output" black(y)
  }
  //Doer
  new Doer{make{
    calcOutput()
    calcWeights()
    chart.update()
  }}
}