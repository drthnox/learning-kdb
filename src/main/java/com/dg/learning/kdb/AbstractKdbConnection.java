package com.dg.learning.kdb;

public abstract class AbstractKdbConnection<T> {

    protected T connection;
    protected String host;
    protected int port;

    public AbstractKdbConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public abstract void reset() throws Exception;

    public abstract boolean isConnected();

    public abstract void close() throws Exception;

    public abstract Object query(String query, Object... parameters) throws Exception;
}
