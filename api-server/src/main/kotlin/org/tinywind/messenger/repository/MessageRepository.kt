package org.tinywind.messenger.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.tinywind.messenger.jooq.Tables.MESSAGE
import org.tinywind.messenger.jooq.tables.pojos.Message
import javax.annotation.PostConstruct

@Repository
open class MessageRepository(val create: DSLContext) {

    fun findAll(): List<Message> {
        return create.select().from(MESSAGE)
            .fetchInto(Message::class.java)
    }

    fun findById(id: Long): Message? {
        return create.select()
            .where(MESSAGE.ID.eq(id))
            .fetchOne()
            ?.into(Message::class.java)
    }

    fun size(): Int {
        return create.fetchCount(MESSAGE)
    }

    fun insert(element: Message): Long? {
        return create.insertInto(MESSAGE)
            .set(MESSAGE.CONTENT, element.content)
            .returning(MESSAGE.ID)
            .fetchOne()
            ?.value1()
    }

    fun delete(id: Long) {
        create.delete(MESSAGE)
            .where(MESSAGE.ID.eq(id))
            .execute()
    }

    @PostConstruct
    fun test() {
        println(size())
    }
}
