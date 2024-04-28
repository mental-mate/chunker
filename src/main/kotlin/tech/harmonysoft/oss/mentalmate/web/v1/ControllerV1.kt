package tech.harmonysoft.oss.mentalmate.web.v1

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.harmonysoft.oss.mentalmate.storage.service.StorageService

@RestController
@RequestMapping("/v1")
class ControllerV1(
    private val service: StorageService
) {

    @PostMapping("/chunk")
    suspend fun process(): ResponseEntity<*> {
        val result = service.chunkIfNecessary()
        return if (result.success) {
            ResponseEntity.ok(result.successValue.map { "${it.parent.path}/${it.name}" })
        } else {
            ResponseEntity.internalServerError().body(result.failureValue)
        }
    }
}