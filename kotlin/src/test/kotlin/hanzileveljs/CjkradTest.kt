package hanzileveljs

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class CjkradTest {
    private val cjkrad = Cjkrad()

    @TestFactory
    fun testRadicalFinder() = listOf(
            "你",
            "好",
            "你好"
    ).map { input ->
        DynamicTest.dynamicTest("Cjkrad of $input") {
            println(cjkrad[input])
        }
    }
}