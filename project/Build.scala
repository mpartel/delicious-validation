import sbt._
import sbt.Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object MasterBuild extends Build {
  val ScalaVersion = "2.10.3"

  val commonSettings = Seq(
    scalaVersion := ScalaVersion,
    organization := "so.delicious.validation",

    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature"
    ),

    resolvers ++= Seq(
      "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
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
        name := "delicious-validation",
        libraryDependencies ++= Seq(
          "org.scalatest" % "scalatest_2.10" % "2.0.RC1" % "test",
          "com.chuusai" % "shapeless_2.10.2" % "2.0.0-M1" % "test"
        )
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
  ) aggregate (
    macroProject, coreProject
  ) dependsOn (
    macroProject, coreProject
  )
}
