package generator

import antlr.toAst
import io.freefair.dmsdsl.antlr.DslVisitor
import io.freefair.dmsdsl.generator.JsonGenerator
import org.junit.jupiter.api.Test

class JsonGeneratorTests {
    @Test
    fun fullIntegration() {
        val text = "select %\n" +
                    "transform prefix remove %.% \"pref_\""
        val ast = text.toAst()
        val rules = DslVisitor().visit(ast)
        val json = JsonGenerator().generateJsonString(rules)

        assert(json == "{\"rules\":[{\"rule-id\":\"0\",\"rule-name\":\"select %\",\"rule-type\":\"selection\",\"object-locator\":{\"schema-name\":\"%\"},\"rule-action\":\"include\"},{\"rule-id\":\"1\",\"rule-name\":\"prefix %.%\",\"rule-type\":\"transformation\",\"rule-target\":\"table\",\"object-locator\":{\"schema-name\":\"%\",\"table-name\":\"%\"},\"rule-action\":\"remove-prefix\",\"value\":\"pref_\"}]}")
    }
}