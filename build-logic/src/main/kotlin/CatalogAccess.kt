import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * В precompiled script plugins нет типобезопасного аксессора libs.*,
 * поэтому достаём каталог вручную: libs.lib("archunit-junit5").
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String) =
    findLibrary(alias).orElseThrow { IllegalArgumentException("Нет библиотеки '$alias' в libs.versions.toml") }
