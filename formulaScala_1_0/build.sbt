lazy val root = (project in file(".")).settings(
  javacOptions := List("-encoding", "UTF-8", "-Xlint:deprecation"),
  name := "formulaScala_1_0",
  fork in run := true,
  mainClass := Some("formula.MainScala"),

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.eclipse.persistence" % "javax.persistence" % "2.2.0",
    "org.eclipse.persistence" % "eclipselink" % "2.6.4",
    "org.scala-lang.modules" %% "scala-swing" % "2.0.0",
    "com.microsoft.sqlserver" % "mssql-jdbc" % "6.4.0.jre8"
  )
)