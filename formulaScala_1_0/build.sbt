import sbt.Package.ManifestAttributes

scalaVersion := "2.11.12"

scalacOptions ++= Seq("-encoding", "UTF-8")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-encoding", "UTF-8")

name := "formulaScala_1_0"

fork in run := true

mainClass in (Compile, run) := Some("formula.Main")

packageOptions := Seq(ManifestAttributes(("Main-Class", "formula.Main"),("Class-Path", "eclipselink-2.3.0.jar;eclipselink-jpa-modelgen-2.3.0.jar;javax.persistence-2.0.jar;org.eclipse.persistence.jpa.jpql_1.0.0.jar;scala-library.jar;scala-swing-2.10.7.jar")))

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.7"
