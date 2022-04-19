package com.ensarsarajcic.kotlinx.newsboatparser.generator

sealed class ParseResult<T>() {
    abstract val rest: String
    data class Success<T>(val value: T, override val rest: String) : ParseResult<T>() {
        fun <R> map(mapper: (T) -> R) = Success(mapper(value), rest)
    }
    data class Failure<T>(val failure: Exception, override val rest: String) : ParseResult<T>() {
        fun <R> adapt() = Failure<R>(failure, rest)
    }

    fun <R> mapSuccess(mapper: (T) -> R): ParseResult<R> {
        return when (this) {
            is Success -> map(mapper)
            is Failure -> adapt()
        }
    }

    fun withDefault(default: T): Success<T> {
        return when (this) {
            is Success -> this
            is Failure -> Success(default, rest)
        }
    }

    fun takeIf(condition: (T) -> Boolean, onFailure: (String) -> Failure<T>): ParseResult<T> {
        return when (this) {
            is Success -> if (condition(value)) this else onFailure(rest)
            is Failure -> this
        }
    }
}

typealias Parser<T> = (String) -> ParseResult<T>
typealias ParserCombinator<T> = () -> Parser<T>

fun charParser(): Parser<Char> = func@ { input: String ->
    if (input.isEmpty()) return@func ParseResult.Failure(Exception("Unexpected end of input"), input)
    return@func ParseResult.Success(input.first(), input.substring(1))
}

fun <T> filterParser(parser: Parser<T>, filter: (T) -> Boolean): Parser<T> = { input ->
    parser(input).takeIf(filter) {
        ParseResult.Failure(Exception("Term rejected"), input)
    }
}

fun digitParser() = filterParser(charParser(), Char::isDigit)
fun letterParser() = filterParser(charParser(), Char::isLetter)
fun charParser(char: Char) = filterParser(charParser()) { it == char }
fun <T> choiceParserOfList(parsers: List<Parser<T>>): Parser<T> = { input ->
    parsers.mapNotNull { it ->
        val result = it(input)
        if (result is ParseResult.Success<T>) result else null
    }.firstOrNull() ?: ParseResult.Failure(Exception("No parsers succeeded"), input)
}
fun <T> choiceParser(vararg parsers: Parser<T>) = choiceParserOfList(parsers.toList())
fun identifierCharParser() = choiceParser(letterParser(), charParser('-'))
fun <T> manyParser(parser: Parser<T>): Parser<List<T>> = { input ->
    when (val result = parser(input)) {
        is ParseResult.Failure -> ParseResult.Success(listOf(), result.rest)
        is ParseResult.Success -> manyParser(parser)(result.rest).mapSuccess {
                listOf(result.value) + it
            }.withDefault(listOf())
    }
}
fun whitespaceParser() = manyParser(choiceParser(charParser(' '), charParser('\n')))
fun <T, R> mapperParser(parser: Parser<T>, mapper: (T) -> R): Parser<R> = { input ->
    parser(input).mapSuccess(mapper)
}
fun identifierParser() = mapperParser(filterParser(manyParser(identifierCharParser()), Collection<*>::isNotEmpty)) { it.joinToString("") }
fun sequenceParserOfList(parsers: List<Parser<*>>): Parser<List<Any>> = func@ { input ->
    if (parsers.isEmpty()) return@func ParseResult.Success(listOf(), input)
    val firstParser = parsers.first()
    val result = firstParser(input)
    if (result is ParseResult.Failure) return@func result.adapt()
    val value = (result as ParseResult.Success).value!!
    return@func sequenceParserOfList(parsers.drop(1))(result.rest).mapSuccess {
        listOf(value) + it
    }
}
fun sequenceParser(vararg parser: Parser<*>) = sequenceParserOfList(parser.toList())
fun <T> tokenParser(parser: Parser<T>) = mapperParser(sequenceParser(whitespaceParser(), parser, whitespaceParser())) { it[1] as T }
fun <T> keywordParser(expected: T) = mapperParser(filterParser(tokenParser(identifierParser())) { expected.toString().equals(it, ignoreCase = true) }) { expected }