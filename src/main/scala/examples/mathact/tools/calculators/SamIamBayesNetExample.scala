package examples.mathact.tools.calculators
import mathact.tools.Workbench
import mathact.tools.calculators.SamIamBayesNet
import mathact.tools.pots.{SwitchBoard, PotBoard}
import mathact.tools.values.ValuesBoard


/**
 * Example of using of SamIamBayesNet.
 * Created by CAB on 14.09.2015.
 */

object SamIamBayesNetExample extends Workbench{
  //Probabilities
  val probs = new PotBoard("Probabilities"){
    val Difficulty  = init(0.6)          in(0,1)
    val Grade_d0_i0 = array(0.3,0.4,0.3) in(0,1)
    val Letter_g0   = init(0.1)          in(0,1)
    val SAT_i0      = array(0.95,0.05)     in(0,1)
  }
  //Evidence
  val evd = new SwitchBoard("Evidence"){
    val Intelligence = init("i0")   options("none", "i0", "i1")
    val Grade        = init("none") options("none", "g0", "g1", "g2")
  }
  //Inference
  val inf = new ValuesBoard("MyVals"){
    var SAT_s0_prob = .0
    var Grade_g0 = .0
    var Grade_g1 = .0
    var Grade_g2 = .0
    var Grade_val = ""
    var Grade_g1_prob = .0
  }
  //Bayes net
  new SamIamBayesNet(netPath = "docs/StudentBayesianNetwork.net", name = "Student net", showCPT = true){
    //Probabilities
    cpt node "Difficulty" binary{probs.Difficulty}
    cpt node "Grade"      column("d0","i0") of(
      {probs.Grade_d0_i0(0)},
      {probs.Grade_d0_i0(1)},
      {probs.Grade_d0_i0(2)})
    cpt node "Letter"     column "g0" binary{probs.Letter_g0}
    cpt node "SAT"        table(
      {probs.SAT_i0(0)},{0.2},
      {probs.SAT_i0(1)},{0.8})
    //Evidence
    evidence node "Intelligence" of{evd.Intelligence}
    evidence node "Grade" of{evd.Grade}
    //Inference
    inference{ allNodes ⇒ //Map(<node ID> → Map(<value name> → <probability>))
      println(allNodes)
    }
    inference allValues{ allValues ⇒ //Map(<node ID> → <value name>)
      println(allValues)
    }
    inference node "SAT" binaryProb{ s0Prob ⇒
      inf.SAT_s0_prob = s0Prob
    }
    inference node "Grade" allProb{ allProb ⇒ //Map(<value name> → <probability>)
      inf.Grade_g0 = allProb("g0")
      inf.Grade_g1 = allProb("g1")
      inf.Grade_g2 = allProb("g2")
    }
    inference node "Grade" value{ valueName ⇒
      inf.Grade_val = valueName
    }
    inference node "Grade" valueProb "g1" prob{ g1Prob ⇒
      inf.Grade_g1_prob = g1Prob
    }
  }
}
