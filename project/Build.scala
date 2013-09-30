import sbt._
import sbt.Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object MasterBuild extends Build {
  val ScalaVersion = "2.10.2"

  val commonSettings = Seq(
    scalaVersion := ScalaVersion,
    organization := "so.delicious.validate",

    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature"
    ),

    EclipseKeys.withSource := true,
    EclipseKeys.eclipseOutput := Some("target/ide")
  )

  lazy val macroProject = Project(
    id = "delicious-validation-macros",
    base = file("macros"),
    settings =
      Project.defaultSettings ++
      commonSettings ++ Seq(
        name := "delicious-validation-macros",
        libraryDependencies += "org.scala-lang" % "scala-reflect" % ScalaVersion
      )
  )

  lazy val coreProject = Project(
    id = "delicious-validation",
    base = file("core"),
    settings =
      Project.defaultSettings ++
      commonSettings ++ Seq(
        name := "delicious-validation"
      )
  ) dependsOn (macroProject)

  lazy val mainProject = Project(
    id = "delicious-validation-master",
    base = file("."),
    settings =
      Project.defaultSettings ++
      commonSettings ++ Seq(
        name := "delicious-validation-master"
      )
  ) aggregate (macroProject, coreProject)
}
