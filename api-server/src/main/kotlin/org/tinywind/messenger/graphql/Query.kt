package org.tinywind.messenger.graphql

import graphql.kickstart.servlet.context.DefaultGraphQLServletContext
import graphql.kickstart.tools.GraphQLQueryResolver
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component

@Component
internal class Query : GraphQLQueryResolver {
    fun test(): String {
        return "test"
    }

    fun noResponse(): String? {
        return null
    }

    fun echo(value: String): String {
        return value
    }

    fun simple(id: String): Simple {
        return Simple(id)
    }

    fun list(): List<Simple> {
        return listOf(simple("1"))
    }

    fun header(name: String?, env: DataFetchingEnvironment): String? {
        val context = env.getContext<DefaultGraphQLServletContext>()
        val headers = context.httpServletRequest.getHeaders(name)
        return if (headers != null && headers.hasMoreElements()) {
            headers.nextElement()
        } else null
    }
}
