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

package mathact.core.dummies

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.duration._
import scala.reflect._


/** Test infrastructure factory
  * Created by CAB on 17.08.2016.
  */

class TestActor(name: String, customReceive: ActorRef⇒PartialFunction[Any, Option[Any]], system: ActorSystem){
  //Parameters
  val expectMsgTimeout: FiniteDuration = 3.seconds
  val waitMsgTimeout: FiniteDuration = 500.millis
  //Messages
  private case class SendTo(to: ActorRef, msg: Any)
  private case class WatchFor(to: ActorRef)
  //Variables
  private object Mutex
  @volatile private var receivedMessages = List[Any]()
  @volatile private var processedMessages = List[Any]()
  //Actor
  val ref: ActorRef = system.actorOf(
    Props(new Actor{
      def receive = {
        case WatchFor(actor) ⇒
          context.watch(actor)
        case SendTo(to, msg) ⇒
          to ! msg
        case msg ⇒
          customReceive(self).lift(msg) match{
            case Some(res) ⇒
              println(s"[TestActor: $name] Processed message: $msg")
              processedMessages :+= msg
              res.foreach(m ⇒ sender ! m)
            case None ⇒
              println(s"[TestActor: $name] Received message: $msg")
              Mutex.synchronized{ receivedMessages :+= msg }}}}),
    name)
  //Classes
  class Response(data: Option[Any]){
    def expectResponseType[T : ClassTag]: T = {
      val clazz = classTag[T]
      assert(data.nonEmpty, s"timeout during request while waiting for type $clazz")
      val msg = data.get
      assert(data.get.getClass == clazz.runtimeClass, s"expected $clazz, found ${msg.getClass} ($msg)")
      msg.asInstanceOf[T]}
    def expectResponseMsg(msg: Any): Any = {
      assert(data.nonEmpty, s"timeout during request while waiting for $msg")
      assert(data.get == msg, s"expected $msg, found ${data.get}")
      data.get}}
  //Methods
  /** Clean old messages */
  def clean(): Unit = {
    receivedMessages = List()
    processedMessages = List()}
  /** Sending of any message to given actor
    * @param to - ActorRef, target actor
    * @param msg - Any, message */
  def send(to: ActorRef, msg: Any): Unit = {
    ref ! SendTo(to, msg)
    println(s"[TestActor: $name] Send message: $msg, to: $to")}
  /** Receive given number of messages and return
    * @param number - Int
    * @return - Seq[Any], received messages */
  def expectNMsg(number: Int)(implicit duration: FiniteDuration = expectMsgTimeout): List[Any] = {
    var counter = duration.toMillis / 10
    while (Mutex.synchronized{ receivedMessages.size } < number && counter > 0){
      Thread.sleep(10)
      counter -= 1}
    Mutex.synchronized{
      val received = receivedMessages.take(number)
      receivedMessages = receivedMessages.drop(received.size)
      received}}
  /** Receive given all of messages during duration and return
    * @return - Seq[Any], received messages */
  def expectAllMsg(implicit duration: FiniteDuration = expectMsgTimeout): List[Any] = {
    Thread.sleep(duration.toMillis)
    receivedMessages}
  /** Expectation of receiving of given message
    * @param msg - Any, to check
    * @param duration - FiniteDuration, wait timeout
    * @return - Any, received message of throw AssertionError */
  def expectMsg(msg: Any)(implicit duration: FiniteDuration = expectMsgTimeout): Any = {
    val opMsg = expectNMsg(1)(duration).headOption
    assert(opMsg.nonEmpty, s"timeout ($duration) during expectMsg while waiting for $msg")
    assert(opMsg.get == msg, s"expected $msg, found ${opMsg.get}")
    opMsg.get}
  /** Expectation of receiving of message with given type
    * @param duration - FiniteDuration, wait timeout
    * @tparam T - expected type
    * @return - message */
  def expectMsgType[T : ClassTag](implicit duration: FiniteDuration = expectMsgTimeout): T = {
    val opMsg = expectNMsg(1)(duration).headOption
    val clazz = classTag[T]
    assert(opMsg.nonEmpty, s"timeout ($duration) during expectMsg while waiting for type $clazz")
    val msg = opMsg.get
    assert(opMsg.get.getClass == clazz.runtimeClass, s"expected $clazz, found ${msg.getClass} ($msg)")
    msg.asInstanceOf[T]}
  /** Expectation no messages
    * @param duration - FiniteDuration, wait timeout */
  def expectNoMsg(implicit duration: FiniteDuration = expectMsgTimeout): Unit = {
    val allMsg = expectAllMsg(duration)
    assert(allMsg.isEmpty, s"timeout ($duration) during expectNMsg while receive: $allMsg")}
  /** Watch for given actor
    * @param actor - ActorRef */
  def watch(actor: ActorRef): Unit = ref ! WatchFor(actor)
  /** Return list of processed messages
    * @return - List[Any] */
  def getProcessedMessages: List[Any] = processedMessages



    //TODO Add more

}

object TestActor {
  def apply(name: String)(receive: ActorRef⇒PartialFunction[Any, Option[Any]])(implicit system: ActorSystem)
  :TestActor =
    new TestActor(name, receive, system)}
