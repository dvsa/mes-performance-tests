package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class LambdaPerformanceSingleEndpoint extends Simulation {


  val csvUser = csv("users.csv").circular // csv feeder currently not working csv file stored in test/resources

  //setting authorisation token --Temporary Solution--
  private val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1zeE1KTUxDSURXTVRQdlp5SjZ0eC1DRHh3MCIsImtpZCI6Ii1zeE1KTUxDSURXTVRQdlp5SjZ0eC1DRHh3MCJ9.eyJhdWQiOiIwOWZkZDY4Yy00ZjJmLTQ1YzItYmU1NS1kZDk4MTA0ZDRmNzQiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC82YzQ0OGQ5MC00Y2ExLTRjYWYtYWI1OS0wYTJhYTY3ZDc4MDEvIiwiaWF0IjoxNTUwODM1NzEwLCJuYmYiOjE1NTA4MzU3MTAsImV4cCI6MTU1MDgzNzIxMCwiYWNyIjoiMSIsImFpbyI6IjQySmdZTGk3UmxGSmE3K0NHbmQyYUtpSWRVSDBjMmVkTTl1NXpUeG1abDA0K0hSL3Fpc0EiLCJhbXIiOlsicHdkIl0sImFwcGlkIjoiMDlmZGQ2OGMtNGYyZi00NWMyLWJlNTUtZGQ5ODEwNGQ0Zjc0IiwiYXBwaWRhY3IiOiIwIiwiZXh0bi5lbXBsb3llZUlkIjpbIjAxMjM0NTY3Il0sImlwYWRkciI6IjkxLjIyMi43MS45OCIsIm5hbWUiOiJNRVNCZXRhIFVzZXIgMSIsIm9pZCI6IjhlMDY0NjgwLThhYmUtNGVmOS1hY2JiLTI5ZDVhMzBhYTZlYyIsInNjcCI6IkRpcmVjdG9yeS5SZWFkLkFsbCBVc2VyLlJlYWQiLCJzdWIiOiJ2cEp3RFVvTG1pcEtETTB4TDU3ODBhazV4M2taWS1DOWlkT3RYbWNNUVVJIiwidGlkIjoiNmM0NDhkOTAtNGNhMS00Y2FmLWFiNTktMGEyYWE2N2Q3ODAxIiwidW5pcXVlX25hbWUiOiJtb2JleGFtaW5lcjFAZHZzYWdvdi5vbm1pY3Jvc29mdC5jb20iLCJ1cG4iOiJtb2JleGFtaW5lcjFAZHZzYWdvdi5vbm1pY3Jvc29mdC5jb20iLCJ1dGkiOiJjaEk0TWdwYXprT29zekVXVS1oUUFBIiwidmVyIjoiMS4wIn0.Z1FBWEA6zBj_gnidiZWcERlKvY7V-pJSDAmppBxA2yK4145khIoJR48mDxviFJtHEQoAw_ZUB2VJjXBki_o5g0IVWTelnT-_waUfF6cwlRtizmP6V1gFK7OikdfpcSjHViFJszsHKz1LfwEYFfBlO-f0ZbxdG-UcijCsvB70zUZXd7JTYBAf8USXVUgoRsIrIOXBv0XFlGUh0A_eRQ7DlxXqj5yRV7tWzH9F8wovheT_hD9JHNL4NGyVxOl0ILnCIy9aPio4nqVuuFTXz7W9snyk17jrrHI8gTSrDci23YmRX_tpTm2Jks95d-GvYVb0trOmdq9CTks0mPFPh5vWtA"
  val headers = Map("Content-Type" -> """application/json""", "Authorization" -> token)

  //values for scenario
  private val baseUrl = "https://dev.mes.dev-dvsacloud.uk/v1/"
  private val user1 = "journals/67128492/personal"// endpoint
  private val contentType = "application/json" // content type for httpProtocol

  // values for setUp phase
  private val waitTime = 1
  // wait before scenario ends (seconds)
  private val maxUsers = 15
  // max users used at once in scenario
  private val rampUpDuration = 15
  // time to ramp up users to full capacity (seconds)
  private val maxDuration = 300 // duration of test run (seconds)

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader(contentType)

  val scn: ScenarioBuilder = scenario("Get_Journal")
    .forever("Get Journal", exitASAP = true) {
      // forever loop max duration can be found in setUp section
      exec(http("Get_Journal_67128492")
        .get(baseUrl + user1) // get on url with endpoint
        .headers(headers) // sets headers
        .check(status.is(200), // checks status
        substring("testSlot"))) // checks if response body contains "testSlot"
        .pause(Duration.apply(waitTime, TimeUnit.SECONDS)) // wait before loop ends
    }

  //setUp section allows to change ramp up and sets maximum duration of the test
  setUp(
    scn.
      inject(
        heavisideUsers(maxUsers) during rampUpDuration)) // max number of users achieved in set amount of time then simulation runs on loo until maxDuration expires
    .maxDuration(FiniteDuration.apply(maxDuration, TimeUnit.SECONDS))

}