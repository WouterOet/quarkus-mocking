package org.acme

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import java.net.URI
import javax.enterprise.context.ApplicationScoped
import javax.websocket.ClientEndpoint
import javax.websocket.ContainerProvider
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint

@ApplicationScoped
class A {
	fun run() = "real-a"
}

@ApplicationScoped
class B(private val a: A) {
	fun run() = println(a.run())
}

@ApplicationScoped
@ServerEndpoint("/tick")
class Endpoint(private val b: B) {

	@OnMessage
	fun onMessage(message: String) {
		println("Websocket")
		b.run()
	}
}

@QuarkusTest
class FullTest {

	@InjectMock
	lateinit var a: A

	@BeforeEach
	internal fun setUp() {
		`when`(a.run()).thenReturn("mock-a")
	}

	@Test
	internal fun `invocation via websocket`() {
		val client = Client()
		ContainerProvider.getWebSocketContainer().connectToServer(client, URI("ws://localhost:8081/tick"))
		client.send("message")
	}
}

@ClientEndpoint
class Client {

	private var session: Session? = null

	@OnOpen
	fun onOpen(session: Session) {
		this.session = session
	}

	fun send(message: String) {
		session?.asyncRemote?.sendText(message)
	}
}
