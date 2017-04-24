package com.bitjourney.plantuml

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MainTest {
    @Test
    fun render() {
        val main = Main()

        val source = """
            @startuml
            Alice -> Bob: Authentication Request
            Bob --> Alice: Authentication Response

            Alice -> Bob: Another authentication Request
            Alice <-- Bob: another authentication Response
            @enduml
        """

        val result = main.render(DataSource(source, main.defaultConfig)).toString(Charsets.UTF_8)

        assertThat(result).contains("Alice")
        assertThat(result).contains("Bob")
    }
}
