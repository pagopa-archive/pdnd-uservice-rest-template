import sbt.{Project, ThisBuild}
import sbtbuildinfo.BuildInfoKeys.buildInfoOptions
import sbtbuildinfo.BuildInfoPlugin.autoImport.{BuildInfoKey, buildInfoKeys}
import sbtbuildinfo.{BuildInfoOption, BuildInfoPlugin}
import sbtdynver.DynVerPlugin.autoImport.{dynverCurrentDate, dynverInstance, dynverSeparator, dynverSonatypeSnapshots}
import sbtdynver.GitDescribeOutput

import java.util.Date
import scala.sys.process.Process
import scala.util.Try

/** Allows customizations of build.sbt syntax.
  */
object ProjectSettings {

  private val describeOutput: Option[GitDescribeOutput] = sbtdynver.DynVer.getGitDescribeOutput(new java.util.Date)

  //TODO since Git 2.22 we could use the following command instead: git branch --show-current
  private val currentBranch: Option[String] = Try(
    Process(s"git rev-parse --abbrev-ref HEAD").lineStream_!.head
  ).toOption
  private val commitSha: Option[String] = Try(Process(s"git rev-parse --short HEAD").lineStream_!.head).toOption

  private def getInterfaceVersion(date: Date) = {
    val semver = raw"([1-9]\d*)\.(\d+)\.(\d+).*".r
    sbtdynver.DynVer.version(date) match {
      case semver(major, minor, _*) =>
        s"$major.$minor"
      case _ =>
        "wrong-version"
    }
  }

  //lifts some useful data in BuildInfo instance
  val buildInfoExtra = Seq[BuildInfoKey](
    "ciBuildNumber"       -> sys.env.get("BUILD_NUMBER"),
    "commitSha"           -> commitSha,
    "headDistanceFromTag" -> describeOutput.map(_.commitSuffix.distance),
    "tag"                 -> describeOutput.map(_.ref.dropPrefix),
    "currentBranch"       -> currentBranch,
    BuildInfoKey.map(dynverInstance) { case (_, v) => "isDirty" -> v.isDirty },
    BuildInfoKey.map(dynverInstance) { case (_, v) => "isSnapshot" -> v.isSnapshot },
    BuildInfoKey.map(dynverInstance) { case (_, v) => "isVersionStable" -> v.isVersionStable },
    BuildInfoKey.map(dynverCurrentDate) { case (_, v) => "interfaceVersion" -> getInterfaceVersion(v) }
  )

  /** Extention methods for sbt Project instances.
    * @param project
    */
  implicit class ProjectFrom(project: Project) {
    def setupBuildInfo: Project = {
      project
        .enablePlugins(BuildInfoPlugin)
        .settings(ThisBuild / dynverSonatypeSnapshots := true)
        .settings(ThisBuild / dynverSeparator := "-")
        .settings(buildInfoKeys ++= buildInfoExtra)
        .settings(buildInfoOptions += BuildInfoOption.BuildTime)
        .settings(buildInfoOptions += BuildInfoOption.ToJson)
    }
  }
}
