/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.tools.pots

import mathact.core.bricks.blocks.SketchContext
import mathact.core.bricks.plumbing.{OnStart, OnStop, ObjFitting}
import mathact.tools.Tool

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/** Pot board tool
  * Created by CAB on 08.05.2016.
  */

abstract class PotBoard(implicit context: SketchContext) extends Tool("PotBoard")(context) with OnStart with OnStop with ObjFitting{

  protected def onStart(): Unit = println("PotBoard.onStart")
  protected def onStop(): Unit = println("PotBoard.onStop")




   private class C extends Outlet[Double] with Inlet[Double]{

    protected def drain(value: Double): Unit = {???}

    def test(): Unit = ???

  }



  private val handler1 = new C

  private val handler2 = new Outlet[Double] with Inlet[Double]{

    protected def drain(value: Double): Unit = {???}

    def test(): Unit = ???

  }



  val in = Inlet(handler1)



  val out = Outlet(handler1)







//  var sender: Int⇒Unit = i ⇒ Unit
//
//  var doer: Future[Unit] = null
//
//
//  protected def onStart(): Unit = {
//
//    println("?????????????? onStart")
//
//
//    doer = Future{ for (i ← 0 to 10){
//
//      println("############ YChartRecorder send: " + i)
//
//      sender(i)
//
//
//      Thread.sleep(1000)
//
//    }
//
//    }
//
//
//  }
//
//
//
//  protected def onStop(): Unit = println("PotBoard.onStop")
//
//  protected class Pot(from: Double, to: Double, in: Option[()⇒Plug[Double]]) extends Outlet[Double] with Inlet[Double]{    //Определение выхода
//
//    protected def drain(value: Double): Unit = {}// pour(value)    //Все полученые из in значения будут нередаватся на выход
//
//
////    in.foreach(e ⇒ Inlet(this, e))       //Эамыкание входа на выход
////
////    in.foreach(e ⇒ new Inlet[Double]{    //Констуирование отдельного вхда
////
////      protected def drain(value: Double): Unit = { ??? }
////
////
////
////    })  //Эамыкание входа на выход
//
//    def test(): Unit = ???
//
//    //Здесь реализация
//
//
//
//    //TODO
//    //TODO Подумать можно ли както заинжектить OutPipe в этот класс до того как будет выполнен этот конструктор
//    //TODO (например при конструировании предка (Outlet или Inlet)), это бы позволило отправляьт сообщения
//    //TODO стартовые сразу от сюдова как например в строке ниже (они будут поставлены в лист ожидания).
//    //TODO
////    pour(10)       //Посылка значения на выход
//
//
//    sender = i ⇒ {pour(i)}   // <--- !!!
//
////    Future{
////      (0 to 5).foreach{ i ⇒
////        println("############# pour: " + i)
////        try{
////          pour(i)}
////        catch{ case t: Throwable ⇒
////          t.printStackTrace()}
////      Thread.sleep(1000)}
////    }
//
//
//
//
//  }
//
//
//
//  def pot(from: Double, to: Double) = Outlet(new Pot(from, from, None))   //DSL для удобного создания выхода (Возвращат Plug)
//
//
////  def pot(in: ⇒Plug[Double]) = Outlet(new Pot(1,2, Some(()⇒in)))   //DSL для удобного создания выхода




}
