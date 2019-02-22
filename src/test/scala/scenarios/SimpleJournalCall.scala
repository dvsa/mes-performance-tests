package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class SimpleJournalCall extends Simulation {

  val csvFeeder = csv("users.csv")

  private val baseUrl = "https://teo.mes.dev-dvsacloud.uk/v1/journals/"
  private val uri = baseUrl + "01234567/personal"
  private val uri2 = "https://teo.mes.dev-dvsacloud.uk/v1/journals/67128492/personal"
  private val contentType = "application/json"
  private val requestCount = 1
  private val maxDuration = 10

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Get_Journal")
    .exec(http("Get_Journal_01234567")
      .get(uri)
      .header("Content-Type", contentType)
      .check(status.is(200)))
    .pause(Duration.apply(5, TimeUnit.SECONDS))

  val scn2: ScenarioBuilder = scenario("Get_Journal2")
    .exec(http("Get_Journal_67128492")
      .get(uri2)
      .header("Content-Type", contentType)
      .check(status.is(200)))
    .pause(Duration.apply(5, TimeUnit.SECONDS))

  setUp(scn.inject(atOnceUsers(requestCount)),scn2.inject(atOnceUsers(requestCount)))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
}