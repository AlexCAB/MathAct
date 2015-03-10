package examples
import mathact.ActBox
import mathact.pots.PotBoard


/**
 * Example of using PotBoard component
 * Created by CAB on 08.03.2015.
 */

object PotBoardExample extends ActBox{



  object MyPotBoard extends PotBoard{


    var a = init(.1)
    var aaaaaaaaaaaa = init(.11111111111111111111111111111111111111111111111)
    val b = in(.2,.3)
//    val b = in(.211111111111111111111111111111111111111111111,.31111111111111111111111111111111111111111111111111111111111111111)
    var c = init(.4) in(.4,.6)
    var e = zero
    val f = zero in(-.7,.8)
    val h = minimum(.9)
    var i = maximum(.10)
    val j = minimum(.11) maximum (.12)
    var k = init(.13) minimum(-.14)
    val l = init(.15) maximum(.16)
    var m = init(.17) minimum(-.18) maximum(.19)
    val n = init(.20) maximum(.21) minimum(-.22)

    val v1 = array(0,.1,.2,.3)
    val v2 = array len(4)
    val v3 = array of(0) len(4)
    val v4 = array(0,.1,.2,.3) in(-.5,.6)
    val v5 = array(0,.1,.2,.3) minimum(-.18) maximum(.3)

    println(a)
    println(b)
    println(c)
    println(e)
    println(f)
    println(h)
    println(i)
    println(j)
    println(k)
    println(l)
    println(m)
    println(n)

    println(v1.toList)
    println(v2.toList)
    println(v3.toList)
    println(v4.toList)
    println(v5.toList)







  }


  MyPotBoard.a



}
