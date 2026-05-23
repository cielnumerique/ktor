package com.example

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ServerTest {

    @Test
    fun `POST compute returns Accepted`() = testApplication {
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }
        val response = client.post("/compute") {
            contentType(ContentType.Application.Json)
            setBody(ComputeRequest(task = "matrix_transpose", matrixA = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))))
        }
        assertEquals(HttpStatusCode.Accepted, response.status)
        assertNotNull(response.body<SubmitResponse>().id)
    }

    @Test
    fun `GET result unknown id returns 404`() = testApplication {
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }
        assertEquals(HttpStatusCode.NotFound, client.get("/result/no-such-id").status)
    }

    @Test
    fun `determinant of 3x3 is correct`() = testApplication {
        application { module() }
        val client = createClient { install(ContentNegotiation) { json() } }
        val id = client.post("/compute") {
            contentType(ContentType.Application.Json)
            setBody(ComputeRequest(
                task = "determinant",
                matrixA = listOf(listOf(1.0, 2.0, 3.0), listOf(0.0, 4.0, 5.0), listOf(1.0, 0.0, 6.0))
            ))
        }.body<SubmitResponse>().id
        runBlocking { delay(300) }
        val result = client.get("/result/$id").body<ResultResponse>()
        assertEquals(ComputationStatus.DONE, result.status)
        assertEquals(22.0, result.scalar!!, absoluteTolerance = 1e-9)
    }
}
