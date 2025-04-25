import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class InternalModuleLoaderPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.extensions.create("modules", ModuleLoaderExtension::class.java, settings)
        settings.gradle.projectsLoaded {
            rootProject.subprojects {
                ModuleLoader.moduleConfigs[path]?.invoke(this)
            }
        }
    }
}

open class ModuleLoader(private val settings: Settings, private val prefix: String = "", ) {
    companion object {
        val moduleConfigs = mutableMapOf<String, (Project) -> Unit>()
    }

    open fun module(name: String, configure: ((Project) -> Unit)? = null, children: ModuleLoader.() -> Unit = {}, ) {
        val path = if (prefix.isEmpty()) name else "$prefix:$name"
        settings.include(path)
        if (configure != null) {
            moduleConfigs[":$path"] = configure
        }
        ModuleLoader(settings, path).apply(children)
    }
}

open class ModuleLoaderExtension(settings: Settings) : ModuleLoader(settings)







