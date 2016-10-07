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

package mathact.core.plumbing.fitting

import mathact.Application


/** Event receiver must be implemented by Inlet
  * Created by CAB on 17.05.2016.
  */

trait Socket[H] extends Flange[H] { _: InPipe[H] ⇒
//  //Get Inlet
//  private val inlet = this match{
//    case in: Inlet[T] ⇒ in
//    case _ ⇒ throw new Exception(
//      s"[Socket] This trait must be implemented only with mathact.core.plumbing.fitting.Inlet, " +
//      s"found implementation: ${this.getClass.getName}")}
  //Methods
  /** Connecting of this Socket to given Plug
    * @param plug - Plug[T] */
  def plug(plug: ⇒Plug[H]): Unit = pump.connect(()⇒plug, ()⇒this)







    //!!! Подумать как передать Socket (этот) и ⇒Plug в drive (через Pump).
    // Можно добавить дин общий метод принимающих их двоих.
    // И лутьше поднять до Inlet и Outlet
    // ! Для подключения нужно предать ID Inlet'а и ActorRef драйва Inlet'а, драйву Outlet'а (чтобы он рассылал
    // сообщения сообщения). Для чего в  Outlet должен быть специальный метод. Так же этот медод должне
    // возвращать ActorRef драйва Outlet'а и ID Outlet'а, их следует сохранить и использовтаь для посилки сообщений
    // об отключении (чтобы Outlet перестал рассылать сообщения на это Inlet), в случае завршения работы инструмента
    // например.
    // ! Продумать алгоритм отключения, чтобы избежать потеряных сообщенй.






    //!!! Далее здесь:
    // 1) Проверка действительно ли Socket реализован при помощи Inlet, если так продолжение работы.
    // 2) Поднятие типа до Inlet
    // 3) упаковка plug в функцию и отправка к Pump вместе с pipeId.












}
