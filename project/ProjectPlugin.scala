import sbt.Keys._
import sbt._
import freestyle.FreestylePlugin
import sbtorgpolicies.model._
import sbtorgpolicies.OrgPoliciesKeys.orgBadgeListSetting
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies.templates.badges._
import sbtorgpolicies.templates._
import scoverage.ScoverageKeys._
import sbtcrossproject.{CrossProject, CrossType}
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.JSPlatform
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.{CrossType => _, _}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import com.timushev.sbt.updates.UpdatesPlugin.autoImport._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = FreestylePlugin

  object autoImport {

    def module(
      modName: String, hideFolder: Boolean = false, prefixSuffix: String = ""
    ): CrossProject =
      CrossProject(
        s"$modName$prefixSuffix",
        file(s"""modules/${if (hideFolder) "." else ""}$modName$prefixSuffix"""),
      )(JVMPlatform, JSPlatform)
      .crossType(
        CrossType.Pure
      )
      .settings(moduleName := s"iota${prefixSuffix}-$modName")
      .settings(
        scalacOptions --= {
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, v)) if v <= 12 =>
              Nil
            case _ =>
              Seq(
                "-Yno-adapted-args",
                "-Ypartial-unification"
              )
          }
        }
      )

    def jvmModule(modName: String): Project =
      Project(modName, file(s"""modules/$modName"""))
        .settings(moduleName := s"iota-$modName")
  }

  lazy val commandAliases: Seq[Def.Setting[_]] = addCommandAlias("tutReadme", ";project readme;tut;project root")

  override def projectSettings: Seq[Def.Setting[_]] = commandAliases ++ Seq(

    name := "iota",
    orgProjectName := "Iota",
    description := "fast product/coproduct types",
    startYear := Option(2016),

    orgBadgeListSetting := List(
      TravisBadge.apply(_),
      MavenCentralBadge.apply(_),
      LicenseBadge.apply(_),
      ScalaLangBadge.apply(_),
      ScalaJSBadge.apply(_),
      GitHubIssuesBadge.apply(_)
    ),

    orgUpdateDocFilesSetting +=
      (baseDirectory in LocalRootProject).value / "modules" / "readme" / "src" / "main" / "tut",
    orgEnforcedFilesSetting := List(
      LicenseFileType(orgGithubSetting.value, orgLicenseSetting.value, startYear.value),
      ContributingFileType(orgProjectName.value, orgGithubSetting.value),
      VersionSbtFileType,
      ChangelogFileType,
      ReadmeFileType(
        orgProjectName.value,
        orgGithubSetting.value,
        startYear.value,
        orgLicenseSetting.value,
        orgCommitBranchSetting.value,
        sbtPlugin.value,
        name.value,
        version.value,
        scalaBinaryVersion.value,
        sbtBinaryVersion.value,
        orgSupportedScalaJSVersion.value,
        orgBadgeListSetting.value
      )
    ),

    headerCreate in Compile := Nil,
    headerCreate in Test := Nil,
    orgMaintainersSetting += Dev("andyscott", Some("Andy Scott (twitter: [@andygscott](https://twitter.com/andygscott))"), Some("andy.g.scott@gmail.com")),

    coverageFailOnMinimum := false,
    fork in Test := !isScalaJSProject.value,
    parallelExecution in Test := false,
    outputStrategy := Some(StdoutOutput),
    connectInput in run := true,
    cancelable in Global := true,
    crossScalaVersions := List("2.12.8", "2.13.0"),
    scalaVersion := "2.12.8",
    libraryDependencies ~= (_.filterNot(module => module.organization == "org.spire-math" && module.name == "kind-projector")),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    dependencyUpdatesFilter -= moduleFilter(organization = "org.eclipse.jetty") |
      moduleFilter(organization = "org.openjdk.jmh") |
      moduleFilter(organization = "pl.project13.scala", name = "sbt-jmh-extras")
  )

}
