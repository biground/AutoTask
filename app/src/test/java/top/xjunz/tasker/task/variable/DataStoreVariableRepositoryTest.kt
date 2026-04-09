package top.xjunz.tasker.task.variable

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType
import top.xjunz.tasker.task.variable.DataStoreVariableRepository.Serialization.deserializeValue
import top.xjunz.tasker.task.variable.DataStoreVariableRepository.Serialization.dtoToVariable
import top.xjunz.tasker.task.variable.DataStoreVariableRepository.Serialization.serializeValue
import top.xjunz.tasker.task.variable.DataStoreVariableRepository.Serialization.variableToDto

class DataStoreVariableRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    // --- serializeValue ---

    @Test
    fun serializeValue_null_returnsNull() {
        assertNull(serializeValue(null, VariableType.STRING))
    }

    @Test
    fun serializeValue_boolean_true() {
        assertEquals("true", serializeValue(true, VariableType.BOOLEAN))
    }

    @Test
    fun serializeValue_boolean_false() {
        assertEquals("false", serializeValue(false, VariableType.BOOLEAN))
    }

    @Test
    fun serializeValue_integer() {
        assertEquals("42", serializeValue(42L, VariableType.INTEGER))
    }

    @Test
    fun serializeValue_integer_negative() {
        assertEquals("-100", serializeValue(-100L, VariableType.INTEGER))
    }

    @Test
    fun serializeValue_decimal() {
        assertEquals("3.14", serializeValue(3.14, VariableType.DECIMAL))
    }

    @Test
    fun serializeValue_string() {
        assertEquals("hello", serializeValue("hello", VariableType.STRING))
    }

    @Test
    fun serializeValue_emptyString() {
        assertEquals("", serializeValue("", VariableType.STRING))
    }

    // --- deserializeValue ---

    @Test
    fun deserializeValue_null_returnsNull() {
        assertNull(deserializeValue(null, VariableType.STRING))
    }

    @Test
    fun deserializeValue_boolean_true() {
        assertEquals(true, deserializeValue("true", VariableType.BOOLEAN))
    }

    @Test
    fun deserializeValue_boolean_false() {
        assertEquals(false, deserializeValue("false", VariableType.BOOLEAN))
    }

    @Test
    fun deserializeValue_integer() {
        assertEquals(42L, deserializeValue("42", VariableType.INTEGER))
    }

    @Test
    fun deserializeValue_decimal() {
        assertEquals(3.14, deserializeValue("3.14", VariableType.DECIMAL))
    }

    @Test
    fun deserializeValue_string() {
        assertEquals("hello", deserializeValue("hello", VariableType.STRING))
    }

    @Test(expected = IllegalArgumentException::class)
    fun deserializeValue_invalidBoolean_throws() {
        deserializeValue("maybe", VariableType.BOOLEAN)
    }

    @Test(expected = NumberFormatException::class)
    fun deserializeValue_invalidInteger_throws() {
        deserializeValue("abc", VariableType.INTEGER)
    }

    // --- roundTrip: serializeValue ↔ deserializeValue ---

    @Test
    fun roundTrip_boolean() {
        val original = true
        val raw = serializeValue(original, VariableType.BOOLEAN)
        assertEquals(original, deserializeValue(raw, VariableType.BOOLEAN))
    }

    @Test
    fun roundTrip_integer() {
        val original = Long.MAX_VALUE
        val raw = serializeValue(original, VariableType.INTEGER)
        assertEquals(original, deserializeValue(raw, VariableType.INTEGER))
    }

    @Test
    fun roundTrip_decimal() {
        val original = -0.001
        val raw = serializeValue(original, VariableType.DECIMAL)
        assertEquals(original, deserializeValue(raw, VariableType.DECIMAL))
    }

    @Test
    fun roundTrip_string() {
        val original = "special chars: 你好 🌍"
        val raw = serializeValue(original, VariableType.STRING)
        assertEquals(original, deserializeValue(raw, VariableType.STRING))
    }

    // --- DTO 转换 ---

    @Test
    fun variableToDto_basic() {
        val variable = Variable(
            id = "v1", name = "counter",
            type = VariableType.INTEGER, value = 10L,
            scope = VariableScope.GLOBAL, persisted = true
        )
        val dto = variableToDto(variable)
        assertEquals("v1", dto.id)
        assertEquals("counter", dto.name)
        assertEquals("INTEGER", dto.type)
        assertEquals("10", dto.value)
        assertEquals("GLOBAL", dto.scope)
    }

    @Test
    fun variableToDto_nullValue() {
        val variable = Variable(
            id = "v2", name = "flag",
            type = VariableType.BOOLEAN, value = null,
            scope = VariableScope.LOCAL, persisted = false
        )
        val dto = variableToDto(variable)
        assertNull(dto.value)
    }

    @Test
    fun dtoToVariable_basic() {
        val dto = DataStoreVariableRepository.VariableDto(
            id = "v1", name = "counter",
            type = "INTEGER", value = "10", scope = "GLOBAL"
        )
        val variable = dtoToVariable(dto)
        assertEquals("v1", variable.id)
        assertEquals("counter", variable.name)
        assertEquals(VariableType.INTEGER, variable.type)
        assertEquals(10L, variable.value)
        assertEquals(VariableScope.GLOBAL, variable.scope)
        assertTrue(variable.persisted)
    }

    @Test
    fun dtoToVariable_nullValue() {
        val dto = DataStoreVariableRepository.VariableDto(
            id = "v2", name = "empty",
            type = "STRING", value = null, scope = "LOCAL"
        )
        val variable = dtoToVariable(dto)
        assertNull(variable.value)
        assertEquals(VariableType.STRING, variable.type)
        assertTrue(variable.persisted)
    }

    // --- JSON roundTrip：模拟 DataStore 读写 ---

    @Test
    fun jsonRoundTrip_fullVariable() {
        val original = Variable(
            id = "g1", name = "score",
            type = VariableType.DECIMAL, value = 99.5,
            scope = VariableScope.GLOBAL, persisted = true
        )
        val dto = variableToDto(original)
        val jsonStr = json.encodeToString(dto)
        val restored = json.decodeFromString<DataStoreVariableRepository.VariableDto>(jsonStr)
        val result = dtoToVariable(restored)

        assertEquals(original.id, result.id)
        assertEquals(original.name, result.name)
        assertEquals(original.type, result.type)
        assertEquals(original.value, result.value)
        assertEquals(original.scope, result.scope)
        assertTrue(result.persisted)
    }

    @Test
    fun jsonRoundTrip_stringWithSpecialChars() {
        val original = Variable(
            id = "s1", name = "msg",
            type = VariableType.STRING, value = """line1\n"quoted" & <tag>""",
            scope = VariableScope.LOCAL, persisted = true
        )
        val dto = variableToDto(original)
        val jsonStr = json.encodeToString(dto)
        val restored = json.decodeFromString<DataStoreVariableRepository.VariableDto>(jsonStr)
        val result = dtoToVariable(restored)

        assertEquals(original.value, result.value)
    }

    @Test
    fun jsonDecode_ignoresUnknownFields() {
        val jsonStr = """{"id":"x","name":"x","type":"BOOLEAN","value":"true","scope":"GLOBAL","extra":"ignored"}"""
        val dto = json.decodeFromString<DataStoreVariableRepository.VariableDto>(jsonStr)
        assertEquals("x", dto.id)
    }
}
