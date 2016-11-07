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

package mathact.playing.actors

import akka.actor.SupervisorStrategy.Stop
import akka.actor._


/** Playing with actor termination
  * Created by CAB on 19.10.2016.
  */

object Termination extends App {
  println("==== Termination ====")
  //Helpers
  abstract class ActorBase extends Actor{
    def reaction: PartialFunction[Any, Unit]
    def receive: PartialFunction[Any, Unit] = { case m ⇒
      println(s"ACTOR: ${this.getClass.getTypeName}, MESSAGE: $m FROM: $sender")
      reaction.applyOrElse[Any, Unit](m, _ ⇒ println(s"NOT HANDLED: $m"))}}
  //Messages
  case object Terminate
  case object Throw
  //System
  val system = ActorSystem.create("Termination")
  //Actors
  class D extends ActorBase{
    override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Stop}
    override def postStop(): Unit = {
      println("[D] postStop")
      Thread.sleep(1000)}
//    throw new Exception("Oops! D")
    def reaction = {
      case m ⇒ println("[D] m: " + m)}}
  class C extends ActorBase{
    override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Stop}
    override def postStop(): Unit = {
      println("[C] postStop")
      Thread.sleep(1000)}
    val d = context.actorOf(Props(new D), "D")
    context.watch(d)
//    throw new Exception("Oops! C")
    def reaction = {
      //
      case Terminate ⇒ self ! PoisonPill
      case Throw ⇒  throw new Exception("Throw C")
      //
      case _: Terminated ⇒ self ! PoisonPill
      case m ⇒ println("[C] m: " + m)}}
  class B extends ActorBase{
    override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Stop}
    override def postStop(): Unit = {
      println("[B] postStop")
      Thread.sleep(1000)}
    val c = context.actorOf(Props(new C), "C")
    context.watch(c)
//    throw new Exception("Oops! B")
    def reaction = {
      //
      case Terminate ⇒ c ! Terminate
      case Throw ⇒ c ! Throw
      //
      case _: Terminated ⇒ self ! PoisonPill
      case m ⇒ println("[B] m: " + m)}}
  class A extends ActorBase{
    override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Stop}
    override def postStop(): Unit = {
      println("[A] postStop")
      Thread.sleep(1000)}
    val b = context.actorOf(Props(new B), "B")
    context.watch(b)
//    throw new Exception("Oops! A")
    def reaction = {
      //
      case Terminate ⇒ b ! Terminate
      case Throw ⇒ b ! Throw
      //
      case _: Terminated ⇒ self ! PoisonPill
      case m ⇒ println("[A] m: " + m)}}
  //Create
  val a = system.actorOf(Props(new A), "A")
  //Terminate
  //
//  Thread.sleep(2000)
//  println("a ! Terminate")
//  a ! Terminate
  //
//  Thread.sleep(2000)
//  println("a ! Throw")
//  a ! Throw
  //
  Thread.sleep(2000)
  println("a ! PoisonPill")
  a ! PoisonPill
  //
  //Stop
  Thread.sleep(5000)
  system.terminate()}
