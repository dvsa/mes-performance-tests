package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class LoggingService extends Simulation {

  // setting authorisation token -- disabled, API key only required --
  // private val token = System.getenv("AD_JWT_TOKEN")
  // private API key
  private val api_key = System.getenv("X_API_KEY_LOGGING")

  // csv feeder currently not working csv file stored in test/resources
  val headers = Map("Content-Type" -> """application/json""", "x-api-key" -> api_key)

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  private val uri = baseUrl + "logs"
  private val contentType = "application/json"

  // values for setUp phase

  // max users used at once in scenario
  private val maxUsers = System.getenv("NO_USERS").toInt
  // time to ramp up users to full capacity (seconds)
  private val rampUpDuration = System.getenv("RAMP_UP_TIME").toInt
  // duration of test run (seconds)
  private val maxDuration = System.getenv("JOB_DURATION").toInt
  // requests per second
  private val requestPerSecond = System.getenv("REQUEST_SECOND").toInt

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Send_Logs")
      .forever("Send_Logs", exitASAP = true) {
          exec(http("Send_Logs")
            .post(uri)
            .headers(headers)
            // 600 KB of journal logs data
          .body(StringBody("""[{
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1566389420000
                               },
                               {
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1566389420000
                               },
                               {
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1566389420000
                               },
                               {
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1566389420000
                               }]
                           """))
            // assertion
          .check(status.is(200),
            substring("4 log messages were received and saved.")))
      }


  // setUp section allows to change ramp up and sets maximum duration of the test
  // simulation will hold given requests per second and  runs on loop until maxDuration expires
  setUp(scn
    .inject(constantUsersPerSec(maxUsers) during (rampUpDuration))) //
    .throttle(
    reachRps(requestPerSecond) in (rampUpDuration),
    holdFor(maxDuration))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
    // test will fail if more than 90% of requests don't pass validation checks
    .assertions(
    global.successfulRequests.percent.gt(90))
}
