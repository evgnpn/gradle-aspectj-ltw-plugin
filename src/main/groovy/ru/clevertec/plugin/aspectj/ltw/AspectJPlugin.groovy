package ru.clevertec.plugin.aspectj.ltw

import org.gradle.api.Plugin
import org.gradle.api.Project

class AspectJPlugin implements Plugin<Project> {

    private final static String EXTENSION_NAME = "aspectjltw"

    private final static String CONFIGURATION_ASPECTJ_COMPILER = "ajc"
    private final static String CONFIGURATION_RUNTIME_AGENT = "runtimeAgent"

    private final static String ASPECTJ_RUNTIME_ARTIFACT = "org.aspectj:aspectjrt"
    private final static String ASPECTJ_TOOLS_ARTIFACT = "org.aspectj:aspectjtools"
    private final static String ASPECTJ_WEAVER_ARTIFACT = "org.aspectj:aspectjweaver"
    private final static String ASPECTJ_VERSION = "1.9.6"

    void apply(Project project) {

        def extension = project.extensions.create(EXTENSION_NAME, AspectJPluginExtension)

        project.plugins.apply("java")

        project.repositories.add(project.repositories.mavenCentral())

        project.configurations.register(CONFIGURATION_ASPECTJ_COMPILER)
        project.configurations.register(CONFIGURATION_RUNTIME_AGENT)

        project.dependencies.implementation("$ASPECTJ_RUNTIME_ARTIFACT:$ASPECTJ_VERSION")
        project.dependencies.implementation("$ASPECTJ_TOOLS_ARTIFACT:$ASPECTJ_VERSION")
        project.dependencies.implementation("$ASPECTJ_WEAVER_ARTIFACT:$ASPECTJ_VERSION")
        project.dependencies.ajc("$ASPECTJ_TOOLS_ARTIFACT:$ASPECTJ_VERSION")
        project.dependencies.runtimeAgent("$ASPECTJ_WEAVER_ARTIFACT:$ASPECTJ_VERSION")

        project.compileJava { compileJava -> compileJava.doLast { aspectj(project, extension) } }
    }

    private static void aspectj(Project project, AspectJPluginExtension extension) {

        project.ant.taskdef(
                resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties",
                classpath: project.configurations.ajc.asPath
        )

        project.ant.iajc(
                maxmem: extension.maxmem,
                fork: extension.fork.toString(),
                Xlint: extension.xlint,
                classpath: extension.classpath.asPath,
                destDir: extension.destDir,
                source: sourceCompatibility(project, extension),
                target: targetCompatibility(project, extension),
                sourceRootCopyFilter: extension.sourceRootCopyFilter)
                {
                    sourceroots {
                        extension.sourceRoots.each { dir ->
                            pathelement(path: dir)
                        }
                    }
                }
    }

    private static String sourceCompatibility(Project project, AspectJPluginExtension extension) {
        return extension.source == null || extension.source.isBlank()
                ? project.sourceCompatibility
                : extension.source
    }

    private static String targetCompatibility(Project project, AspectJPluginExtension extension) {
        return extension.target == null || extension.target.isBlank()
                ? project.targetCompatibility
                : extension.target
    }
}
