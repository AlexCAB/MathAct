package mathact.tools.doers
import mathact.utils.clockwork.CalculationGear
import mathact.utils.{Tool, Environment}


/**
 * Simple calculation tool which recalc by update event.
 * Created by CAB on 15.03.2015.
 */

abstract class Calc(implicit environment:Environment)extends Tool{
  //Variables
  private var procs:List[()⇒Unit] = List[()⇒Unit]()
  //DSL Methods
  def make(proc: ⇒Unit) = {procs +:= (()⇒proc)}
  //Gear
  private val gear:CalculationGear = new CalculationGear(environment.clockwork, updatePriority = 1){
    def start() = {}
    def update() = {
      procs.foreach(p ⇒ p())}
    def stop() = {}}}
