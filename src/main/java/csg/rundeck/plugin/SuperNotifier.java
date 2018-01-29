package csg.rundeck.plugin;

import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SuperNotifier implements NotificationPlugin{
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public SuperNotifier(){

    }

    static final String SERVICE_PROVIDER_NAME="HttpNotification";
    static final String  SERVICE_TITLE="Http Notification";
    static final String  SERVICE_PROVIDER_DESCRIPTION="Sends HTTP Notifications";
    static final String[] LIST_HTTP_METHOD = {"GET", "POST", "PUT"};
    static final String[] LIST_HTTP_CONTENT_TYPE = {"application/json", "application/xml"};

    static final String HTTP_URL="remoteUrl";
    static final String HTTP_METHOD="method";
    static final String HTTP_CONTENT_TYPE="contentType";
    static final String HTTP_BODY="body";

    private static final Description DESCRIPTION = DescriptionBuilder.builder()
            .name(SERVICE_PROVIDER_NAME)
            .title(SERVICE_TITLE)
            .description(SERVICE_PROVIDER_DESCRIPTION)
            .property(PropertyBuilder.builder()
                    .string(HTTP_URL)
                    .title("Remote URL")
                    .description("HTTP URL to which to make the request.")
                    .required(true)
                    .build())
            .property(PropertyBuilder.builder()
                    .select(HTTP_METHOD)
                    .title("HTTP Method")
                    .description("HTTP method used to make the request.")
                    .required(true)
                    .defaultValue("GET")
                    .values(LIST_HTTP_METHOD)
                    .build())
            .property(PropertyBuilder.builder()
                    .freeSelect(HTTP_CONTENT_TYPE)
                    .title("Content Type")
                    .description("HTTP Content Type.")
                    .required(true)
                    .defaultValue("application/json")
                    .values(LIST_HTTP_CONTENT_TYPE)
                    .build())
            .property(PropertyBuilder.builder()
                    .string(HTTP_BODY)
                    .title("Body")
                    .description("Add Body.")
                    .renderingAsTextarea()
                    .build())
            .build();


    public Description getDescription() {
        return DESCRIPTION;
    }

    private static String insertVariableReplacements(Map executionData, Map config) {
        val bodyStr = config.containsKey(HTTP_BODY) ? config.get(HTTP_BODY).toString() : "";
        if (bodyStr.length() == 0)
            return bodyStr;

        /*  In a more complete implementation, this is where I would code text replacements in
            the body that we pass to POST and PUT. Example:
            '{"url": "${config.remoteUrl}"}' => '{"url": "http://foo/bar"}'
         */
        return bodyStr;
    }

    @SneakyThrows(IllegalArgumentException.class)
    public boolean postNotification(String trigger, Map executionData, Map config) {
        val remoteUrl = config.containsKey(HTTP_URL) ? config.get(HTTP_URL).toString() : null;
        val method = config.containsKey(HTTP_METHOD) ? config.get(HTTP_METHOD).toString() : null;
        val contentTypeStr = config.containsKey(HTTP_CONTENT_TYPE) ? config.get(HTTP_CONTENT_TYPE).toString() : null;
        val bodyStr = insertVariableReplacements(executionData, config);
        val content = getContent(method, contentTypeStr, bodyStr);

        if(remoteUrl == null || method == null) {
            throw new IllegalArgumentException("Remote URL and Method are required.");
        }

        try {
            final HttpRequest request = startRequest(remoteUrl, method, content);
            request.execute();
            return true;
        } catch(IOException ex) {
            System.err.printf("Your postNotification exception is %s", ex);
            return false;
        }
    }

    private HttpContent getContent(String method, String contentType, String body) {
        return method == "GET"
                ? new EmptyContent()
                : new ByteArrayContent(contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private HttpRequest startRequest(String url, String method, HttpContent content) throws IOException {
        val requestFactory =
                HTTP_TRANSPORT.createRequestFactory();
        val targetUrl = new GenericUrl(url);
        return requestFactory.buildRequest(method, targetUrl, content);
    }

}