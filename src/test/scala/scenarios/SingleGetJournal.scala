package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.json.Json
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class SingleGetJournal extends Simulation {

  val csvuser = csv("users.csv")

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  private val uri2 = baseUrl + "67128492/personal"
  private val contentType = "application/json"

  //values for setUp phase
  private val staticRequestCount = 10
  private val waitTime = 1
  private val rampUpDuration = 15
  private val maxDuration = 90
  private val usersPerSecondFrom = 1
  private val userPerSecondTo = 1

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Get_Journal_7_days")
    .exec(http("Get_Journal_67128492")
      .get(uri2)
      .header("Content-Type", contentType)
      .check(status.is(200),
        substring("testSlot")))             //check if response body contains "testSlot"
    .pause(Duration.apply(waitTime, TimeUnit.SECONDS))

  //setUp section allows to change ramp up and sets maximum duration of the test
  setUp(
    scn.
      inject(nothingFor(waitTime),
        constantUsersPerSec(usersPerSecondFrom) during rampUpDuration randomized))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
}