package org.tinywind.messenger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    print("Hello World")
    runApplication<Application>(*args)
}
