package prototyping
import java.lang.reflect.Field
import mathact.utils.dsl.SyntaxException
import scala.language.implicitConversions
import scala.reflect.{ClassTag,classTag}


/**
 * SwitchBoard prototyping.
 * Created by CAB on 13.09.2015.
 */

object SwitchBoardDSLProto extends App{
  println("==== SwitchBoardDSLProto ====")
  //
  abstract class SwitchBoard(){
    //Private definition
    private abstract class Switch
    private case class SwitchBool(
      init:Boolean, changed:()⇒Boolean = ()⇒false, field:Field = null) extends Switch
    private case class SwitchInt(
      init:Int, vs:List[Int] = List(), changed:()⇒Boolean = ()⇒false, field:Field = null) extends Switch
    private case class SwitchDouble(
      init:Double, vs:List[Double] = List(), changed:()⇒Boolean = ()⇒false, field:Field = null) extends Switch
    private case class SwitchString(
      init:String, vs:List[String] = List(), changed:()⇒Boolean = ()⇒false, field:Field = null) extends Switch
    //Variables
    private var switchs = List[Switch]()
    //Functions
    private def updateSwitch[T <: Switch : ClassTag](up:(T)⇒Switch):Unit = {
      switchs = switchs match{
      case s :: t if classTag[T].runtimeClass.isInstance(s) ⇒ up(s.asInstanceOf[T]) :: t
      case _ ⇒ throw new SyntaxException("Error: Incorrect operators sequence, 'init(<value>)' must be called first.")}}
    private def checkRange(begin:Double, end:Double, step:Double):Unit = {
      if(begin > end || step == 0){
        throw new SyntaxException(
          s"Error: Incorrect range must be begin($begin) <= end($end) and step($step) != 0.")}
      if((begin to end by step).size < 2){
        throw new SyntaxException(
          s"Error: Incorrect range size must be (begin($begin) to end($end) by step($step)).size >= 2.")}}
    private def checkOptions[T](ops:Seq[T], v:T):Unit = {
      if(! ops.contains(v)){
        throw new SyntaxException(
          s"Error: Options(${ops.toList}) must contain init value ($v).")}
      if(ops.size < 2){
        throw new SyntaxException(
          s"Error: Incorrect number of options: (${ops.size}) must be >= 2.")}}
    private def checkOnStart():Unit = {
      val existsEmpty = switchs.exists{
        case _:SwitchBool  ⇒ false
        case SwitchInt(_,vs,_,_) ⇒ vs.isEmpty
        case SwitchDouble(_,vs,_,_) ⇒ vs.isEmpty
        case SwitchString(_,vs,_,_) ⇒ vs.isEmpty}
      if(existsEmpty){
        throw new SyntaxException(
          "Error: All defined vars and vals must have a not empty list of options.")}}
    private def setFields():Unit = {
      val typeNames = List("boolean", "int", "double", "java.lang.String")
      val fields = getClass.getDeclaredFields.filter(f ⇒ typeNames.contains(f.getType.getName)).reverse
      switchs = switchs.zip(fields).map{
        case (s:SwitchBool,f) if f.getType.getName == typeNames(0) ⇒ SwitchBool(s.init,s.changed,f)
        case (s:SwitchInt,f) if f.getType.getName == typeNames(1) ⇒ SwitchInt(s.init, s.vs,s.changed,f)
        case (s:SwitchDouble,f) if f.getType.getName == typeNames(2) ⇒ SwitchDouble(s.init, s.vs,s.changed,f)
        case (s:SwitchString,f) if f.getType.getName == typeNames(3) ⇒ SwitchString(s.init, s.vs,s.changed,f)
        case (s,f) ⇒ throw new SyntaxException(s"Error:  Not match of field type ($f) and switch type ($s).")}}
    //DSL
    protected def init(v:Boolean):Boolean = {switchs +:= new SwitchBool(v); v}
    protected def init(v:Int):Int = {switchs +:= new SwitchInt(v); v}
    protected def init(v:Double):Double = {switchs +:= new SwitchDouble(v); v}
    protected def init(v:String):String = {switchs +:= new SwitchString(v); v}
    protected implicit class SwitchBoolMethods(v:Boolean){
      def changed(f: ⇒Unit):Boolean = {updateSwitch[SwitchBool]((s)⇒{SwitchBool(s.init,()⇒{f ;false})}); v}
      def changedWithUpdate(f: ⇒Unit):Boolean = {updateSwitch[SwitchBool]((s)⇒{SwitchBool(s.init,()⇒{f ;true})}); v}}
    protected implicit class SwitchIntMethods(v:Int){
      def in(begin:Int, end:Int, step:Int = 1):Int = {
        checkRange(begin, end, step)
        updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, (begin to end by step).toList, s.changed)}); v}
      def options(op1:Int, opn:Int*):Int = {
        checkOptions(op1 +: opn, v)
        updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, (op1 +: opn).toList, s.changed)}); v}
      def changed(f: ⇒Unit):Int = {
        updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, s.vs, ()⇒{f ;false})}); v}
      def changedWithUpdate(f: ⇒Unit):Int = {
        updateSwitch[SwitchInt]((s)⇒{SwitchInt(s.init, s.vs, ()⇒{f ;true})}); v}}
    protected implicit class SwitchDoubleMethods(v:Double){
      def in(begin:Double, end:Double, step:Double = 1):Double = {
        checkRange(begin, end, step)
        updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, (begin to end by step).toList, s.changed)}); v}
      def options(op1:Double, opn:Double*):Double = {
        checkOptions(op1 +: opn, v)
        updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, (op1 +: opn).toList, s.changed)}); v}
      def changed(f: ⇒Unit):Double = {
        updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, s.vs, ()⇒{f ;false})}); v}
      def changedWithUpdate(f: ⇒Unit):Double = {
        updateSwitch[SwitchDouble]((s)⇒{SwitchDouble(s.init, s.vs, ()⇒{f ;true})}); v}}
    protected implicit class SwitchStringMethods(v:String){
      def options(op1:String, opn:String*):String = {
        checkOptions(op1 +: opn, v)
        updateSwitch[SwitchString]((s)⇒{SwitchString(s.init, (op1 +: opn).toList, s.changed)}); v}
      def changed(f: ⇒Unit):String = {
        updateSwitch[SwitchString]((s)⇒{SwitchString(s.init, s.vs, ()⇒{f ;false})}); v}
      def changedWithUpdate(f: ⇒Unit):String = {
        updateSwitch[SwitchString]((s)⇒{SwitchString(s.init, s.vs, ()⇒{f ;true})}); v}}
    //Gear
    def start():Unit = {
      checkOnStart()
      setFields()
      println(switchBoard.switchs)
      switchBoard.switchs.last.asInstanceOf[switchBoard.SwitchBool].changed()
      //???
    }
  }
  //
  val switchBoard = new SwitchBoard{
    val bool =       init(true)   changed{println("changed")}
    var boolVar =    init(false)
    val int =        init(1)      in(1, 3)
    var intVar =     init(2)      options(2,4,7)
    val double =     init(1.0)    in(.5, 1.5, .5)
    var doubleVar =  init(2.0)    options(0, 2.0, 1.0)
    val string =     init("s")    options("d","c","s")
    var stringVar =  init("vs")   options("vs","fs")
    println(stringVar)
  }
  //
  switchBoard.start()
}
