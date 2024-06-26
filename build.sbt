ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0-RC1"

lazy val root = (project in file("."))
  .settings(
    name := "ECScalibur",
    libraryDependencies += "com.google.guava" % "guava" % "33.2.1-jre",

    // Test libraries
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test,
    // libraryDependencies += "org.scalamock" %% "scalamock" % "6.0.0" % Test,

    scalacOptions ++= Seq(
      "-deprecation",
      "-experimental",
      "-feature",
      "-language:experimental.macros",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Wsafe-init",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:imports",
      "-Wunused:locals",
      "-Wunused:params",
      "-Wunused:privates",
      "-Wvalue-discard",
      //"-Xfatal-warnings",
      "-Xcheck-macros", 
      "-Xkind-projector",
      "-Ycheck:all", 
      //"-Yexplicit-nulls",
      ),
  )
