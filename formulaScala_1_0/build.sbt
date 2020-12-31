javacOptions := List("-encoding", "UTF-8")

name := "formulaScala_1_0"

fork in run := true

mainClass in (Compile, run) := Some("formula.Main")

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.7"
