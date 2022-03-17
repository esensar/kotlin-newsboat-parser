package com.ensarsarajcic.kotlinx.newsboatparser.generator

import org.gradle.api.tasks.Input

abstract class GenerateNewsboatConfigTask : BaseGenerateConfigTask() {
    @get:Input
    override val commandsUrl: String = "https://raw.githubusercontent.com/newsboat/newsboat/master/doc/configcommands.dsv"
    @get:Input
    override val className: String = "NewsboatConfig"
}