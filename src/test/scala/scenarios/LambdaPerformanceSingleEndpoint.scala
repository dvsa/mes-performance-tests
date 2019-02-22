package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class LambdaPerformanceSingleEndpoint extends Simulation {


  val csvUser = csv("users.csv").circular // csv feeder currently not working csv file stored in test/resources

  //setting authorisation token --Temporary Solution--
  private val token = System.getenv("AD_JWT_TOKEN")
  val headers = Map("Content-Type" -> """application/json""", "Authorization" -> token)

  //values for scenario
  private val baseUrl = "https://dev.mes.dev-dvsacloud.uk/v1/"
  private val user1 = "journals/67128492/personal"// endpoint
  private val contentType = "application/json" // content type for httpProtocol

  // values for setUp phase
  private val waitTime = 1
  // wait before scenario ends (seconds)
  private val maxUsers = 15
  // max users used at once in scenario
  private val rampUpDuration = 15
  // time to ramp up users to full capacity (seconds)
  private val maxDuration = 300 // duration of test run (seconds)

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Get_Journal")
    .forever("Get Journal", exitASAP = true) {
      // forever loop max duration can be found in setUp section
      exec(http("Get_Journal_67128492")
        .get(baseUrl + user1) // get on url with endpoint
        .headers(headers) // sets headers
        .check(status.is(200), // checks status
        substring("testSlot"))) // checks if response body contains "testSlot"
        .pause(Duration.apply(waitTime, TimeUnit.SECONDS)) // wait before loop ends
    }

  //setUp section allows to change ramp up and sets maximum duration of the test
  setUp(
    scn.
      inject(
        heavisideUsers(maxUsers) during rampUpDuration)) // max number of users achieved in set amount of time then simulation runs on loo until maxDuration expires
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))

}