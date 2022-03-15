package com.dg.learning.kdb.glue;

import com.exxeleron.qjava.QTable;
import cucumber.api.DataTable;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.assertj.core.api.Assertions.assertThat;

public class KdbUtilsTest {
    public static Logger LOGGER = getLogger(KdbUtilsTest.class);

    private static QTable getTestTable() {
        final String[] columns = new String[]{"f", "i", "s"};
        return getTestTable(columns);
    }

    private static QTable getTestTable(final String[] columns) {
        final Object[] data = new Object[]{new double[]{-1.1, 0.0, 10.32}, new int[]{10, 0, -2}, new String[]{"foo", "bar", ""}};
        final QTable table = new QTable(columns, data);
        return table;
    }

    @Test
    public void convert() throws Exception {
        DataTable table = KdbUtils.convert(getTestTable());

        assertThat(table).isNotNull();
        LOGGER.debug("\n{}", table.toString());
    }

}
