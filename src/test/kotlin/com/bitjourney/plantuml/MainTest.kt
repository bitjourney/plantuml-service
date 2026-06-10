package com.bitjourney.plantuml

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MainTest {

    private val originalErr: PrintStream = System.err
    private val errCapture = ByteArrayOutputStream()

    @Before
    fun captureStderr() {
        val tee = object : OutputStream() {
            override fun write(b: Int) {
                originalErr.write(b)
                errCapture.write(b)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                originalErr.write(b, off, len)
                errCapture.write(b, off, len)
            }
        }
        System.setErr(PrintStream(tee, true, Charsets.UTF_8))
    }

    @After
    fun verifyStderr() {
        System.setErr(originalErr)
        val stderr = errCapture.toString(Charsets.UTF_8)
        assertThat(stderr).doesNotContain("Exception")
        assertThat(stderr).doesNotContain("Error")
    }

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

        val result = main.render(DataSource(source, main.fileFormat)).toString(Charsets.UTF_8)

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

        val result = main.render(DataSource(source, main.fileFormat)).toString(Charsets.UTF_8)

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

        val result = main.render(DataSource(source, main.fileFormat)).toString(Charsets.UTF_8)

        assertThat(result).doesNotContain("Syntax error")
    }

    @Test
    fun renderAWSIcon() {
        val main = Main()

        val source = """
            @startuml
            !define AWSPuml https://raw.githubusercontent.com/awslabs/aws-icons-for-plantuml/v23.0/dist
            !include AWSPuml/AWSCommon.puml
            !include AWSPuml/Compute/Lambda.puml
            !include AWSPuml/Storage/SimpleStorageService.puml
            Lambda(fn, "Function", "function")
            SimpleStorageService(s3, "S3", "storage")
            fn --> s3
            @enduml
        """

        val result = main.render(DataSource(source, main.fileFormat)).toString(Charsets.UTF_8)
        assertThat(result).contains("Function")
        assertThat(result).contains("S3")
    }
}
