package es.uib.record.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.modulith.core.ApplicationModules

@SpringBootTest
class BackendApplicationTests {

	@Test
	fun contextLoads() {
	}

	@Test
	fun verifyModulithic() {
		ApplicationModules.of(BackendApplication::class.java).verify()
	}
}
