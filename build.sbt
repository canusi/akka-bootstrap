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

lazy val settingsMain = Seq(
  name := "myprojectname",
  version := "0.1",
  organization := "com.organization",
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



// Declare your versions here
lazy val akkaVersion = "2.5.23"


// Add your libraries here.
libraryDependencies ++= depTest ++ depAkkaTests ++ depTypConf


// Compose your modules here
lazy val depTest = Seq(
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
  , "org.specs2" %% "specs2-core" % "4.7.1" % Test
)

lazy val depAkkaTests = Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
  , "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  , "com.typesafe.akka" %% "akka-testkit" % akkaVersion
)

lazy val depTypConf = Seq(
  "com.typesafe" % "config" % "1.3.0"
)
