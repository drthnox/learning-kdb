package com.dg.learning.kdb;

import kx.c;

public class KdbKxConnection extends AbstractKdbConnection<c> {

    public KdbKxConnection(String host, int port) {
        super(host, port);
    }

    @Override
    public void reset() throws Exception {
        connection = new c(host, port);
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public void close() throws Exception {
        connection.close();
        connection = null;
    }

    @Override
    public Object query(String query, Object... parameters) throws Exception {
        return connection.k(query, parameters);
    }
}
