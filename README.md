**gradle-aspectj-ltw-plugin**

This plugin makes it easy to use AspectJ LTW in your project.

**Example:**
```groovy
plugins {
    // ...
    id 'ru.clevertec.plugin.aspectj.ltw' version '1.1'
}

def aspectjSourceRoot = "src/aspects/java"

sourceSets {
    aspects.java.srcDir aspectjSourceRoot
    main.java.srcDir aspectjSourceRoot
}

aspectjltw {
    xlint = "ignore"
    fork = true
    sourceRoots = sourceSets.aspects.java.srcDirs
    destDir = sourceSets.main.java.outputDir
    classpath = sourceSets.main.runtimeClasspath
}

def applyJvmArgs = { o ->
    o.jvmArgs += ["--add-opens", "java.base/java.lang=ALL-UNNAMED"]
    configurations.runtimeAgent.each { o.jvmArgs "-javaagent:${it.absolutePath}" }
}

test {
    useJUnitPlatform()
    doFirst { t -> applyJvmArgs(t) }
}

tasks.withType(JavaExec) {
    it.each { t -> doFirst { applyJvmArgs(t) } }
}
```

**Important:**
>If the aspect sources have the same package as the project, add the aspect
sources to exclude in aop.xml file (example: ```<exclude within="group.module.aspects..*"/>```),
see also example below.
```xml
<!DOCTYPE aspectj PUBLIC
        "-//AspectJ//DTD//EN"
        "http://www.eclipse.org/aspectj/dtd/aspectj.dtd">
<aspectj>
    <weaver options="-Xset:weaveJavaxPackages=true"> <!-- -verbose -showWeaveInfo  -->
        
        // ADD ASPECT SOURCES TO EXCLUDE!!!
        <exclude within="group.module.aspects..*"/>
      
        <include within="group.module..*"/>
    </weaver>
    <aspects>
        <aspect name="group.module.aspects.SomeAspect"/>
    </aspects>
</aspectj>
```

**Available options:**
- maxmem (default: "1024m")
- fork (default: false)
- xlint (default: "warning")
- classpath
- destDir
- source
- target
- sourceRootCopyFilter
- sourceRoots
