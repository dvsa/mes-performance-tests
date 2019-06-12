package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class LoggingService extends Simulation {

  //setting authorisation token --Temporary Solution--
  private val token = System.getenv("AD_JWT_TOKEN")
    private val api_key = System.getenv("X_API_KEY_LOGGING")

  // csv feeder currently not working csv file stored in test/resources
  val headers_10 = Map("Content-Type" -> """application/json""", "x-api-key" -> api_key)

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  private val uri = baseUrl + "logs"
  private val contentType = "application/json"
  //values for setUp phase
  private val maxUsers = System.getenv("NO_USERS").toInt
  private val maxDuration = System.getenv("JOB_DURATION").toInt
  private val rampUpDuration = System.getenv("RAMP_UP_TIME").toInt
  private val waitTime = 1

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Send_Logs")
      .forever("Send_Logs", exitASAP = true) {
          exec(http("Send_Logs")
            .post(uri)
            .headers(headers_10)
          .body(StringBody([{
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1552994170000
                               },
                               {
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1552994170000
                               },
                               {
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1552994170000
                               },
                               {
                               "type": "info",
                               "message": "DE with id: 47182032 - [JournalPage] Load Journal Test",
                               "timestamp": 1552994170000
                               }]
                           ))
          .check(status.is(200),
            substring("received and saved.")))
      }


  //Setup for users and maximum run time values
  setUp(scn
    .inject(
      rampConcurrentUsers(0) to(maxUsers) during(rampUpDuration)))
    .assertions(
      global.successfulRequests.percent.gt(95))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
}

