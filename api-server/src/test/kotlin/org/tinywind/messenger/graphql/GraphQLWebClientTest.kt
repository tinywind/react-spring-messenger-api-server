package org.tinywind.messenger.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLRequest
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.util.AssertionErrors
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
internal class GraphQLWebClientTest {

    @LocalServerPort
    var randomServerPort = 0

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var graphqlClient: GraphQLWebClient

    @BeforeEach
    fun beforeEach() {
        val webClient = WebClient.builder()
            .baseUrl("http://localhost:$randomServerPort/graphql")
            .build()
        graphqlClient = GraphQLWebClient.newInstance(webClient, objectMapper)
    }

    @Test
    fun queryWithoutVariablesSucceeds() {
        val response = graphqlClient.post("query-test.graphql", null, String::class.java)
        Assertions.assertNotNull(response, "response should not be null")
        Assertions.assertEquals("test", response.block(), "response should equal 'test'")
    }

    @Test
    fun echoStringSucceeds() {
        val response = graphqlClient.post("query-echo.graphql", mapOf("value" to "echo echo echo"), String::class.java)
        Assertions.assertNotNull(response, "response should not be null")
        Assertions.assertEquals("echo echo echo", response.block())
    }

    @Test
    fun simpleTypeSucceeds() {
        val response = graphqlClient.post("query-simple.graphql", mapOf("id" to "my-id"), Simple::class.java)
        Assertions.assertNotNull(response, "response should not be null")
        val simple = response.block()
        Assertions.assertNotNull(simple)
        Assertions.assertEquals("my-id", simple!!.id, "response id should equal 'my-id'")
    }

    @Test
    fun simpleTypeAsRequestSucceeds() {
        val request = GraphQLRequest.builder()
            .resource("query-simple.graphql")
            .variables(mapOf("id" to "my-id"))
            .build()
        val response = graphqlClient.post(request)
        Assertions.assertNotNull(response, "response should not be null")
        val graphQLResponse = response.block()
        Assertions.assertNotNull(graphQLResponse)
        val simple: Simple = graphQLResponse!!["simple", Simple::class.java]
        Assertions.assertEquals("my-id", simple.id, "response id should equal 'my-id'")
    }

    @Test
    fun errorResponseSucceeds() {
        val response = graphqlClient.post("error.graphql", String::class.java)
        Assertions.assertThrows(GraphQLErrorsException::class.java) { response.block() }
    }

    @Test
    fun noResponseSucceeds() {
        val response = graphqlClient.post("query-noResponse.graphql", String::class.java)
        val noResponse = response.blockOptional()
        AssertionErrors.assertTrue("response should be empty", noResponse.isEmpty)
    }

    @Test
    fun listSucceeds() {
        val response = graphqlClient.flux("query-list.graphql", Simple::class.java)
        val list = response.collectList().block()
        Assertions.assertNotNull(list)
        Assertions.assertEquals(1, list!!.size)
    }

    @Test
    fun headerIsAdded() {
        val request = GraphQLRequest.builder()
            .resource("query-header.graphql")
            .variables(mapOf("name" to "my-custom-header"))
            .header("my-custom-header", "my-custom-header-value")
            .build()
        val response = graphqlClient.post(request)
        Assertions.assertNotNull(response, "response should not be null")
        val graphQLResponse = response.block()
        Assertions.assertNotNull(graphQLResponse)
        Assertions.assertEquals(
            "my-custom-header-value",
            graphQLResponse!!.get("header", String::class.java), "response should equal 'my-custom-header-value'"
        )
    }
}
