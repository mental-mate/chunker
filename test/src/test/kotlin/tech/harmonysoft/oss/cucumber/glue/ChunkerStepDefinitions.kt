package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.core.options.CurlOption
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tech.harmonysoft.oss.mentalmate.ChunkerTestApplication

@CucumberContextConfiguration
@SpringBootTest(
    classes = [ ChunkerTestApplication::class ]
)
class ChunkerStepDefinitions {

    @Autowired private lateinit var http: HttpClientStepDefinitions

    @When("^chunking is requested$")
    fun requestChunking() {
        http.makeRequest(CurlOption.HttpMethod.POST.name, "/v1/chunk")
    }
}