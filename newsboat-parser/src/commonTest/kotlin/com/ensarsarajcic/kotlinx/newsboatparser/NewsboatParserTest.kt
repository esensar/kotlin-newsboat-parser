package com.ensarsarajcic.kotlinx.newsboatparser

import kotlin.test.Test
import kotlin.test.assertTrue

enum class Keyword {
    SELECT,
    FROM;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}

class NewsboatParserTest {
    @Test
    fun Test() {
        val input = "   select hola, hola-two, hola-three from kemo"
        KeywordParser(Keyword.SELECT).invoke(input).also {
            println(it)
        }
        ColumnsParser.invoke("hola, hola-two, hola-three").also { println(it) }
        SequenceParser(
            KeywordParser(Keyword.SELECT),
            ColumnsParser,
            KeywordParser(Keyword.FROM)
        ).invoke(input).also {
            println(it)
            assertTrue(false)
        }
    }
}
