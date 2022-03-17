package com.ensarsarajcic.kotlinx.newsboatparser

object NewsboatParser

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

interface ParserCombinator<T> {
    operator fun invoke(input: String): ParseResult<T>
}

object CharParser : ParserCombinator<Char> {
    override operator fun invoke(input: String): ParseResult<Char> {
        if (input.isEmpty()) return ParseResult.Failure(Exception("Unexpected end of input"), input)
        return ParseResult.Success(input.first(), input.substring(1))
    }
}

class FilterParser<T>(private val parser: ParserCombinator<T>, private val filter: (T) -> Boolean) : ParserCombinator<T> {
    override operator fun invoke(input: String): ParseResult<T> {
        return parser(input).takeIf(filter) {
            ParseResult.Failure(Exception("Term rejected"), input)
        }
    }
}

object DigitParser : ParserCombinator<Char> by FilterParser(CharParser, Char::isDigit)
object LetterParser : ParserCombinator<Char> by FilterParser(CharParser, Char::isLetter)
class SpecificCharParser(private val char: Char) : ParserCombinator<Char> by FilterParser(CharParser, { it == char })
class ChoiceParser<T>(private val parsers: List<ParserCombinator<T>>) : ParserCombinator<T> {

    constructor(vararg parsers: ParserCombinator<T>) : this(parsers.toList())

    override operator fun invoke(input: String): ParseResult<T> {
        return parsers.firstNotNullOfOrNull {
            val result = it(input)
            if (result is ParseResult.Success) result else null
        } ?: ParseResult.Failure(Exception("No parsers succeeded"), input)
    }
}
object IdentifierCharParser : ParserCombinator<Char> by ChoiceParser(LetterParser, SpecificCharParser('-'))
class ManyParser<T>(private val parser: ParserCombinator<T>) : ParserCombinator<List<T>> {
    override operator fun invoke(input: String): ParseResult<List<T>> {
        val result = parser(input)
        if (result is ParseResult.Failure) return ParseResult.Success(listOf(), result.rest)
        result as ParseResult.Success
        return ManyParser(parser)(result.rest).mapSuccess {
            listOf(result.value) + it
        }.withDefault(listOf())
    }
}
object WhitespaceParser : ParserCombinator<List<Char>> by ManyParser(
    ChoiceParser(SpecificCharParser(' '), SpecificCharParser('\n'))
)
class MapperParser<T, R>(private val parser: ParserCombinator<T>, private val mapper: (T) -> R) : ParserCombinator<R> {
    override operator fun invoke(input: String): ParseResult<R> = parser(input).mapSuccess(mapper)
}
object IdentifierParser : ParserCombinator<String> by MapperParser(
    FilterParser(
        ManyParser(IdentifierCharParser),
        Collection<*>::isNotEmpty
    ),
    { it.joinToString(separator = "") }
)
class SequenceParser(private val parsers: List<ParserCombinator<*>>) : ParserCombinator<List<Any>> {

    constructor(vararg parsers: ParserCombinator<*>) : this(parsers.toList())

    override fun invoke(input: String): ParseResult<List<Any>> {
        if (parsers.isEmpty()) return ParseResult.Success(listOf(), input)
        val firstParser = parsers.first()
        val result = firstParser(input)
        if (result is ParseResult.Failure) return result.adapt()
        val value = (result as ParseResult.Success).value!!
        return SequenceParser(parsers.drop(1))(result.rest).mapSuccess {
            listOf(value) + it
        }
    }
}
class TokenParser<T>(private val parser: ParserCombinator<T>) : ParserCombinator<T> by MapperParser(
    SequenceParser(
        WhitespaceParser,
        parser,
        WhitespaceParser
    ),
    { it[1] as T }
)
class SeparatedListParser<T>(
    private val elementParser: ParserCombinator<T>,
    private val separatorParser: ParserCombinator<*>
) : ParserCombinator<List<T>> by MapperParser(
    SequenceParser(
        elementParser,
        ManyParser(SequenceParser(separatorParser, elementParser))
    ),
    {
        val firstElement = it[0] as T
        val otherElements = it[1] as List<List<T>>
        listOf(firstElement) + otherElements.map { it[1] }
    }
)
object ColumnsParser : ParserCombinator<List<String>> by SeparatedListParser(
    TokenParser(IdentifierParser),
    TokenParser(SpecificCharParser(','))
)
class KeywordParser<T>(private val expected: T) : ParserCombinator<T> by MapperParser(
    FilterParser(TokenParser(IdentifierParser)) { expected.toString().equals(it, ignoreCase = true) },
    { expected }
)
