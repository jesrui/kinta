package com.dailymotion.kinta

import com.dailymotion.kinta.command.Bootstrap
import com.dailymotion.kinta.command.Init
import com.dailymotion.kinta.command.InitPlayStoreConfig
import com.dailymotion.kinta.command.Update
import com.dailymotion.kinta.integration.gradle.Gradle
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.io.File
import java.lang.IllegalStateException
import java.net.URLClassLoader
import java.util.*
import kotlin.system.exitProcess


val runtimeCommands by lazy {
    // Update the project workflows if needed
    if (File("kintaSrc").exists()) {
        Gradle(File("kintaSrc")).executeTask("shadowJar")
    }

    val jarFile = File("./kintaSrc/build/libs/kinta-workflows-custom.jar")

    val urlClassLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()))

    val loader = ServiceLoader.load(Workflows::class.java, urlClassLoader)

    try {
        loader.flatMap { it.all() }
    } catch (serviceError: ServiceConfigurationError) {
        throw serviceError
    }
}

val compiledCommands = listOf(
        Init,
        Bootstrap,
        Update,
        InitPlayStoreConfig
)

fun main(args: Array<String>) {
    val allCommands = runtimeCommands + compiledCommands
    object : CliktCommand(
            name = "kinta",
            help = "mobile workflows automation. Read more at https://dailymotion.github.io/kinta/",
            invokeWithoutSubcommand = true,
            printHelpOnEmptyArgs = true
    ) {
        val version by option("--version", "-V").flag()

        override fun run() {
            if (version) {
                println(VERSION)
                exitProcess(0)
            }
        }
    }.subcommands(allCommands.sortedBy { it.commandName })
            .main(args)

    // The apollo threadpools are still busy so don't wait for them and just exit the program
    // See https://github.com/apollographql/apollo-android/issues/1896
    exitProcess(0)
}