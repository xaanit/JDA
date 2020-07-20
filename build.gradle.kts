/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//to build everything:             "gradlew build"
//to build and upload everything:  "gradlew bintrayUpload"

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import name.remal.gradle_plugins.plugins.code_quality.sonar.SonarLintExtension
import org.apache.tools.ant.filters.ReplaceTokens
import java.util.*

plugins {
    signing
    `java-library`
    `maven-publish`

    id("com.jfrog.bintray") version "1.8.1"
    id("com.github.ben-manes.versions") version "0.19.0"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("name.remal.sonarlint") version "1.0.208"
}

val versionObj = Version(major = "4", minor = "2", revision = "0")

project.group = "net.dv8tion"
project.version = "$versionObj"
val archivesBaseName = "JDA"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<SourceSetContainer> {
    register("examples") {
        java.srcDir("src/examples/java")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}

configure<SonarLintExtension> {
    reportsDir = file("$buildDir/sonarlint")
    excludes {
        message("java:S3776") // Cognitive Complexity of methods should not be too high
        // Technically this is correct but we kinda need it anyway
        message("java:S2139") // Exceptions should be either logged or rethrown but not both

        message("java:S108")  // Nested blocks of code should not be left empty
        message("java:S115")  // Constant names should comply with a naming convention
        message("java:S117")  // Local variable and method parameter names should comply with a naming convention
        message("java:S125")  // Sections of code should not be commented out
        message("java:S127")  // "for" loop stop conditions should be invariant
        message("java:S128")  // Switch cases should end with an unconditional "break" statement
        message("java:S135")  // Loops should not contain more than a single "break" or "continue" statement
        message("java:S1113") // The Object.finalize() method should not be overridden
        message("java:S1192") // String literals should not be duplicated
        message("java:S1186") // Methods should not be empty
        message("java:S1452") // Generic wildcard types should not be used in return types (this is nonsense)
        message("java:S1117") // Local variables should not shadow class fields
        message("java:S1118") // Utility classes should not have public constructors
        message("java:S1119") // Labels should not be used
        message("java:S1133") // Deprecated code should be removed
        message("java:S1141") // Try-catch blocks should not be nested
        message("java:S1191") // Classes from "sun.*" packages should not be used
        message("java:S1193") // Exception types should not be tested using "instanceof" in catch blocks
        message("java:S1199") // Nested code blocks should not be used
        // this is incorrect, the suggestion in effective java is to not have interfaces dedicated to constants
        message("java:S1214") // Constants should not be defined in interfaces
        message("java:S1301") // "switch" statements should have at least 3 "case" clauses
        message("java:S1602") // Lambdas containing only one statement should not nest this statement in a block
        message("java:S1611") // Parentheses should be removed from a single lambda input parameter when its type is inferred
        message("java:S1659") // Multiple variables should not be declared on the same line
        message("java:S1700") // A field should not duplicate the name of its containing class
        message("java:S1845") // signatures differ only by capitalization
        message("java:S1874") // Deprecated code should not be used
        // We don't use serializable
        message("java:S1948") // Fields in a "Serializable" class should either be transient or serializable
        message("java:S2142") // "InterruptedException" should not be ignored
        message("java:S2147") // Catches should be combined
        message("java:S2165") // "finalize" should not set fields to "null"
        message("java:S2445") // Blocks should be synchronized on "private final" fields
        message("java:S3077") // Non-primitive fields should not be "volatile"
        message("java:S3358") // Ternary operators should not be nested
        message("java:S3516") // Methods returns should not be invariant
        // Nobody cares about this lol
        message("java:S4524") // "default" clauses should be last
    }
}

repositories {
    jcenter()
}

dependencies {
    /* ABI dependencies */

    //Code safety
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("org.jetbrains:annotations:16.0.1")

    //Logger
    api("org.slf4j:slf4j-api:1.7.25")

    //Web Connection Support
    api("com.neovisionaries:nv-websocket-client:2.9")
    api("com.squareup.okhttp3:okhttp:3.13.0")

    //Opus library support
    api("club.minnced:opus-java:1.0.4@pom") {
        isTransitive = true
    }

    //Collections Utility
    api("org.apache.commons:commons-collections4:4.1")

    //we use this only together with opus-java
    // if that dependency is excluded it also doesn't need jna anymore
    // since jna is a transitive runtime dependency of opus-java we don't include it explicitly as dependency
    compileOnly("net.java.dev.jna:jna:4.4.0")

    /* Internal dependencies */

    //General Utility
    implementation("net.sf.trove4j:trove4j:3.0.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")

    //Sets the dependencies for the examples
    configurations.asMap["examplesCompile"] = configurations["apiElements"]
    configurations.asMap["examplesRuntime"] = configurations["implementation"]

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
}

val bintrayUpload: BintrayUploadTask by tasks
val compileJava: JavaCompile by tasks
val shadowJar: ShadowJar by tasks
val javadoc: Javadoc by tasks
val jar: Jar by tasks
val build: Task by tasks
val clean: Task by tasks
val test: Test by tasks
val check: Task by tasks

shadowJar.classifier = "withDependencies"

val sourcesForRelease = task<Copy>("sourcesForRelease") {
    from("src/main/java") {
        include("**/JDAInfo.java")
        val tokens = mapOf(
                "versionMajor" to versionObj.major,
                "versionMinor" to versionObj.minor,
                "versionRevision" to versionObj.revision,
                "versionBuild" to getBuild()
        )
        filter<ReplaceTokens>(mapOf("tokens" to tokens))
    }
    into("build/filteredSrc")

    includeEmptyDirs = false
}

val generateJavaSources = task<SourceTask>("generateJavaSources") {
    val javaSources = sourceSets["main"].allJava.filter {
        it.name != "JDAInfo.java"
    }.asFileTree

    source = javaSources + fileTree(sourcesForRelease.destinationDir)

    dependsOn(sourcesForRelease)
}

val noOpusJar = task<ShadowJar>("noOpusJar") {
    dependsOn(shadowJar)
    classifier = shadowJar.classifier + "-no-opus"

    configurations = shadowJar.configurations
    from(sourceSets["main"].output)
    exclude("natives/**")     // ~2 MB
    exclude("com/sun/jna/**") // ~1 MB
    exclude("club/minnced/opus/util/*")
    exclude("tomp2p/opuswrapper/*")

    manifest.inheritFrom(jar.manifest)
}

val minimalJar = task<ShadowJar>("minimalJar") {
    dependsOn(shadowJar)
    minimize()
    classifier = shadowJar.classifier + "-min"
    configurations = shadowJar.configurations
    from(sourceSets["main"].output)
    exclude("natives/**")     // ~2 MB
    exclude("com/sun/jna/**") // ~1 MB
    exclude("club/minnced/opus/util/*")
    exclude("tomp2p/opuswrapper/*")
    manifest.inheritFrom(jar.manifest)
}

val sourcesJar = task<Jar>("sourcesJar") {
    classifier = "sources"
    from("src/main/java") {
        exclude("**/JDAInfo.java")
    }
    from(sourcesForRelease.destinationDir)

    dependsOn(sourcesForRelease)
}

val javadocJar = task<Jar>("javadocJar") {
    dependsOn(javadoc)
    classifier = "javadoc"
    from(javadoc.destinationDir)
}

tasks.withType<ShadowJar> {
    exclude("*.pom")
}

tasks.withType<JavaCompile> {
    val arguments = mutableListOf("-Xlint:deprecation", "-Xlint:unchecked")
    options.encoding = "UTF-8"
    options.isIncremental = true
    if (JavaVersion.current().isJava9Compatible) doFirst {
        arguments += "--release"
        arguments += "8"
    }
    doFirst {
        options.compilerArgs = arguments
    }
}

compileJava.apply {
    source = generateJavaSources.source
    dependsOn(generateJavaSources)
}

jar.apply {
    baseName = project.name
    manifest.attributes(mapOf(
            "Implementation-Version" to version,
            "Automatic-Module-Name" to "net.dv8tion.jda"))
}

javadoc.apply {
    isFailOnError = false
    options.memberLevel = JavadocMemberLevel.PUBLIC
    options.encoding = "UTF-8"

    if (options is StandardJavadocDocletOptions) {
        val opt = options as StandardJavadocDocletOptions
        opt.author()
        opt.tags("incubating:a:Incubating:")
        opt.links(
                "https://docs.oracle.com/javase/8/docs/api/",
                "https://takahikokawasaki.github.io/nv-websocket-client/",
                "https://square.github.io/okhttp/3.x/okhttp/")
        if (JavaVersion.current().isJava9Compatible) {
            opt.addBooleanOption("html5", true)
            opt.addStringOption("-release", "8")
        }
        if (JavaVersion.current().isJava11Compatible) {
            opt.addBooleanOption("-no-module-directories", true)
        }
    }

    //### excludes ###

    //jda internals
    exclude("net/dv8tion/jda/internal")

    //voice crypto
    exclude("com/iwebpp/crypto")
}

build.apply {
    dependsOn(jar)
    dependsOn(javadocJar)
    dependsOn(sourcesJar)
    dependsOn(shadowJar)
    dependsOn(noOpusJar)
    dependsOn(minimalJar)

    jar.mustRunAfter(clean)
    javadocJar.mustRunAfter(jar)
    sourcesJar.mustRunAfter(javadocJar)
    shadowJar.mustRunAfter(sourcesJar)
}

bintrayUpload.apply {
    dependsOn(clean)
    dependsOn(build)
    build.mustRunAfter(clean)

    onlyIf { getProjectProperty("bintrayUsername").isNotEmpty() }
    onlyIf { getProjectProperty("bintrayApiKey").isNotEmpty() }
    onlyIf { System.getenv("BUILD_NUMBER") != null }
}

test.apply {
    useJUnitPlatform()
    failFast = true
}

publishing {
    publications {
        register("BintrayRelease", MavenPublication::class) {
            from(components["java"])

            artifactId = archivesBaseName
            groupId = project.group as String
            version = project.version as String

            artifact(javadocJar)
            artifact(sourcesJar)
        }
    }
}

bintray {
    user = getProjectProperty("bintrayUsername")
    key = getProjectProperty("bintrayApiKey")
    setPublications("BintrayRelease")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "JDA"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/DV8FromTheWorld/JDA.git"
        publish = true
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
            released = Date().toString()
        })
    })
}

fun getProjectProperty(propertyName: String): String {
    var property = ""
    if (hasProperty(propertyName)) {
        property = project.properties[propertyName] as? String ?: ""
    }
    return property
}

fun getBuild(): String {
    return System.getenv("BUILD_NUMBER")
            ?: System.getProperty("BUILD_NUMBER")
            ?: System.getenv("GIT_COMMIT")?.substring(0, 7)
            ?: System.getProperty("GIT_COMMIT")?.substring(0, 7)
            ?: "DEV"
}

class Version(
        val major: String,
        val minor: String,
        val revision: String) {
    override fun toString() = "$major.$minor.${revision}_${getBuild()}"
}
