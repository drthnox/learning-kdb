package com.dg.learning.kdb;

import com.dg.learning.kdb.glue.KdbUtils;
import com.exxeleron.qjava.QMessagesListener;
import com.exxeleron.qjava.QType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;
import org.zeroturnaround.process.SystemProcess;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public enum QServer {

    INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(QServer.class);

    static {
        KdbUtils.canParse.put(QType.INT, KdbUtils::isInteger);
        KdbUtils.canParse.put(QType.LONG, KdbUtils::isLong);
        KdbUtils.canParse.put(QType.STRING, KdbUtils::isString);
        KdbUtils.canParse.put(QType.DATE, KdbUtils::isDate);
        KdbUtils.canParse.put(QType.DOUBLE, KdbUtils::isDouble);
    }

    static {
        for (int year = 1900; year < 2050; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int day = 1; day <= KdbUtils.daysInMonth(year, month); day++) {
                    StringBuilder date = new StringBuilder();
                    date.append(String.format("%04d", year));
                    date.append(String.format("%02d", month));
                    date.append(String.format("%02d", day));
                    KdbUtils.dates.add(date.toString());
                }
            }
        }
    }

    QMessagesListener listener;
    int port;
    //    QConnection connection;
    AbstractKdbConnection connection;
    String host;
    private StartedProcess process;

    public void start(String host, int thePort) throws Exception {
        LOGGER.debug("start({},{})", host, thePort);
        if (!isRunning()) {
            if (thePort == 0) {
                LOGGER.debug("looking for a free port...");
                port = KdbUtils.findFreePort();
                LOGGER.debug("found a free port on {}", port);
            } else {
                port = thePort;
            }
            LOGGER.info("start(): setting port: {}", port);
            startInternal();
        }
        this.host = host;
    }

    private void startInternal() throws Exception {
        if (isRunning()) {
            stop();
        }
        process = new ProcessExecutor()
                .command("/Users/davidgregson/q/m32/q", "-p", String.valueOf(port))
                .redirectOutputAlsoTo(Slf4jStream.of(LOGGER).asDebug())
                .redirectErrorAlsoTo(Slf4jStream.of(LOGGER).asError())
                .start();
    }

    public int getPort() {
        return port;
    }

    public AbstractKdbConnection getConnection() {
        LOGGER.debug("getConnection()");
        if (!isConnectionAvailable()) {
            LOGGER.debug("getConnection(): creating a new connection");
//            connection = createCallbackConnection();
//            connection = createBasicConnection();
            connection = KdbConnectionFactory.createQConnection(host, port);
            LOGGER.debug("getConnection(): new connection created: {}", connection);
        }
        try {
            connection.reset();
        } catch (Exception e) {
            LOGGER.error("Error getting connection:", e);
        }
        LOGGER.debug("getConnection(): connection = {}", connection);
        return connection;
    }

    private boolean isConnectionAvailable() {
        return connection != null && connection.isConnected();
    }

    public void stop() throws Exception {
        if (processExists()) {
            try {
                disconnect();
                Process proc = process.getProcess();
                SystemProcess systemProcess = Processes.newStandardProcess(proc);
                ProcessUtil.destroyGracefullyOrForcefullyAndWait(systemProcess, 30, TimeUnit.SECONDS, 10, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    private void disconnect() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public boolean isRunning() throws Exception {
        return processExists() && process.getProcess().isAlive();
    }

    private boolean processExists() {
        return process != null && process.getProcess() != null;
    }

    // return the last result of the queries
    public Object executeQueries(List<String> queries) throws Exception {
        Object result = null;
        if (isRunning()) {
            openConnection();
            if (connection.isConnected()) {
                for (String query : queries) {
                    result = executeQuery(query);
                }
            }
            closeConnection();
        } else {
            LOGGER.warn("Q server process not running.");
        }
        return result;
    }

    Object executeQuery(String query) throws Exception {
        return sync(query);
    }

    private void closeConnection() throws Exception {
        connection.close();
        LOGGER.debug("closing connection");
    }

    private void openConnection() throws Exception {
        LOGGER.debug("opening connection");
        connection = getConnection();
        connection.reset();
        LOGGER.debug("connection open? {}", connection.isConnected());
    }

    public String getHost() {
        return host;
    }

    Object sync(String query) throws Exception {
        LOGGER.debug("\n\tquery: {}", query);
        Object response = connection.query(query);
        LOGGER.debug("\n\tresponse: {}", response);
        return response;
    }

    //    Object sync(String query) throws Exception {
//        LOGGER.debug("\n\tquery: {}", query);
//        openConnection();
//        int msgSize = connection.query(QConnection.MessageType.SYNC, query);
//        LOGGER.debug("sent: {} bytes", msgSize);
//    low level
//    receive
    //        final QMessage message = (QMessage) connection.receive(false, false);
//    print message
//    meta data
//        LOGGER.debug("\n\tmessage type: {}\n\tsize: {}\n\tisCompressed: {}\n\tendianess: {}", message.getMessageType(), message.getMessageSize(), message.isCompressed(), message.getEndianess());
//        LOGGER.debug("\n\tdata:\n\t{}", message.getData());
//        return message.getData();
//    }

    public void loadCsv(String tableName, String csvFile) throws Exception {
        String query = getLoadCsvQuery(tableName, csvFile);
        LOGGER.debug("load csv query = {}", query);
        getConnection().reset();
        executeQuery(query);
        LOGGER.debug("Removing spaces from column names...");
        executeQuery(tableName + ":xcol[`$ssr[;\" \";\"_\"]each string cols " + tableName + ";" + tableName + "]");
//        getConnection().close();
    }

    //        data:("**I";enlist",") 0: `$"/path/to/worldPopulation.csv"
    //        (4#"*";enlist csv) 0: `:test.csv
    private String getLoadCsvQuery(String tableName, String csvFile) throws IOException {
        File file = new File(csvFile);
        LOGGER.debug("{} exists? {}", file.getAbsolutePath(), file.exists());
        String cleanFileName = FilenameUtils.separatorsToSystem(file.getAbsolutePath());
        Reader reader = new FileReader(cleanFileName);
        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        List<CSVRecord> records = parser.getRecords();
        CSVRecord r = records.get(0);
        int numCols = r.size();
        LOGGER.debug("number of records = {}", records.size());
        // get column types
        QType columnType[] = new QType[numCols];
        CSVRecord csvRecord = records.get(1);
        for (int i = 0; i < numCols; i++) {
            String record = csvRecord.get(i);
            if (KdbUtils.canParse.get(QType.INT).test(record)) {
                columnType[i] = QType.INT;
            } else if (KdbUtils.canParse.get(QType.LONG).test(record)) {
                columnType[i] = QType.LONG;
            } else if (KdbUtils.canParse.get(QType.DATE).test(record)) {
                columnType[i] = QType.DATE;
            } else if (KdbUtils.canParse.get(QType.DOUBLE).test(record)) {
                columnType[i] = QType.DOUBLE;
            } else {
                columnType[i] = QType.STRING;
            }
        }

        return getQueryString(tableName, cleanFileName, columnType);
    }

    private String getQueryString(String tableName, String cleanFileName, QType[] columnTypes) {
        StringBuilder builder = new StringBuilder(tableName)
                .append(":(");
        builder.append("\"");
        for (int i = 0; i < columnTypes.length; i++) {
            if (columnTypes[i] == QType.STRING) {
                builder.append("S");
            }
            if (columnTypes[i] == QType.INT) {
                builder.append("I");
            }
            if (columnTypes[i] == QType.DOUBLE) {
                builder.append("F");
            }
            if (columnTypes[i] == QType.LONG) {
                builder.append("J");
            }
            if (columnTypes[i] == QType.DATE) {
                builder.append("D");
            }
        }
        builder.append("\"");
//                .append(numCols)
//                .append("#\"*\"")
        builder.append("; enlist\",\") 0: `$\"")
                .append(cleanFileName)
                .append("\"");
        return builder.toString();
    }

}
