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
  // csv feeder currently not working csv file stored in test/resources
  val csvUser = csv("users.csv").circular
  private val baseUrl = System.getenv("BASE_URL")
  private val contentType = "application/json" // content type for httpProtocol

  // values for setUp phase
  // wait before scenario ends (seconds)
  private val waitTime = 1
  // max users used at once in scenario
  private val maxUsers = System.getenv("NO_USERS").toInt
  // time to ramp up users to full capacity (seconds)
  private val rampUpDuration = System.getenv("RAMP_UP_TIME").toInt
  // duration of test run (seconds)
  private val maxDuration = System.getenv("JOB_DURATION").toInt

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
      .exec(http("Get_" + "${user}")
      // get on url with endpoint from feeder
      .get(baseUrl + "${user}")
      // sets headers
      .headers(headers)
      // checks status
      .check(status.is(200),
      // checks if response body contains "testSlot"
      substring("staffNumber")))
      // wait before loop ends
      .pause(Duration.apply(waitTime, TimeUnit.SECONDS))
  }


  // setUp section allows to change ramp up and sets maximum duration of the test
  // max number of users achieved in set amount of time then simulation runs on loop until maxDuration expires
setUp(scn
  .inject(
    heavisideUsers(maxUsers) during rampUpDuration))
  .assertions(
    global.responseTime.max.lt(3000),
    global.successfulRequests.percent.gt(95))
  .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
}