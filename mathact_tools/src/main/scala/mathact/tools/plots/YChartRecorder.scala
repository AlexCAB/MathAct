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

package mathact.tools.plots

import mathact.core.bricks.blocks.SketchContext
import mathact.core.bricks.plumbing.ObjFitting
import mathact.tools.Tool

import scala.concurrent.Future


/** Chart recorder by Y tool
  * Created by CAB on 08.05.2016.
  */

abstract class YChartRecorder(implicit context: SketchContext) extends Tool(context, "YChartRecorder") with ObjFitting{



  private[mathact] def blockImagePath = None


  val handler = new Outflow[Double] with Inflow[Double]{

    protected def drain(value: Double): Unit = {???}

  }




  val in = Inlet(handler)



  val out = Outlet(handler)




//  protected class Line(name: String) extends Inlet[Double]{   //В этом случае имеется один обработчик, к которому может быть подключено несколько флянцев
//    //Если нужно несколько обработчиков для разных типов, можно внутири Line создать несколько Inlet
//
//    protected def drain(v: Double): Unit = {
//
//      println("$$$$$$$$$$$$$$$$$$$$$ Handle: " + v)
//
//    }
//
//
//    def of(out: ⇒Plug[Double]): Socket[Double] = {
//
//
//
//      val in = Inlet(this)
//
//
//
//      println("RRRRRRRRRRRRRRRRRRRRRRR")
//
//
//      in.plug(out)
//
//      in
//
//
////      connect(() ⇒ in)      //!!! Этот метод должен возвращать что-то из чего можно будет получить последнее значений
//
//
//      //!!! Метод должен возврящать как раз Inlet, из которого можно забрать последнее (опциональное) занчение,
//      //либо параметризорованый функцией-обработчиком
//      //Варианты создания входа: 1) без обработчика (значение забирается в ручьную), 2) С функций обработчиком
//      // 3) С обьектом обработчиком (реализающим интерфейс Handler).
//
//
//
//
//    }
//
//  }
//
////  protected class Line2(name: String) extends Inlet[(Double, String)]{ //Пример с обработчиком с двумя значениями разного типа
////
////
////    protected def drain(v: (Double, String)): Unit = {
////
////
////      println("Handle: " + v)
////
////    }
////
////
////    def of(in1: ⇒Plug[Double], in2: ⇒Plug[String]): Unit = {
////
////
////      val s = Inlet(this, () ⇒ in1, () ⇒ in2)
////
////
////
////
////    }
////
////  }
//
//
//
//
//
//
//
//
//  def line(name: String) = new Line(name)    //DSL для более простого подключниея входя и создания подинстумента
//
//
//  //!!!Нужно API для получения последнего значения входа, для агрегирования значений с нескольких входов.
//
//
//
//
//
//  //??? Далее о том как реализовать внутреннюю мехнику передачи сообщений
//







}
