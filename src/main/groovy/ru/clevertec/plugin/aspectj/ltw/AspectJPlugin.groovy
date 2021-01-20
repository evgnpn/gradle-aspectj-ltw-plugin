package ru.clevertec.plugin.aspectj.ltw

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class AspectJPlugin implements Plugin<Project> {

    private final static String EXTENSION_NAME = "aspectjltw"

    private final static String IMPLEMENTATION_CONFIGURATION = "implementation"
    private final static String ASPECTJ_COMPILER_CONFIGURATION = "ajc"
    private final static String RUNTIME_AGENT_CONFIGURATION = "runtimeAgent"

    private final static String ASPECTJ_RUNTIME_ARTIFACT = "org.aspectj:aspectjrt"
    private final static String ASPECTJ_TOOLS_ARTIFACT = "org.aspectj:aspectjtools"
    private final static String ASPECTJ_WEAVER_ARTIFACT = "org.aspectj:aspectjweaver"
    private final static String ASPECTJ_VERSION = "1.9.6"

    void apply(Project project) {

        def extension = project.extensions.create(EXTENSION_NAME, AspectJPluginExtension)

        project.plugins.apply("java")

        project.repositories.add(project.repositories.mavenCentral())

        project.configurations.add(findOrCreateConfiguration(project, IMPLEMENTATION_CONFIGURATION))
        project.configurations.add(findOrCreateConfiguration(project, ASPECTJ_COMPILER_CONFIGURATION))
        project.configurations.add(findOrCreateConfiguration(project, RUNTIME_AGENT_CONFIGURATION))

        project.dependencies.add(IMPLEMENTATION_CONFIGURATION,
                project.dependencies.create("$ASPECTJ_RUNTIME_ARTIFACT:$ASPECTJ_VERSION"))

        def aspectjTools = project.dependencies.create("$ASPECTJ_TOOLS_ARTIFACT:$ASPECTJ_VERSION")
        project.dependencies.add(IMPLEMENTATION_CONFIGURATION, aspectjTools)
        project.dependencies.add(ASPECTJ_COMPILER_CONFIGURATION, aspectjTools)

        def aspectjWeaverDependency = project.dependencies.create("$ASPECTJ_WEAVER_ARTIFACT:$ASPECTJ_VERSION")
        project.dependencies.add(IMPLEMENTATION_CONFIGURATION, aspectjWeaverDependency)
        project.dependencies.add(RUNTIME_AGENT_CONFIGURATION, aspectjWeaverDependency)

        def aspectj = { sourceFileSet, destDir ->

            project.ant.taskdef(
                    resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties",
                    classpath: project.configurations.ajc.asPath
            )

            // https://www.eclipse.org/aspectj/doc/next/devguide/antTasks-iajc.html
            project.ant.iajc(
                    maxmem: "1024m",
                    fork: "true",
                    Xlint: "ignore",
                    classpath: sourceFileSet.runtimeClasspath.asPath,
                    destDir: destDir,
                    source: project.sourceCompatibility,
                    target: project.targetCompatibility)
                    {
                        sourceroots { sourceFileSet.java.srcDirs.each { dir -> pathelement(path: dir) } }
                    }
        }

        project.compileJava {
            doLast {
                aspectj(extension.sourceSetPart, extension.destinationDir)
            }
        }
    }

    private static Configuration findOrCreateConfiguration(Project project, String name) {
        def existsConfiguration = project.configurations.findByName(name)
        return existsConfiguration != null ? existsConfiguration : project.configurations.create(name)
    }
}
