import com.typesafe.config.ConfigFactory
import sbt.Keys.test
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy

// Specify the resource + source directories
resourceDirectory in Compile := baseDirectory.value / "src"/ "main" / "resources"
resourceDirectory in Test := baseDirectory.value / "src"/ "test" / "resources"

scalaSource in Compile := baseDirectory.value / "src"/ "main" / "scala"
scalaSource in Test := baseDirectory.value / "src"/ "test" / "scala"


// use this to load in library versions ( akka for example ) across multiple libraries
val config = ConfigFactory.load()


lazy val vCanusi = "1.0.8"

lazy val settingsMain = Seq(
  name := "akka-bootstrap",
  version := vCanusi,
  organization := "com.canusi",
  scalaVersion := "2.12.7",
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case x if x.contains("versions.properties") => MergeStrategy.first
    case "module-info.class" => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case "application.conf" => MergeStrategy.concat
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "META-INF/io.netty.versions.properties" => MergeStrategy.first
    case x => MergeStrategy.first
  }
)

lazy val root = ( project in file(".") )
  .settings( settingsMain )

resolvers += ( "Nexus" at "http://repository.canusi.com:9081/repository/com.canusi/" ).withAllowInsecureProtocol(true)
credentials += Credentials("Sonatype Nexus", "repository.canusi.com", "admin", "BjvBBPQlK1clPODkBGyFjcdUu6JhIJ5*hFVBo")
publishTo := {
  val nexus = "http://repository.canusi.com:9081/repository/com.canusi/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "content/repositories/releases")
}


  // Declare your versions here
lazy val vAkka = "2.5.23"
lazy val vScalaTest = "3.0.8"
lazy val vSpecs2Core = "4.7.1"
lazy val vTypeLogging = "3.9.2"
lazy val vTypeConfig = "1.3.0"


// Add your libraries here.
libraryDependencies ++= depTest ++ depAkkaTests ++ depTypeConfig ++ depTypeLogging


// Compose your modules here
lazy val depTest = Seq(
  "org.scalatest" %% "scalatest" % vScalaTest % Test
  , "org.specs2" %% "specs2-core" % vSpecs2Core % Test
)

lazy val depAkkaTests = Seq(
  "com.typesafe.akka" %% "akka-stream" % vAkka
  , "com.typesafe.akka" %% "akka-stream-testkit" % vAkka % Test
  , "com.typesafe.akka" %% "akka-testkit" % vAkka % Test
)

lazy val depTypeLogging = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % vTypeLogging
)

lazy val depTypeConfig = Seq(
  "com.typesafe" % "config" % vTypeConfig
)
