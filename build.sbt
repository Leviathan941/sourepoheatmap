import Dependencies._

val srcPath: File = file(".")

lazy val commonSettings: Seq[Setting[_]] = Seq(
  scalaVersion := scala211,
  version := "0.2-SNAPSHOT",
  organization := "org.leviathan941",
  scalacOptions += "-feature",
  licenses += ("BSD 3-Clause", url("http://opensource.org/licenses/BSD-3-Clause")),

  javaHome := {
    val jdkHome: File = file(System.getenv("JAVA_HOME"))
    if (!jdkHome.exists) throw new RuntimeException( "No JDK found - try to set 'JAVA_HOME' environment variable." )
    Some(jdkHome)
  }
  )

lazy val root: Project = (project in srcPath).
  aggregate(guiApp, cliApp, core).
  settings(commonSettings: _*).
  settings(
    name := "sourepoheatmap",
    description := "Application for building changes tree map of a source repository",
    libraryDependencies ++= Seq(
      scalaFx withJavadoc() withSources()
    )
  )

lazy val core: Project = (project in srcPath / "core").
  settings(commonSettings: _*).
  settings(
    name := "sourepoheatmap-core",
    description := "Core of Sourepo Heatmap",
    libraryDependencies ++= Seq(
      scalaCombinator,
      scalaReflect.value,
      eclipseJgit withJavadoc() withSources()
    )
  )

lazy val guiApp: Project = (project in srcPath / "guiapp").
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "sourepoheatmap-gui",
    description := "GUI for Sourepo Heatmap",
    libraryDependencies += scalaFx withJavadoc() withSources(),

    unmanagedJars in Compile += Attributed.blank(javaHome.value.getOrElse(file("/usr/lib/jvm/java-8-oracle")) /
      "jre/lib/ext/jfxrt.jar"),

    mainClass in (Compile, run) := Some("sourepoheatmap.application.gui.GuiApplication"),
    mainClass in (Compile, packageBin) := Some("sourepoheatmap.application.gui.GuiApplication"),

    mainClass in assembly := Some("sourepoheatmap.application.gui.GuiApplication"),
    assemblyJarName in assembly := name.value + "-" + version.value + ".jar"
  )

lazy val cliApp: Project = (project in srcPath / "cliapp").
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "sourepoheatmap-cli",
    description := "CLI for Sourepo Heatmap"
  )
