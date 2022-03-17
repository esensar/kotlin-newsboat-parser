package com.ensarsarajcic.kotlinx.newsboatparser.generator

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class GenerateNewsboatOperationsTask : DefaultTask() {

    @TaskAction
    fun generate() {
        val result = downloadTextFromUrl("https://raw.githubusercontent.com/newsboat/newsboat/master/doc/keycmds.dsv")
    }
}