package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.core.options.CurlOption
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import tech.harmonysoft.oss.mentalmate.ChunkerApplication

@CucumberContextConfiguration
@SpringBootTest(
    classes = [ ChunkerApplication::class ],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ActiveProfiles("cucumber")
class ChunkerStepDefinitions {

    @Autowired private lateinit var http: HttpClientStepDefinitions

    @When("^chunking is requested$")
    fun requestChunking() {
        http.makeRequest(CurlOption.HttpMethod.POST.name, "/v1/chunk")
    }
}