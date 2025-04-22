import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings

/**
 * DSL for hierarchical module inclusion and configuration in settings.gradle.kts
 */
class ModuleLoader(private val settings: Settings) {
    /**
     * Include a module
     * @param path   The project path (e.g. "effi-rpc-registry" or "effi-rpc-common")
     * @param config Configuration block for extra properties; defaults to no-op
     * @param children Nested child modules DSL; defaults to no-op
     */
    fun module(
        path: String,
        config: ProjectDescriptor.() -> Unit = {},
        children: ModuleLoader.() -> Unit = {}
    ) {
        settings.include(path)
        settings.findProject(":$path")?.apply {
            name = path.substringAfterLast(":")
            config()
        }
        ModuleLoader(this.settings).apply(children)
    }
}

/**
 * Entry point for modules DSL in settings.gradle.kts
 */
fun Settings.modules(block: ModuleLoader.() -> Unit) {
    ModuleLoader(this).apply(block)
}