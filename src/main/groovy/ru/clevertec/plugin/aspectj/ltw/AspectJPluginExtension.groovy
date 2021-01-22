package ru.clevertec.plugin.aspectj.ltw

import org.gradle.api.file.FileCollection

// https://www.eclipse.org/aspectj/doc/next/devguide/antTasks-iajc.html
class AspectJPluginExtension {
    String maxmem = "1024m"
    boolean fork = false
    String xlint = "warning"
    FileCollection classpath
    String destDir
    String source
    String target
    String sourceRootCopyFilter
    Set sourceRoots
}
