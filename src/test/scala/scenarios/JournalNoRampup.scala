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
  private val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1zeE1KTUxDSURXTVRQdlp5SjZ0eC1DRHh3MCIsImtpZCI6Ii1zeE1KTUxDSURXTVRQdlp5SjZ0eC1DRHh3MCJ9.eyJhdWQiOiIwOWZkZDY4Yy00ZjJmLTQ1YzItYmU1NS1kZDk4MTA0ZDRmNzQiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC82YzQ0OGQ5MC00Y2ExLTRjYWYtYWI1OS0wYTJhYTY3ZDc4MDEvIiwiaWF0IjoxNTUwODM3NTM5LCJuYmYiOjE1NTA4Mzc1MzksImV4cCI6MTU1MDgzOTAzOSwiYWNyIjoiMSIsImFpbyI6IjQySmdZUENxeURnbU1vRnhXV2JoL25XcnZmUkRyOVl1amZ2a2YxMXRqMUVuUzQzTjIzQUEiLCJhbXIiOlsicHdkIl0sImFwcGlkIjoiMDlmZGQ2OGMtNGYyZi00NWMyLWJlNTUtZGQ5ODEwNGQ0Zjc0IiwiYXBwaWRhY3IiOiIwIiwiZXh0bi5lbXBsb3llZUlkIjpbIjAxMjM0NTY3Il0sImlwYWRkciI6IjkxLjIyMi43MS45OCIsIm5hbWUiOiJNRVNCZXRhIFVzZXIgMSIsIm9pZCI6IjhlMDY0NjgwLThhYmUtNGVmOS1hY2JiLTI5ZDVhMzBhYTZlYyIsInNjcCI6IkRpcmVjdG9yeS5SZWFkLkFsbCBVc2VyLlJlYWQiLCJzdWIiOiJ2cEp3RFVvTG1pcEtETTB4TDU3ODBhazV4M2taWS1DOWlkT3RYbWNNUVVJIiwidGlkIjoiNmM0NDhkOTAtNGNhMS00Y2FmLWFiNTktMGEyYWE2N2Q3ODAxIiwidW5pcXVlX25hbWUiOiJtb2JleGFtaW5lcjFAZHZzYWdvdi5vbm1pY3Jvc29mdC5jb20iLCJ1cG4iOiJtb2JleGFtaW5lcjFAZHZzYWdvdi5vbm1pY3Jvc29mdC5jb20iLCJ1dGkiOiJ2NDVheGR2N3NVcU44dEpsMGtsUUFBIiwidmVyIjoiMS4wIn0.GCHYGp7_W8MYx2hlOuuHrgRury68w__Y36Qw70oMruaJeEwc_Ub4BFaBGOfcsjfY3mQkBcmvnct8a2MTb1Xgid0A2yJ-dwMe7ljrTyb9ydlHRMxHcfDntAiCDlNFAnrlKHKdQidGf8vYopV28kbLwMilH6mbiwfdh9QFtmYYFggPqnwCYL2ENoTzyPC2FWMaNk1Hejr0iqmgjfW1FMQ4TVUapRlvuxKy-hJwkh4pCeffWYbqru1Mi1frr-C-JyvY-pdQZcijk4aQrCGx7bSfuuhkBEMug7woLeeUVXAi4J6XTW6zDBE2T6fZrQ2MhUINpzlvL2kjBueBSL5ejILt3A"
  val headers_10 = Map("Content-Type" -> """application/json""", "Authorization" -> token)

  //values for scenario
  private val baseUrl = "https://tom.mes.dev-dvsacloud.uk/v1/journals/"
  private val uri = baseUrl + "01234567/personal"
  private val uri2 = baseUrl + "67128492/personal"
  private val contentType = "application/json"

  //values for setUp phase
  private val staticRequestCount = 2
  private val waitTime = 5
  private val maxDuration = 30

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Get_Journal")
      .feed(csvFeeder)
    .exec(http("Get_Journal")
      .get(uri)
      .headers(headers_10)
      .check(status.is(200)))
    .pause(Duration.apply(waitTime, TimeUnit.SECONDS))

  val scn2: ScenarioBuilder = scenario("Get_Journal2")
    .feed(csvFeeder)
    .exec(http("Dupa")
      .get("${user}")
      .headers(headers_10)
      .check(status.is(200)))
    .pause(Duration.apply(waitTime, TimeUnit.SECONDS))

  //Setup for users and maximum run time values
  setUp(scn.
    inject(nothingFor(waitTime),
      atOnceUsers(staticRequestCount)),
    scn2.
      inject(nothingFor(waitTime),
        atOnceUsers(staticRequestCount)))
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))
}