package examples.mathact.tools.calculators
import mathact.tools.Workbench
import mathact.tools.calculators.SamIamBayesNet


/**
 * Example of using of SamIamBayesNet.
 * Created by CAB on 14.09.2015.
 */

object SamIamBayesNetExample extends Workbench{





  //
  new SamIamBayesNet(netPath = "docs/StudentBayesianNetwork.net", name = "Student net", showCPT = true){


    //Probabilities
    cpt node "Difficulty" binary{0.7}
    cpt node "Grade" column("d0","i0") of({0.1},{0.5},{0.4})
    cpt node "Letter" column "g0" binary{0.1}
    cpt node "SAT" table(
      {0.8},{0.3},
      {0.2},{0.7})
    //Evidence
    evidence node "Difficulty" of{"d0"}
    evidence node "Grade" of{"g1"}
    //Inference
    inference{ allNodes ⇒ //Map(<node name> → Map(<value name> → <probability>))

    }
    inference allValues{ allValues ⇒ //Map(<node name> → <value name>)

    }
    inference node "SAT" binaryProb{ s0Prob ⇒

    }
    inference node "Grade" allProb{ allProb ⇒ //Map(<node name> → <probability>)

    }
    inference node "Grade" value{ valueName ⇒

    }
    inference node "Grade" valueProb "g1" prob{ g1Prob ⇒

    }




  }


}
