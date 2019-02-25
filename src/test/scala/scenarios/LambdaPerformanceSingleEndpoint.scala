package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scala.concurrent.duration.{Duration, FiniteDuration}


class LambdaPerformanceSingleEndpoint extends Simulation {

//setting authorisation token --Temporary Solution--
  private val token = System.getenv("AD_JWT_TOKEN")
  val headers = Map("Content-Type" -> """application/json""", "Authorization" -> token)

//values for scenario
  private val baseUrl = System.getenv("BASE_URL")// "https://dev.mes.dev-dvsacloud.uk/v1/"
  private val contentType = "application/json" // content type for httpProtocol

// values for setUp phase
  // csv feeder currently not working csv file stored in test/resources
  val csvUser = csv("users.csv").circular
  // wait before scenario ends (seconds)
  private val waitTime = 1
  // max users used at once in scenario
  private val maxUsers = System.getenv("NO_USERS")
  // time to ramp up users to full capacity (seconds)
  private val rampUpDuration = System.getenv("RAMP_UP_TIME")
  // duration of test run (seconds)
  private val maxDuration = System.getenv("JOB_DURATION")

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

// executable scenario section
  val scn: ScenarioBuilder = scenario("Get_Journal")
     // forever loop max duration can be found in setUp section
    .forever("Get Journal", exitASAP = true) {
     // loads values from csv
    feed(csvUser)
      exec(http("Get_Journal_67128492")
        // get on url with endpoint from feeder
        .get(baseUrl + "${user}")
        // sets headers
        .headers(headers)
        // checks status
        .check(status.is(200),
        // checks if response body contains "testSlot"
        substring("testSlot")))
        // wait before loop ends
        .pause(Duration.apply(waitTime, TimeUnit.SECONDS))
    }

// setUp section allows to change ramp up and sets maximum duration of the test
  // max number of users achieved in set amount of time then simulation runs on loop until maxDuration expires
  setUp(
    scn.
      inject(
        heavisideUsers(maxUsers) during rampUpDuration))
    .maxDuration(maxDuration, TimeUnit.SECONDS)

}