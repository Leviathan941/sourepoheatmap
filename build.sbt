lazy val root = (project in file(".")).
  settings(
    name := "sourepoheatmap",
    version := "0.1",
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
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)
