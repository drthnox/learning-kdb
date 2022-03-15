package com.dg.learning.kdb.glue;

import com.dg.learning.kdb.QServer;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 10:13 AM, 04/Jun/2017.
 */
public class KdbGlueTest {

    @Before
    public void setUp() throws Exception {
        QServer.INSTANCE.start("localhost", 0);
    }

    @Test
    public void stopServer() throws Exception {
        QServer.INSTANCE.stop();
    }

    @Test
    public void theQServerIsStarted() throws Exception {
        assertThat(QServer.INSTANCE.isRunning()).isTrue();
    }

    @Test
    public void confirmQIsListeningOnARandomPort() throws Exception {
    }

    @Test
    public void aConnectionCanBeMadeToTheQServer() throws Exception {
    }

    @Test
    public void theFollowingQueryIsExecuted() throws Exception {
    }

    @Test
    public void theFollowingDataIsReturned() throws Exception {
    }

}
