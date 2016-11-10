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

package mathact.core

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

import scala.reflect._


/** Base class for testing of infrastructure
  * Created by CAB on 12.08.2016.
  */

class ActorTestSpec extends TestKit(ActorSystem("ActorTestSpec"))
with WordSpecLike with Matchers with BeforeAndAfterAll with FutureHelpers with RandomDataGenerators{
  //Parameters
  private implicit val askTimeout = Timeout(5.seconds)
  //Stop actor sys
  override def afterAll = {TestKit.shutdownActorSystem(system)}
  //Helpers definitions
  case object GetDriveState
  case object GetChildActor
  abstract class RootActor extends Actor{
    override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Stop}
    val childActor: ActorRef
    def receive = {
      case GetChildActor ⇒ sender ! childActor
      case m ⇒ println("[RootActor] m: " + m)}}
  //Actor helpers
  implicit class ActorRefEx(actor: ActorRef){
    def askFor[R : ClassTag](msg: AnyRef): R = Await.result(ask(actor, msg).mapTo[R], askTimeout.duration)
    def askForState[R : ClassTag]: R = askFor(GetDriveState)}
  def newRootChildActorOf(actorInstance: Props, name: String): ActorRef = system
    .actorOf(Props(new RootActor{ val childActor = context.actorOf(actorInstance, name) }))
    .askFor[ActorRef](GetChildActor)
  //Test helpers
  implicit class AnySeqEx(seq: Seq[Any]){
    def getOneWithType[T : ClassTag]: T = {
      val clazz = classTag[T].runtimeClass
      val value = seq.find(_.getClass == clazz)
      assert(value.nonEmpty, s"for type $clazz value not found in seq $seq")
      value.get.asInstanceOf[T]}}

  //TODO Add more

}
