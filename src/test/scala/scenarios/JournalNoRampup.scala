package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class JournalNoRampup extends Simulation {
  // csv feeder currently not working csv file stored in test/resources
  val csvFeeder = csv("users.csv").circular

  //setting authorisation token --Temporary Solution--
  private val token = System.getenv("AD_JWT_TOKEN")
  val headers_10 = Map("Content-Type" -> """application/json""", "Authorization" -> token)

  //values for scenario
  private val baseUrl = System.getenv("BASE_URL")
  private val uri = baseUrl + "01234567/personal"
  private val uri2 = baseUrl + "67128492/personal"
  private val contentType = "application/json"

  //values for setUp phase
  private val maxUsers = System.getenv("NO_USERS").toInt
  private val waitTime = 5
  private val maxDuration = System.getenv("JOB_DURATION").toInt

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Get_Journal")
    feed(csvFeeder)
      .exec(http("Get_Journal")
      .get(uri)
      .headers(headers_10)
      .check(status.is(200)))
    .pause(Duration.apply(waitTime, TimeUnit.SECONDS))

  val scn2: ScenarioBuilder = scenario("Get_Journal2")
    feed(csvFeeder)
      .exec(http("Error_CSV")
      .get(baseUrl + "${user}")
      .headers(headers_10)
      .check(status.is(200)))
    .pause(Duration.apply(waitTime, TimeUnit.SECONDS))

  //Setup for users and maximum run time values
  setUp(
    scn
      .inject(
        nothingFor(waitTime),
        atOnceUsers(maxUsers)),
    scn2
      .inject(
        nothingFor(waitTime),
        atOnceUsers(maxUsers)))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))}