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

package mathact.core.bricks

import mathact.core.app.Application


/** Box class for placing of blocks
  * Created by CAB on 09.05.2016.
  */

class Workbench extends WorkbenchLike{ //extends Fitting{
  //Get of SketchContext
  protected implicit val context = Application.getSketchContext(this)


  //!!! Workbench должен зарегистировать себя и получить свой собсвенный controller и plumbing





  //Здесь нижно получить environment из сервиса Applicati, если уже зарегестироват, логировать оштбку




}


//
//  extends Fitting{
//
//
//  //Environment должен констрироватся до того как будетсоздан хоть один инструмент (т.е. самый первый при старте программы),
//  //так как Environment содержыт все служебные обьекты и сервисы (как например ActorSystem)
//
////  protected implicit val environment = new Environment
////
////  private[mathact] val pump: Pump = new Pump(environment, this, "WorkbenchPump")
////
////
////
////  def terminate(): Unit = ???      //Разрушает инструмент или соединетель, предварительно отключив все трубы
////
////
////
////
////
////
////
////
////  def main(arg:Array[String]):Unit = {
////    //Starting of main controller
////    environment.controller ! M.MainControllerStart
////
////
////     //До вызова этого метода акторы могут обмениватся только конструкционными сообщениями (NewDrive, NewImpeller)
////
////
////
////  }
//
//
//    //Далее: работа над UI Workbench (запуск, и пошаговое выполение приложения)
//
//    //Нету готового решение для конкурентного интерфейса, прийдётся делать что-то своё.
//
//    //Полезный метод: Swing.onEDT  --> http://stackoverflow.com/questions/32355872/gui-for-akka-application
//
//
//    //Scala-swing заброшена, придётся использовать scalafx, нужно разобратся как создать несколько окон и интегрировать
//    //с AKKA.
//
//
//}