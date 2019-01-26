package hanzileveljs

import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class CedictTest {
    private val cedict = Cedict()

    @TestFactory
    fun testSearchChinese() = listOf(
            "你们"
    ).map { input ->
        dynamicTest("Searching $input as Chinese") {
            println(cedict.searchChinese(input))
        }
    }

    @TestFactory
    fun testSearchPinyin() = listOf(
            "zhong1 wen2"
    ).map { input ->
        dynamicTest("Searching $input as Pinyin") {
            println(cedict.searchPinyin(input))
        }
    }

    @TestFactory
    fun testSearchEnglish() = listOf(
            "English"
    ).map { input ->
        dynamicTest("Searching $input as English") {
            println(cedict.searchEnglish(input).filterIndexed { index, _ -> index < 5 })
        }
    }

    @TestFactory
    fun testSearch() = listOf(
            "你们",
            "zhong1 wen2",
            "English"
    ).map { input ->
        dynamicTest("Searching $input as any") {
            println(cedict[input].filterIndexed { index, _ -> index < 5 })
        }
    }

    @Test
    fun testRandom() {
        println(cedict.random())
    }
}