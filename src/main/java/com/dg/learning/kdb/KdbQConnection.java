package com.dg.learning.kdb;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;

public class KdbQConnection extends AbstractKdbConnection<QConnection> {
    public KdbQConnection(String host, int port) {
        super(host, port);
        connection = createBasicConnection();
    }

    @Override
    public void reset() throws Exception {
        connection.reset();
    }

    @Override
    public boolean isConnected() {
        return connection.isConnected();
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    @Override
    public Object query(String query, Object... parameters) throws Exception {
        return connection.sync(query, parameters);
    }


    private QBasicConnection createBasicConnection() {
        return new QBasicConnection("localhost", port, "", "");
    }


//    private void createCallbackConnection() {
//        connection = new QCallbackConnection("localhost", port, "", "");
//        listener = new QMessagesListener() {
//
//            // definition of messageListener that prints every message it gets on stdout
//            public void messageReceived(final QMessage message) {
//                LOGGER.debug("{}", String.format("Asynchronous message received.\nmessage type: %1s size: %2d isCompressed: %3b endianess: %4s",
//                        message.getMessageType(), message.getMessageSize(), message.isCompressed(), message.getEndianess()));
//                LOGGER.debug("Result: {}", (message.getData().toString()));
//            }
//
//            public void errorReceived(final QErrorMessage message) {
//                System.err.println((message.getCause()));
//            }
//        };
//        ((QCallbackConnection) connection).addMessagesListener(listener);
//        ((QCallbackConnection) connection).startListener();
//    }
}
