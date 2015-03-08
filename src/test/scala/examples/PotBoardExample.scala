package examples
import mathact.ActBox
import mathact.pots.PotBoard


/**
 * Example of using PotBoard component
 * Created by CAB on 08.03.2015.
 */

object PotBoardExample extends ActBox{



  object MyPotBoard extends PotBoard{


    val a = 0

//    val a = init(.1) in(.2,.3)
//    val b = in(.4,.5)
//    var c = init(.6)
//    var d = zero
//
//
//    println(a)
//    println(b)
//    println(c)
//    println(d)

  }


  MyPotBoard.a



}
