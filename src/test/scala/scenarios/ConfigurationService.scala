package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class ConfigurationService extends Simulation {

  //setting authorisation token
  private val token = System.getenv("AD_JWT_TOKEN")
  val headers = Map("Content-Type" -> """application/json""", "Authorization" -> token)

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  // version of the app that cannot be lover that version of microservices
  private val app_version = System.getenv("APP_VERSION")
  private val uri = baseUrl + "configuration/perf" + "?app_version=" + app_version
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

  val scn: ScenarioBuilder = scenario("Get_Config")
    // forever loop during test runtime
    .forever("Get_Config", exitASAP = true) {
      exec(http("Get_Config")
        .get(uri)
        .headers(headers)
        .check(status.is(200),
          substring("journalUrl")))
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