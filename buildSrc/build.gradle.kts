plugins {
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("internalMavenPlugin") {
            id = "internal-maven-plugin"
            implementationClass = "InternalMavenPlugin"
        }
    }
}