package net.ni30.bootstrap;

import io.vertx.core.json.JsonObject;

/**
 * Created by nitish.aryan on 08/12/17.
 */
public class KfkRecord {
    private Metadata metadata;
    private JsonObject data;


    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }

    public void setData(JsonObject data) {
        this.data  = data;
    }

    public JsonObject getData() {
        return this.data;
    }

    public static class Metadata {
        private String topic;
        private int partition;
        private long offset;
        private long timestamp;
        private String router;

        public Metadata(String topic, int partition, long offset, long timestamp, String router) {
            this.topic  = topic;
            this.partition = partition;
            this.offset = offset;
            this.timestamp = timestamp;
            this.router = router;
        }

        public String getTopic() {
            return this.topic;
        }

        public int getPartition() {
            return this.partition;
        }

        public long getOffset() {
            return this.offset;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public String getRouter() {
            return this.router;
        }
    }
}
