package net.liplum

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DistributeInjection : DefaultTask() {
    val jar: ConfigurableFileCollection = project.files()
        @InputFiles get
    val excludeFolders: SetProperty<File> = project.objects.setProperty(File::class.java)
        @Input get
    val excludeFiles: SetProperty<File> = project.objects.setProperty(File::class.java)
        @Input get
    val name: Property<String> = project.objects.property(String::class.java)
        @Input get
    @TaskAction
    fun dist() {
        val dest = temporaryDir.resolve(name.get())
        val first = jar.files.first() ?: throw GradleException("No input")
        val excludedFiles = excludeFiles.get()
        val excludedFolders = excludeFolders.get()
        val unziped = temporaryDir.resolve("unziped")
        dest.delete()
        unziped.deleteRecursively()
        ZipFile(first).extractAll(unziped.absolutePath)
        val para = ZipParameters().apply {
            setExcludeFileFilter {
                val relative = it.relativeTo(unziped)
                excludedFiles.contains(relative)
            }
        }
        val zip = ZipFile(dest)
        unziped.listFiles()?.forEach {
            if (it.isFile && it.relativeTo(unziped) !in excludedFiles)
                zip.addFile(it, para)
            else if (it.isDirectory && it.relativeTo(unziped) !in excludedFolders)
                zip.addFolder(it, para)
        }
    }
}