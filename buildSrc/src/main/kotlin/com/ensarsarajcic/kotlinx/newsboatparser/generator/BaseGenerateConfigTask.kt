package com.ensarsarajcic.kotlinx.newsboatparser.generator

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.util.regex.Pattern

abstract class BaseGenerateConfigTask : DefaultTask() {

    sealed class Parameter {
        class Optional(val parameters: List<Parameter>, val variableLength: Boolean = false) : Parameter()
        class Field(val name: String, val type: Type) : Parameter()
        class Either<L : Parameter, R : Parameter>(val left: L, val right: R) : Parameter()
        class Literal(val value: String) : Parameter()
        // Special type of field, representing multiple fields joined together (no spaces between)
        class Joined<L : Parameter, R : Parameter>(val left: L, val right: R) : Parameter()

        enum class Type {

        }
    }

    data class ConfigCommandDefinition(
        val key: String,
        val parameters: List<Parameter>,
        val default: String?,
        val description: String,
        val example: String
    )

    @get:Input
    protected abstract val commandsUrl: String
    @get:Input
    protected abstract val className: String

    @TaskAction
    fun generate() {
        val config = downloadTextFromUrl(commandsUrl)

        val defs = config.lines().filter { it.isNotBlank() }.map {
            lineParser()(it)
        }
    }

    fun separatorParser() = sequenceParser(charParser('|'), charParser('|'))

    fun argsParser() = charParser()

    fun lineParser(): Parser<ConfigCommandDefinition> = mapperParser(sequenceParser(
        identifierParser(),
        separatorParser(),
        argsParser(),
        separatorParser(),
        charParser(),
        separatorParser()
    )) {
        ConfigCommandDefinition(
            it[0] as String,
            it[2] as List<Parameter>,
            it[4] as String,
            it[6] as String,
            it[8] as String
        )
    }

    private fun parseParameter(parameter: String): Parameter {
        val regex = Pattern.compile("((?:(\\[.*\\])|(<.*?>))(?:(\\[.*\\])|(<.*?>)))|(\\[.*\\])|(<.*?>)|(\".*\")").toRegex()
        val match = regex.find(parameter)

        return Parameter.Field("", Parameter.Type.valueOf(""))
    }

    private fun parseOptional(params: String): Parameter.Optional {
        return Parameter.Optional(listOf())
    }

    private fun parseParams(params: String) : List<Parameter> {
        var position = 0
        val result = mutableListOf<Parameter>()
        while (position < params.length) {
            if (params[position] == '<') {
            }
        }
        val regex = Pattern.compile("((?:(\\[.*\\])|(<.*?>))(?:(\\[.*\\])|(<.*?>)))|(\\[.*\\])|(<.*?>)|(\\.\\.\\.)").toRegex()
        val matches = regex.findAll(params)
        return matches.map { match ->
            val value = match.groups.first { it?.value == match.groups[0]?.value }?.value
            value?.let { parseParameter(it) }
        }.filterNotNull().toList()
    }

    private fun parseDefault(default: String) : String? {
        if (default == "n/a") return null
        if (default.endsWith("(localized)")) return parseDefault(default.removeSuffix("(localized)"))
        return default.removeSurrounding("(localized)").removeSurrounding(" ").removeSurrounding("\"")
    }
}