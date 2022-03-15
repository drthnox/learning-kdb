package com.dg.learning.kdb;

public class KdbConnectionFactory {
    public static KdbQConnection createQConnection(String host, int port) {
        return new KdbQConnection(host, port);
    }

}
