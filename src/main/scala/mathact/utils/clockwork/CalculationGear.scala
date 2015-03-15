package mathact.utils.clockwork


/**
 * Gear for calculation tools (evaluate after CalculationGear)
 * updatePriority: -1 - not evaluate, 0 - first, 1 - second ...
 * Created by CAB on 15.03.2015.
 */

abstract class CalculationGear(val clockwork:Clockwork, val updatePriority:Int) extends Gear
