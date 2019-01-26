package hanzileveljs

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class TatoebaTest {
    private val tatoeba = Tatoeba()

    @TestFactory
    fun testSearchChinese() = listOf(
            "你好",
            "中文"
    ).map { input ->
        DynamicTest.dynamicTest("Search for $input") {
            println(tatoeba[input])
        }
    }
}