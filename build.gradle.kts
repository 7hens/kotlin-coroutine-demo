
plugins {
    kotlin("jvm") version "1.3.31"
}

buildscript {
    val keystoreProperties = java.util.Properties()
    keystoreProperties.load(java.io.FileInputStream(rootProject.file("keystore.properties")))

    val sdkVersion by extra(28)
    val kotlinVersion by extra("1.3.31")
    val ankoVersion by extra("0.10.6")

    val STORE_PASSWORD by extra(keystoreProperties["STORE_PASSWORD"])
    val KEY_PASSWORD by extra(keystoreProperties["KEY_PASSWORD"])
    val KEY_ALIAS by extra(keystoreProperties["KEY_ALIAS"])
    val STORE_FILE by extra(keystoreProperties["STORE_FILE"])

    repositories {
        google()
        jcenter()
        mavenLocal()
        maven("https://dl.bintray.com/7hens/maven/repositories")
        maven("https://repo.spring.io/plugins-snapshot")
        maven("http://maven.aliyun.com/nexus/content/groups/public/")
        maven("http://repository.sonatype.org/content/groups/public")
        maven("http://mvnrepository.com/")
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.2.1")
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(kotlin("script-runtime", kotlinVersion))
        classpath("org.jetbrains.dokka:dokka-android-gradle-plugin:0.9.17")
//        classpath "com.qihoo360.replugin:replugin-host-gradle:$repluginVersion"
//        classpath "com.qihoo360.replugin:replugin-plugin-gradle:$repluginVersion"
//        classpath "cn.thens.andemo:test-gradle-plugin:$VERSION"
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenLocal()
        maven("https://dl.bintray.com/7hens/maven/")
        maven("http://maven.aliyun.com/nexus/content/groups/public/")
        maven("http://repository.sonatype.org/content/groups/public")
        maven("http://mvnrepository.com/")
        maven("https://jitpack.io")
        maven("http://dl.bintray.com/vsazel/libjingle")
    }
    tasks.withType(Javadoc::class.java) {
        setExcludes(listOf("**/*.kt"))
//        options.addStringOption("Xdoclint:none", "-quiet")
//        options.addStringOption("encoding", "UTF-8")
//        options.addBooleanOption("Xdoclint:none", true)
    }
}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}
