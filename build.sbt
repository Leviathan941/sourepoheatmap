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
  "org.scala-lang.modules" %% "scala-parser-combinators" % "latest.integration",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalafx" %% "scalafx" % "latest.integration" withJavadoc() withSources(),
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.+" withJavadoc() withSources() excludeAll(
    ExclusionRule(organization = "com.googlecode.javaewah"),
    ExclusionRule(organization = "com.jcraft"),
    ExclusionRule(organization = "org.apache.httpcomponents"),
    ExclusionRule(organization = "org.slf4j")
  )
)

mainClass in (Compile, run) := Some("org.sourepoheatmap.application.gui.GuiApplication")

mainClass in (Compile, packageBin) := Some("org.sourepoheatmap.application.gui.GuiApplication")

mainClass in assembly := Some("org.sourepoheatmap.application.gui.GuiApplication")

assemblyJarName in assembly := "sourepoheatmap_" + version.value + ".jar"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {
    x => x.data.getName == "jfxrt.jar"
  }
}
