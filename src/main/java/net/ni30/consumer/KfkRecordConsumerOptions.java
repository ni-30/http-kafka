package net.ni30.consumer;

import io.vertx.ext.web.client.WebClient;

/**
 * Created by nitish.aryan on 09/12/17.
 */
public class KfkRecordConsumerOptions {
    private String groupId;
    private WebClient webClient;
    private HttpRedirect httpRedirect;

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public WebClient getWebClient() {
        return this.webClient;
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public HttpRedirect getHttpRedirect() {
        return this.httpRedirect;
    }

    public void setHttpRedirect(HttpRedirect httpRedirect) {
        this.httpRedirect = httpRedirect;
    }

    public static class HttpRedirect {
        private String host;
        private int port;
        private String path;
        private long timeout;

        public HttpRedirect(String host, int port, String path, long timeout) {
            this.host = host;
            this.port = port;
            this.path = path;
            this.timeout = timeout;
        }

        public String getHost() {
            return this.host;
        }

        public int getPort() {
            return this.port;
        }

        public String getPath() {
            return this.path;
        }

        public long getTimeout() {
            return this.timeout;
        }
    }
}
