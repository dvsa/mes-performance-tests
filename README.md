# mes-performance-tests
=========================

	DVSA Mobile Examiner Services - back end microservice performance test suite



To test it out, execute the following command:

   $mvn gatling:test -Dgatling.simulationClass=scenarios.[TestScenatioName]
   e.g. 
   $mvn gatling:test -Dgatling.simulationClass=scenarios.JournalNoRampup

##Jenkins

Tests can be run using Jenkins job located here:
https://jenkins.mgmt.mes.dvsacloud.uk/job/Test%20Gatling/
   
   


