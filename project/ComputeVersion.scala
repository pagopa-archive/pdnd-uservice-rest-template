import scala.sys.process._
import scala.util.matching.Regex

object ComputeVersion {

  val gitOutput: String = ("git symbolic-ref -q --short HEAD" #|| "git describe --tags --exact-match" #|| "git rev-parse --short HEAD").lineStream_!.head

  val releaseBranch: Regex = "([1-9]\\d*)\\.(\\d+)\\.([x])".r

  val tag: Regex = "(v[1-9]\\d*)\\.(\\d+)\\.(\\d+)".r

  lazy val version: String =  gitOutput match {
    case tag(major, minor, build) =>
      s"$major.$minor.$build"
    case releaseBranch(major, minor, _*) =>
      val out = s"""git tag --list v$major.$minor*""".lineStream_!.toList
      if (out.isEmpty)
        s"$major.$minor.1-SNAPSHOT"
      else {
        val lastBuildVersion = out.map(_.split("\\.")(2)).map(_.toInt).foldLeft(0)(Math.max)
        s"$major.$minor.${(lastBuildVersion + 1).toString}-SNAPSHOT"
      }
    case _ =>
      "0.0.0"
  }
}
