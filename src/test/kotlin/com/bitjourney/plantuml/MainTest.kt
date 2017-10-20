package com.bitjourney.plantuml

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MainTest {
    @Test
    fun renderSequenceDiagram() {
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
        assertThat(result).doesNotContain("Syntax error")
    }

    @Test
    fun renderSalt() {
        val main = Main()

        val source = """
            @startuml
            salt
            {
              Just plain text
              [This is my button]
              ()  Unchecked radio
              (X) Checked radio
              []  Unchecked box
              [X] Checked box
              "Enter text here   "
              ^This is a droplist^
            }
            @enduml
        """

        val result = main.render(DataSource(source, main.defaultConfig)).toString(Charsets.UTF_8)

        assertThat(result).doesNotContain("Syntax error")
    }

    @Test
    fun renderVersion() {
        val main = Main()

        val source = """
            @startuml
            version
            @enduml
        """

        val result = main.render(DataSource(source, main.defaultConfig)).toString(Charsets.UTF_8)

        assertThat(result).doesNotContain("Syntax error")
    }
}
