package com.dg.learning.kdb;

import org.apache.logging.log4j.Logger;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.assertj.core.api.Assertions.assertThat;

public class QServerTest {
    public static Logger LOGGER = getLogger(QServerTest.class);

    private QServer testSubject = QServer.INSTANCE;

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            LOGGER.info("Running test {}: SERVER: {}:{}", method.getName(), testSubject.getHost(), testSubject.port);
        }

        public void succeeded(FrameworkMethod method) {
            LOGGER.info("Completed test {}:", method.getName());
        }

        public void failed(Throwable e, FrameworkMethod method) {
            LOGGER.error("Test {} failed! Reason: {}", method.getName(), e.getMessage());
        }
    };

    @After
    public void tearDown() throws Exception {
        testSubject.stop();
        testSubject.port = 0;
        testSubject.connection = null;
    }

    @Before
    public void setUp() throws Exception {
        testSubject.connection = null;
        testSubject.port = 0;
        testSubject.start("localhost", 0);
        LOGGER.debug("server started: {}:{}", testSubject.getHost(), testSubject.getPort());
    }

    @Test
    public void start() throws Exception {
        assertThat(testSubject.isRunning()).isTrue();
    }

    @Test
    public void getPort() throws Exception {
        assertThat(testSubject.getPort()).isNotEqualTo(0);
    }

    @Test
    public void getConnection() throws Exception {
        AbstractKdbConnection connection = testSubject.getConnection();

        assertThat(connection).isNotNull();
        assertThat(connection.getHost()).isEqualTo("localhost");
        assertThat(connection.getPort()).isEqualTo(testSubject.getPort());
    }

    @Test
    public void isRunning() throws Exception {
        assertThat(testSubject.isRunning()).isTrue();
    }

    @Test
    public void stop() throws Exception {
        testSubject.stop();

        assertThat(testSubject.isRunning()).isFalse();
    }

    @Test
    public void executeQueries() throws Exception {
        testSubject.getConnection();
        assertThat(testSubject.connection).isNotNull();
        String[] s = {
                "n:100;",
                "item:`apple`banana`orange`pear;",
                "city:`beijing`chicago`london`paris;",
                "tab:([]time:asc n?0D0;n?item;amount:n?100;n?city);",
                "select from tab where item=`banana"// note the lack of a semi-colon at the end
        };
        List<String> queries = Lists.newArrayList(s);

        Object result = testSubject.executeQueries(queries);

        assertThat(result).isNotNull();
    }

}
