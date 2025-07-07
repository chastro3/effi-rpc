subprojects {
    apply(plugin = "java-library")
    dependencies {
        add("implementation", project(":effi-rpc-protocols:effi-rpc-http"))
    }
}