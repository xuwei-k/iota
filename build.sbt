lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(scalacheckJVM, scalacheckJS)
  .aggregate(testsJVM, testsJS)
  .aggregate(examplesCatsJVM, examplesCatsJS)
  .aggregate(examplesScalazJVM, examplesScalazJS)
  .aggregate(bench)
  .aggregate(corezJVM, corezJS)
  .aggregate(scalacheckzJVM, scalacheckzJS)
  .aggregate(testszJVM, testszJS)
  .aggregate(readme, docs)

lazy val core = module("core", hideFolder = true)
  .settings(macroSettings)
  .settings(yax(file("modules/core/src/main/scala"), Compile,
    flags    = "cats" :: Nil,
    yaxScala = true),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.0.0-RC1",
      "org.typelevel" %%% "cats-free" % "2.0.0-RC1",
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val corez = module("core", hideFolder = true, prefixSuffix = "z")
  .settings(macroSettings)
  .settings(yax(file("modules/core/src/main/scala"), Compile,
    flags    = "scalaz" :: Nil,
    yaxScala = true),
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-core" % "7.2.28"
    )
  )

lazy val corezJVM = corez.jvm
lazy val corezJS  = corez.js

lazy val scalacheck = module("scalacheck", hideFolder = true)
  .dependsOn(core)
  .settings(macroSettings)
  .settings(yax(file("modules/scalacheck/src/main/scala"), Compile,
    flags    = "cats" :: Nil,
    yaxScala = true),
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.14.0"
    )
  )

lazy val scalacheckJVM = scalacheck.jvm
lazy val scalacheckJS  = scalacheck.js

lazy val scalacheckz = module("scalacheck", hideFolder = true, prefixSuffix = "z")
  .dependsOn(corez)
  .settings(macroSettings)
  .settings(yax(file("modules/scalacheck/src/main/scala"), Compile,
    flags    = "scalaz" :: Nil,
    yaxScala = true),
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.14.0"
    )
  )

lazy val scalacheckzJVM = scalacheckz.jvm
lazy val scalacheckzJS  = scalacheckz.js

lazy val tests = module("tests", hideFolder = true)
  .dependsOn(core)
  .dependsOn(scalacheck)
  .settings(noPublishSettings)
  .settings(macroSettings)
  .settings(yax(file("modules/tests/src/main/scala"), Compile,
    flags       = "cats" :: Nil,
    yaxPlatform = true))
  .settings(yax(file("modules/tests/src/test/scala"), Test,
    flags       = "cats" :: Nil,
    yaxPlatform = true),
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.14.0" % "test",
      "com.chuusai" %%% "shapeless" % "2.3.3" % "test",
      "com.github.alexarchambault" %%% "scalacheck-shapeless_1.14" % "1.2.3" % "test"
    )
  )

lazy val testsJVM = tests.jvm
lazy val testsJS  = tests.js

lazy val testsz = module("tests", hideFolder = true, prefixSuffix = "z")
  .dependsOn(corez)
  .dependsOn(scalacheckz)
  .settings(noPublishSettings)
  .settings(macroSettings)
  .settings(yax(file("modules/tests/src/main/scala"), Compile,
    flags       = "scalaz" :: Nil,
    yaxPlatform = true))
  .settings(yax(file("modules/tests/src/test/scala"), Test,
    flags       = "scalaz" :: Nil,
    yaxPlatform = true),
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.14.0" % "test",
      "com.chuusai" %%% "shapeless" % "2.3.3" % "test",
      "com.github.alexarchambault" %%% "scalacheck-shapeless_1.14" % "1.2.3"
    )
  )

lazy val testszJVM = testsz.jvm.settings(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)
lazy val testszJS  = testsz.js.settings(
  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.8" % "test"
)

lazy val examplesCats = module("examples-cats")
  .dependsOn(core)
  .settings(noPublishSettings)
  .settings(macroSettings)

lazy val examplesCatsJVM = examplesCats.jvm
lazy val examplesCatsJS  = examplesCats.js

lazy val examplesScalaz = module("examples-scalaz")
  .dependsOn(corez)
  .settings(noPublishSettings)
  .settings(macroSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-effect" % "7.2.28"
    )
  )

lazy val examplesScalazJVM = examplesScalaz.jvm
lazy val examplesScalazJS  = examplesScalaz.js

lazy val readme = jvmModule("readme")
  .dependsOn(coreJVM)
  .dependsOn(corezJVM)
  .enablePlugins(TutPlugin)
  .settings(noPublishSettings)
  .settings(macroSettings)
  .settings(
    scalacOptions in Tut := Nil,
    tutTargetDirectory := (baseDirectory in LocalRootProject).value)

lazy val docs = jvmModule("docs")
  .dependsOn(coreJVM)
  .dependsOn(corezJVM)
  .enablePlugins(TutPlugin)
  .settings(noPublishSettings)
  .settings(macroSettings)
  .settings(
    scalacOptions in Tut := Nil,
    tutTargetDirectory := (baseDirectory in LocalRootProject).value / "docs")
  .settings(libraryDependencies +=
    "org.scalaz" %% "scalaz-effect" % "7.2.28")

lazy val bench = jvmModule("bench")
  .enablePlugins(JmhPlugin)
  .dependsOn(coreJVM)
  .configs(Codegen)
  .settings(inConfig(Codegen)(Defaults.configSettings))
  .settings(classpathConfiguration in Codegen := Compile)
  .settings(noPublishSettings)
  .settings(macroSettings)
  .settings(libraryDependencies ++= Seq(
    %%("scalacheck")))
  .settings(inConfig(Compile)(
    sourceGenerators += Def.task {
      val path = (sourceManaged in(Compile, compile)).value / "bench.scala"
      (runner in (Codegen, run)).value.run(
        "iota.bench.BenchBoiler",
        Attributed.data((fullClasspath in Codegen).value),
        path.toString :: Nil,
        streams.value.log)
      path :: Nil
    }
  ))

lazy val Codegen = config("codegen").hide

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.asc")
pgpSecretRing := file(s"$gpgFolder/secring.asc")

lazy val macroSettings: Seq[Setting[_]] = Seq(
  libraryDependencies ++= Seq(
    scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
    scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
  ),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
     case Some((2, v)) if v <= 12 =>
       Nil
     case _ =>
       Seq("-Ymacro-annotations")
    }
  },
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
     case Some((2, v)) if v <= 12 =>
       Seq(
         compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
       )
     case _ =>
       Nil
    }
  }
)
