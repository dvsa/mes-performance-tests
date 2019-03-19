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

  // csv feeder currently not working csv file stored in test/resources
  val errorLog = csv("errorLog.csv").circular
  val headers_10 = Map("Content-Type" -> """application/json""", "Authorization" -> token)

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  private val uri = baseUrl + "logs"
  private val contentType = "application/json"
  //values for setUp phase
  private val maxUsers = System.getenv("NO_USERS").toInt
  private val maxDuration = System.getenv("JOB_DURATION").toInt
  private val waitTime = 1

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Send_Logs")
      .forever("Send_Logs", exitASAP = true) {
        feed(errorLog)
          .exec(http("Send_Logs")
            .post(uri)
            .headers(headers_10)
            .body(StringBody("[" + "${logs}" + "]"))
            .check(status.is(200)))
      }


  //Setup for users and maximum run time values
  setUp(
    scn
      .inject(
        nothingFor(waitTime),
        atOnceUsers(maxUsers)))
    .assertions(
      global.successfulRequests.percent.gt(90))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
}