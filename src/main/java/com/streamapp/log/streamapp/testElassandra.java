package com.streamapp.log.streamapp;

import static com.streamapp.log.streamapp.ElassandraClient.elassandraClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ashutosh.sharma1 on 1/2/2019.
 */
public class testElassandra {
    public static void main(String[] args) throws Exception {
        ElassandraClient elassandraClient = ElassandraClient.getInstance();
        Date dt = new Date();

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("timestamp", dt.getTime());
        jsonMap.put("clientip", "172.16.41.216");
        elassandraClient.ElassandraInsert("test3", "visitor", "2", jsonMap);
    }
}