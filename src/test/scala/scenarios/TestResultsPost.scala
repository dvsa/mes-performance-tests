package scenarios

import java.util.concurrent.TimeUnit
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scala.concurrent.duration.{Duration, FiniteDuration}

class TestResultsPost extends Simulation {

  // authorisation token
  private val token = System.getenv("AD_JWT_TOKEN")
  //setting authorisation token --Temporary Solution--
  val headers = Map("Content-Type" -> """application/json""", "Authorization" -> token)
  // CSV file with test results
  val csvResults = csv("results.csv").circular

  // values taken from Jenkins job parameters
  // compressed test result generated by script in jenkins
  private val compressedTestResult = System.getenv("TEST_RESULTS")
  // max users used at once in scenario
  private val maxUsers = System.getenv("NO_USERS").toInt
  // time to ramp up users to full capacity (seconds)
  private val rampUpDuration = System.getenv("RAMP_UP_TIME").toInt
  // duration of test run (seconds)
  private val maxDuration = System.getenv("JOB_DURATION").toInt
  // requests per second
  private val requestPerSecond = System.getenv("REQUEST_SECOND").toInt

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  private val uri = baseUrl + "test-results"
  private val contentType = "application/json"
  private val waitTime = 5


  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val testServicePost: ScenarioBuilder = scenario("Test_Results_Post")
    .forever("Send_Logs", exitASAP = true) {
      feed(csvResults)
      exec(http("Send_Logs")
        .post(uri)
        .headers(headers)
        // xxxxx KB of test result data
        .body(StringBody("${results}"))
        // assertion
        .check(status.is(200)
      //  substring("xxxxxxxxxxxxxxxxxx"))
      ))
  }


  // setUp section allows to change ramp up and sets maximum duration of the test
  // simulation will hold given requests per second and  runs on loop until maxDuration expires
  setUp(testServicePost
    .inject(constantUsersPerSec(maxUsers) during (rampUpDuration))) //
    .throttle(
    reachRps(requestPerSecond) in (rampUpDuration),
    holdFor(maxDuration))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
    // test will fail if more than 90% of requests don't pass validation checks
    .assertions(
    global.successfulRequests.percent.gt(90))
}
