package scenarios

import java.util.concurrent.TimeUnit

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration.{Duration, FiniteDuration}

class GetJournalRepeat extends Simulation {

  val scn = scenario("GetJournal").repeat(100, "n") {
    exec(
      http("Journal")
        .get("https://teo.mes.dev-dvsacloud.uk/v1/journals/01234567/personal")
        .header("Content-Type", "application/json")
        .check(status.is(200))
    ).pause(Duration.apply(50, TimeUnit.MILLISECONDS))
  }.repeat(3, "n") {
    exec(
      http("Journal-repeat")
        .get("https://teo.mes.dev-dvsacloud.uk/v1/journals/67128492/personal")
        .check(status.is(200))
    )
  }
    setUp(scn.inject(atOnceUsers(10))).maxDuration(FiniteDuration.apply(10, "minutes"))

}