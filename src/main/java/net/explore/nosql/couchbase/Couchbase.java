package net.explore.nosql.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.GetResult;

public class Couchbase {

	public static void main(String[] args) {
        Cluster cluster = Cluster.connect("192.168.1.28", "cbuser", "cbuser");
        Bucket bucket = cluster.bucket("travel-sample");
        System.out.println(bucket.name()); 
        Collection collection = bucket.defaultCollection();
        System.out.println(collection.name());

        GetResult result = collection.get("airline_10");
        
        System.out.println(result.contentAs(String.class));
        cluster.disconnect();        
    }
    
}
