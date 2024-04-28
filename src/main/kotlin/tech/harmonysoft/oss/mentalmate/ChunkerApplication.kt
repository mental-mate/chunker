package tech.harmonysoft.oss.mentalmate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan("tech.harmonysoft.oss")
@SpringBootApplication
class ChunkerApplication

fun main(args: Array<String>) {
    runApplication<ChunkerApplication>(*args)
}

