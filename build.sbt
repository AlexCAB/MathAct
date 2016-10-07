name := "MathAct"

version := "0.2.0"

lazy val mathact_core = project in file("mathact_core")

  
lazy val mathact_tools = (project in file("mathact_tools"))
  .dependsOn(mathact_core)
  .aggregate(mathact_core)
  
  
lazy val mathact_examples = (project in file("mathact_examples"))
  .dependsOn(mathact_core, mathact_tools)
  .aggregate(mathact_core, mathact_tools)  
  

lazy val root = (project in file("."))
  .aggregate(mathact_core, mathact_tools, mathact_examples)