package examples.mathact.tools.graphs
import mathact.tools.Workbench
import mathact.tools.graphs.SimpleStaticGraph
import mathact.tools.pots.PotBoard


/**
 * Example of using of SimpleStaticGraph visualisation tool.
 * Created by CAB on 29.05.2015.
 */

object SimpleStaticGraphExample extends Workbench{
  //Creating PotBoard with arrays xs and ys
  val variables = new PotBoard{
    val xColor =      init(0)   in(-1,+1)
    val xWeight =     init(.3)  in(0,10)
    val xVar =        init(0)   in(0,1)
    val w1Weight =    init(.2)  in(0,1)
    val w1Var =       init(1)   in(0,1)
    val w2Weight =    init(.2)  in(0,1)
    val xbEdgeColor = init(0)   in(-1,+1)
  }
  //Creating  XYHistogram
  new SimpleStaticGraph{
    import variables._
    val a1 = node(name = "a1", color = blue) fixOn(10,10)
    val a2 = node(name = "a2", color = blue) fixOn(40,10)
    val x = node(name = "x") color{colorBySign(xColor)} weight{xWeight} variable("xVar", {xVar})
    val b = node(name = "b", color = blue, weight = .5) fixOn(25,40)
    arrow(a1, x, name = "w1", color = black)  weight{w1Weight} variable("w1Var", {w1Var})
    arrow(a2, x, name = "w2", color = black)  weight("myWeight", {w2Weight})
    edge(x, b, weight = .1) color{colorBySign(xbEdgeColor)}
  }
}
