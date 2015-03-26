package examples.mathact.tools.pots
import mathact.tools.Workbench
import mathact.tools.plots.YChartRecorder
import mathact.tools.pots.PotBoard


/**
 * Example of using PotBoard tool.
 * Created by CAB on 17.03.2015.
 */

object PotBoardExample extends Workbench{
  //Functions
  def norm(a:Double,b:Double,c:Double):(Double,Double) = {
    val s = b + c
    val r = 1 - a
    if(s != 0) ((b / s) * r, (c / s) * r) else (r / 2, r / 2)}
  //Creating PotBoard
  val variables = new PotBoard{
    //Simple values
    val a = init(.1)
    val b = init(-.2) in(-5,+5)
    //Sum of x,y,z be always equal 1
    var x:Double = init(.2) in(0,1) changedWithUpdate(x ⇒ {val (a,b) = norm(x,y,z); y = a; z = b})
    var y:Double = init(.4) in(0,1) changedWithUpdate(y ⇒ {val (a,b) = norm(y,x,z); x = a; z = b})
    var z:Double = init(.4) in(0,1) changedWithUpdate(z ⇒ {val (a,b) = norm(z,x,y); x = a; y = b})
    //Arrays
    val a1 = array(0,.1,.2)
    val a2 = array len(4)
    val a3 = array of(0) len(4)
    val a4 = array(0,.1,.2,.3) in(-.5,.6)
    val a5 = array(0,.1,.2,.3) minimum(-.18) maximum(.3)
  }
  //Creating YChartRecorder
  new YChartRecorder(autoUpdate = true){
    import variables._
    green(x)
    red(y)
    blue(z)
    black(a1(0))
  }
}
