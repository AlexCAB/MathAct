package examples
import mathact.tools.ActBox
import mathact.tools.doers.{Calc, Doer}
import mathact.tools.loggers.Logger
import mathact.tools.plots.{Histogram, XYPlot, XTracer}
import mathact.tools.pots.PotBoard


/**
 * Example of using PotBoard component
 * Created by CAB on 08.03.2015.
 */

object PotBoardExample extends ActBox{



  object MyPotBoard extends PotBoard {


    var a = init(.1)
    var aaaaaaaaaaaa = init(.11111111111111111111111111111111111111111111111)
    val b = in(-1,1)
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


  new Histogram("my histogram"){
    black(MyPotBoard.a)
    red(MyPotBoard.b)
    greenArray(MyPotBoard.v1)
    blueArray(MyPotBoard.v2)







  }

//  new Calc{
//
//
//    make{
//
//      println(1)
//
//      MyPotBoard.c += .1
//
//      println(MyPotBoard.c)
//
//    }
//  }


//
//
  MyPotBoard.a
//
//
//
//
//  var i = 0
//   new Doer{
//     make{
//
//       MyLogger.log("Hi" + i)
////       MyLogger.red("Hi" + i)
////       MyLogger.green("Hi" + i)
//       i += 1
////       println("ssssss")
////       MyPotBoard.a += .002
//
//   }}
//
//   object MyLogger extends Logger


//
//
//
//
//
//
//  new XTracer{
//
//    black{x ⇒
//
//
////      println("trace")
//
//
//    x * x + MyPotBoard.b
//
//
//    }
//    "test" red {x ⇒
//
//
////      println("trace")
//
//
//      -x
//
//
//    }
//
//  }


//  new XYPlot{
//
//    black{((0 to MyPotBoard.v1.size).map(_.toDouble).toArray, MyPotBoard.v1)}
//    "test" red{((0 to MyPotBoard.v2.size).map(_.toDouble).toArray, MyPotBoard.v2)}
//
//
//
//
//  }




}
