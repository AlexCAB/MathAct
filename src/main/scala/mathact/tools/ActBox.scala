package mathact.tools
import mathact.utils.Environment
import mathact.utils.clockwork.Clockwork
import mathact.utils.ui.{Layout, Parameters}


/**
 * Box class for replacing acting gridRow
 * Created by CAB on 08.03.2015.
 */

abstract class ActBox(x:Int = 10, y:Int = 10, width:Int = Int.MaxValue, height:Int = Int.MaxValue){
  //Fields
  implicit val environment = Environment(new Clockwork,new Layout(x, y, width, height), new Parameters)
  //Methods
  def main(arg:Array[String]):Unit = environment.clockwork.start()}
