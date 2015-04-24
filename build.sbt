lazy val root = (project in file(".")).
  settings(
    name := "sourepoheatmap",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.6"
  )

scalacOptions ++= Seq("-feature")

scalaSource in Compile := baseDirectory.value / "src"

javaHome := {
  val jdkHome: File = file(System.getenv("JAVA_HOME"))
  if (!jdkHome.exists) throw new RuntimeException( "No JDK found - try to set 'JAVA_HOME' environment variable." )
  Some(jdkHome)
}

unmanagedJars in Compile += Attributed.blank(javaHome.value.getOrElse(file(".")) / "jre/lib/ext/jfxrt.jar")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalafx" %% "scalafx" % "8.0.40-R8" withJavadoc() withSources()
)

mainClass in (Compile, run) := Some("org.sourepoheatmap.application.gui.GuiApplication")

mainClass in (Compile, packageBin) := Some("org.sourepoheatmap.application.gui.GuiApplication")

mainClass in assembly := Some("org.sourepoheatmap.application.gui.GuiApplication")

assemblyJarName in assembly := "sourepoheatmap_" + version.value + ".jar"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {
    x => x.data.getName.matches(".*javadoc\\.jar$") || x.data.getName.matches(".*sources\\.jar$") ||
      x.data.getName == "jfxrt.jar"
  }
}
