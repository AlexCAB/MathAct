package mathact.utils.definitions


/**
 * Agent States (used in games)
 * Created by CAB on 11.09.2015.
 */

trait AgentState

object AgentState {
  case object NotDefined extends AgentState
  case object Live extends AgentState
  case object Dead extends AgentState
  case object Funky extends AgentState
  case object Pursuer extends AgentState
  case object Vagrant extends AgentState
}
