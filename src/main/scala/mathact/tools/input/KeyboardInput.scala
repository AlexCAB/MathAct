package mathact.tools.input
import java.awt.event.KeyEvent
import java.awt.{KeyEventDispatcher, KeyboardFocusManager}
import mathact.utils.clockwork.CalculationGear
import mathact.utils.{Tool, Environment}


/**
 * Tool for read keyboard input.
 * Created by CAB on 09.09.2015.
 */

abstract class KeyboardInput(implicit environment:Environment) extends Tool{
  //Keys
  val ESC = 27
  val F1 = 112
  val F2 = 113
  val F3 = 114
  val F4 = 115
  val F5 = 116
  val F6 = 117
  val F7 = 118
  val F8 = 119
  val F9 = 120
  val F10 = 121
  val BACKTICK = 192
  val ONE = 49
  val TWO = 50
  val THREE = 51
  val FOUR = 52
  val FIVE = 53
  val SIX = 54
  val SEVEN = 55
  val EIGHT = 56
  val NINE = 57
  val ZERO = 48
  val MINUS = 45
  val EQUAL = 61
  val BACKSPACE = 8
  val TAB	= 9
  val Q = 81
  val W = 87
  val E = 69
  val R = 82
  val T = 84
  val Y = 89
  val U = 85
  val I = 73
  val O = 79
  val P = 80
  val LSB = 91
  val RSB = 93
  val BACKSLASH = 92
  val CAPSLOCK = 20
  val A = 65
  val S = 83
  val D = 68
  val F = 70
  val G = 71
  val H = 72
  val J = 74
  val K = 75
  val L = 76
  val SEMICOLON = 59
  val APOSTROPHE = 222
  val ENTER = 10
  val SHIFT = 16
  val Z = 90
  val X = 88
  val C = 67
  val V = 86
  val B = 66
  val N = 78
  val M = 77
  val COMA = 44
  val DOT = 46
  val SLASH = 47
  val UP = 38
  val CTRL = 17
  val ALT = 18
  val SPACE = 32
  val LEFT = 37
  val DOWN = 40
  val RIGHT = 39
  val INSERT = 155
  val DELETE = 127
  val HOME = 36
  val PAGEUP = 33
  val PAGEDOWN = 34
  val END = 35
  //Variables
  private var keyPressedProcs:List[PartialFunction[Int,Unit]] = List[PartialFunction[Int,Unit]]()
  private var keyReleasedProcs:List[PartialFunction[Int,Unit]] = List[PartialFunction[Int,Unit]]()
  //DSL Methods
  def keyPressed(proc: ⇒PartialFunction[Int,Unit]) = {keyPressedProcs +:= proc}
  def keyReleased(proc: ⇒PartialFunction[Int,Unit]) = {keyReleasedProcs +:= proc}
  //Install keyboard listener
  private val doNothing:PartialFunction[Int,Unit] = {case _ ⇒}
  KeyboardFocusManager.getCurrentKeyboardFocusManager.addKeyEventDispatcher(
    new KeyEventDispatcher{
      override def dispatchKeyEvent(e:KeyEvent):Boolean = {
        e.getID match{
          case KeyEvent.KEY_PRESSED ⇒ {
            keyPressedProcs.foreach(_.applyOrElse(e.getKeyCode, doNothing)); gear.changed()}
          case KeyEvent.KEY_RELEASED ⇒ {
            keyReleasedProcs.foreach(_.applyOrElse(e.getKeyCode, doNothing)); gear.changed()}
          case _ ⇒}
        false}})
  //Gear
  private val gear:CalculationGear = new CalculationGear(environment.clockwork, updatePriority = 1){
    def start() = {}
    def update() = {}
    def stop() = {}}}
