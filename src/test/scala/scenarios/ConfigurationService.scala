package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class ConfigurationService extends Simulation {

  //setting authorisation token --Temporary Solution--
  private val token = System.getenv("AD_JWT_TOKEN")

  // csv feeder currently not working csv file stored in test/resources
  val headers_10 = Map("Content-Type" -> """application/json""", "Authorization" -> token)

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  private val uri = baseUrl + "configuration/perf"
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

  val scn: ScenarioBuilder = scenario("Get_Config")
    .forever("Get_Config", exitASAP = true) {
      exec(http("Get_Config")
        .get(uri)
        .headers(headers_10)
        .check(status.is(200),
          substring("journalUrl")))
    }


  //Setup for users and maximum run time values
  setUp(scn
    .inject(
      rampConcurrentUsers(0) to(maxUsers) during(rampUpDuration)))
    .assertions(
      global.successfulRequests.percent.gt(95))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
}