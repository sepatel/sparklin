package io.tekniq.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class ValidationTest {
    data class TestBean(var name: String, var age: Int, val numbers: IntArray, val colors: Collection<String>, val zero: Int = 0, val emptyList: List<String> = emptyList(), val nested: TestBean? = null, val ts: Date = Date(), val whitespace: String = "      ")

    private val timestamp = Date()
    val pojo = TestBean("John Doe", 42, intArrayOf(3, 4, 5), listOf("Red"), nested = TestBean("Child Doe", 69, intArrayOf(4, 2, 6, 9), listOf("Yellow", "Green")), ts = timestamp)
    val map = mapOf("name" to "John Doe",
            "age" to 42,
            "numbers" to intArrayOf(3, 4, 5),
            "colors" to listOf("Red"),
            "zero" to 0,
            "emptyList" to emptyList<String>(),
            "nested" to mapOf(
                    "name" to "Child Doe",
                    "age" to 69,
                    "numbers" to intArrayOf(4, 2, 6, 9),
                    "colors" to listOf("Yellow", "Green")
            ),
            "ts" to timestamp,
            "whitespace" to "     ")

    @Test fun requiredUsingBean() = testRequired(Validation(pojo))

    @Test fun requiredUsingMap() = testRequired(Validation(map))

    @Test fun combineValidations() {
        val all = Validation(null)
                .merge(Validation(null).reject("ugly").reject("short", "name"))
                .merge(Validation(mapOf("age" to 42)).required("birthday"))

        assertEquals(3, all.rejections.size)
        println(all.rejections)
    }

    @Test fun validateOrFailed() {
        val validation = Validation(pojo)
        validation.or {
            required("sage")
            required("sero")
        }

        println(validation.rejections)
        assertEquals(1, validation.rejections.size)
    }

    @Test fun validateOrHalfPassed() {
        val validation = Validation(pojo)
        validation.or {
            required("age")
            required("sero")
        }

        println(validation.rejections)
        assertEquals(0, validation.rejections.size)
    }

    @Test fun validateAndFailed() {
        val validation = Validation(pojo)
        validation.and {
            number("age")
            string("name")
            required("game")
            required("shame")
        }

        println(validation.rejections)
        assertEquals(1, validation.rejections.size)
    }

    @Test fun validateAndPass() {
        val validation = Validation(pojo)
        validation.and {
            number("age")
            string("name")
        }

        println(validation.rejections)
        assertEquals(0, validation.rejections.size)
    }

    private fun testRequired(v: Validation) {
        v.required("name")
        assertEquals(0, v.rejections.size)

        v.required("nested.name")
        assertEquals(0, v.rejections.size)

        v.required("firstName")
        assertEquals(1, v.rejections.size)

        v.required("age")
        assertEquals(1, v.rejections.size)

        v.required("zero")
        assertEquals(1, v.rejections.size)

        v.required("emptyList")

        assertEquals(2, v.rejections.size)
        v.rejections.forEach {
            assertEquals("required", it.code)
            assertTrue("'${it.field}' was not expected", listOf("firstName", "emptyList").contains(it.field))
        }

        v.arrayOf("colors") { string() }
        assertEquals(2, v.rejections.size)

        v.arrayOf("colors") { number() }
        assertEquals(3, v.rejections.size)

        v.date("ts")
        assertEquals(3, v.rejections.size)

        println(v.rejections)
    }
}

