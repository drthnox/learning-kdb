package com.dg.learning.kdb.glue;

import com.dg.learning.kdb.QServer;
import com.exxeleron.qjava.QTable;
import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.logging.log4j.LogManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KdbGlue {
    public static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(KdbGlue.class);

    private final QServer qServer = QServer.INSTANCE;
    private DataTable resultDataTable;

    @Before
    public void setUp(Scenario scenario) {
        LOGGER.debug("\n\nScenario:{}\n", scenario.getName());
    }

    @Given("^the Q server is started on port (\\d+)$")
    public void theQServerIsStarted(int port) throws Throwable {
        LOGGER.info("starting q on {}", port);
        qServer.start("localhost", port);
        assertThat(qServer.isRunning()).isTrue();
    }

    @Then("^confirm Q is listening on a random port$")
    public void confirmQIsListeningOnARandomPort() throws Throwable {
        assertThat(qServer.getPort()).isNotEqualTo(0);
    }

    @And("^a connection can be made to the Q server$")
    public void aConnectionCanBeMadeToTheQServer() throws Throwable {
        assertThat(qServer.getConnection()).isNotNull();
    }

    @When("^the following query is executed:$")
    public void theFollowingQueryIsExecuted(DataTable query) throws Throwable {
        List<String> queries = query.asList(String.class);
        Object result = qServer.executeQueries(queries);
        if (result instanceof QTable) {
            resultDataTable = KdbUtils.convert((QTable) result);
        }
    }

    @Then("^the following data is returned:$")
    public void theFollowingDataIsReturned(DataTable expected) throws Throwable {
        assertThat(resultDataTable).isNotNull();
        resultDataTable.diff(expected);
    }

    @Given("^the following CSV file is loaded into KDB as '(.+)': (.+)$")
    public void theFollowingCSVFileIsLoadedIntoKDB(String tableName, String csvFile) throws Throwable {
        qServer.loadCsv(tableName, csvFile);
    }

    @When("^the metadata for table '(.+)' is queried$")
    public void theMetadataForTaleDataIsQueried(String tableName) throws Throwable {
        List<String> queries = Lists.newArrayList();
        queries.add("m: meta " + tableName);
        queries.add("m: flip 0!m");
        Object result = qServer.executeQueries(queries);
        if (result instanceof QTable) {
            resultDataTable = KdbUtils.convert((QTable) result);
        }
    }
}
