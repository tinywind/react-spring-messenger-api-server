package org.tinywind.messenger.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class MessageRepositoryTest {

    @Autowired
    lateinit var repository: MessageRepository

    @Test
    fun test_size() {
        repository.size()
    }
}
