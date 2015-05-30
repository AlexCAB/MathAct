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
    val xColor = init(0)
    val xWeight = init(0)
    val w1Weight = init(.5)
    val w2Weight = init(.5)
    val xbEdgeColor = init(0)
  }
  //Creating  XYHistogram
  new SimpleStaticGraph{
    import variables._
    val a1 = node(name = "a1", color = blue) fixOn(10,10)
    val a2 = node(name = "a2", color = blue) fixOn(40,10)
    val x = node(name = "a2") color{if(xColor > 0) green else if(xColor < 0) red else gray} weight{xWeight}
    val b = node(name = "b", color = blue, weight = .5) fixOn(30,50)
    arrow(a1, x, name = "w1", color = black)  weight{w1Weight}
    arrow(a2, x, name = "w2", color = black)  weight{w2Weight}
    edge(x, b, weight = .8) color{if(xbEdgeColor > 0) green else if(xbEdgeColor < 0) red else gray}
  }
}
