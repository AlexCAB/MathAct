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

package examples

import mathact.core.app.Application
import mathact.core.bricks.Workbench
import mathact.tools.plots.YChartRecorder
import mathact.tools.pots.PotBoard


/** Chart end pot example
  * Created by CAB on 08.05.2016.
  */

class ChartPotExample extends Workbench{




  //! Workbench это корень приложения, не стоит разрешать чтобы они общялись между собой, но сотоит допустить
  //возможность существоания нескольких его реализаций


  println("ChartPotExample init")





//  new Workbench{
//
//  println("Workbench init")
//
//  }
//}




//{ new Workbench{
//
//
//
//  println("ChartPotExample")
//
//
//
//
//}}




//  extends App {
//
//
//
//
//
//
  val pots = new PotBoard{      //Создание компонента с выходвми

//    val pot1 = Outlet(new Pot(1,2, None))    //Регистрация выхода

    val pot2 = pot(2,3)               //егистрация выхода, вариант с DSL

//  pot2.testVal


//    pot(chart.out1)                  //Рекурсивное связание




  }
//
//
//
//  val chart: YChartRecorder{val out1 : Outlet[Double]} = new YChartRecorder{    //Создание компоненеа с входами и выходом
  val chart = new YChartRecorder{    //Создание компоненеа с входами
//
//
//
    line("line1").of(pots.pot2)    //Регистрация вход c DSL
//
//
//
//
//
////    val col1 = Collect(pots.pot1, pots.pot2) // Пример коллектора из нескольких выходов нв один
////
////
////    line("line1").of(col1)    //Работа так же как и с обычным выходом
////
////
////
//    val out1 = Outlet(new Outlet[Double]{})
//
//
//
//
  }
}
