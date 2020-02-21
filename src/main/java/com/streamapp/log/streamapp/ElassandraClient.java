package com.streamapp.log.streamapp;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import java.util.Map;

/**
 * Created by ashutosh.sharma1 on 12/31/2018.
 */
public class ElassandraClient {

    public static ElassandraClient elassandraClient;

    private ElassandraClient() {  }

    public static ElassandraClient getInstance() {
        if (elassandraClient==null)
        {
            elassandraClient=new  ElassandraClient();
        }
        return elassandraClient;
    }

    private static RestHighLevelClient getClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("172.16.41.216", 9200, "http")));
        return client;
    }

    public void ElassandraInsert(String indexName, String indexType, String document_id, Map<String, Object> jsonMap) {
        RestHighLevelClient client = this.getClient();
        IndexRequest indexRequest = null;
        if(document_id!=null)
            indexRequest = new IndexRequest(indexName, indexType, document_id).source(jsonMap);
        else
            indexRequest = new IndexRequest(indexName, indexType).source(jsonMap);

        client.indexAsync(indexRequest, RequestOptions.DEFAULT, getListener());
    }

    private static ActionListener getListener() {
        ActionListener listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                String index = indexResponse.getIndex();
                String type = indexResponse.getType();
                String id = indexResponse.getId();
                long version = indexResponse.getVersion();
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {

                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {

                }
                ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

                }
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure :
                            shardInfo.getFailures()) {
                        String reason = failure.reason();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Request Failed");
                e.printStackTrace();
            }
        };
        return listener;
    }
}

    /*    Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kimchy");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "trying out Elasticsearch");
        IndexRequest indexRequest = new IndexRequest("test", "doc", "2")
                .source(jsonMap);

        client.indexAsync(indexRequest, RequestOptions.DEFAULT, listener);
*/