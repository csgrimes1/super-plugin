package csg.rundeck.plugin;

import com.dtolabs.rundeck.core.plugins.configuration.Property;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import csg.rundeck.plugin.util.MapMaker;
import csg.rundeck.plugin.util.Pair;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class SuperNotifierTest {
    final static int PORT = 18089;
    final static String BASE_URL = "http://localhost:" + PORT;
    final static String URL_OK = "/webhook";
    final static String URL_500 = "/webhook500";
    final static String[] METHODS = SuperNotifier.LIST_HTTP_METHOD;

    final static HashMap<String, Object> _executionData = new HashMap<String, Object>();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Before
    public void setUp() {
        Stream.of(METHODS).forEach((method) -> {
            // Simple endpoint
            WireMock.stubFor(WireMock.request(method, WireMock.urlEqualTo(URL_OK)).atPriority(100)
                    .willReturn(WireMock.aResponse()
                            .withStatus(200)));

            // 500 Error
            WireMock.stubFor(WireMock.request(method, WireMock.urlEqualTo(URL_500))
                    .willReturn(WireMock.aResponse()
                            .withStatus(500)));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void willValidateConfig() {
        val config = MapMaker.create(Pair.of("remoteUrl", BASE_URL + URL_OK));
        val plugin = new SuperNotifier();

        val result = plugin.postNotification("trigger", _executionData, config);
        assert(result);
    }

    @Test()
    public void canCallSimpleEndpoint() {
        val body = "{\"a\": 1}";
        val contentType = "application/json";
        Stream.of(METHODS).forEach((method) -> {
            //Manually reset wiremock.
            setUp();
            final Map<String, Object> config = MapMaker.create(
                    Pair.of("remoteUrl", BASE_URL + URL_OK),
                    Pair.of("method", method),
                    Pair.of("body", body),
                    Pair.of("contentType", contentType)
            );
            final SuperNotifier plugin = new SuperNotifier();

            final boolean result = plugin.postNotification("trigger", _executionData, config);
            assert(result);
        });
        verify(exactly(1), postRequestedFor(urlEqualTo(URL_OK))
                .withHeader("Content-Type", equalTo(contentType))
                .withRequestBody(equalTo(body))
        );
        verify(exactly(1), putRequestedFor(urlEqualTo(URL_OK))
                .withHeader("Content-Type", equalTo(contentType))
                .withRequestBody(equalTo(body))
        );
        verify(exactly(1), getRequestedFor(urlEqualTo(URL_OK)));
    }

    @Test()
    public void canCanEatEndpointErrors() {
        Stream.of(METHODS).forEach((method) -> {
            final Map<String, Object> config = MapMaker.create(
                    Pair.of("remoteUrl", BASE_URL + URL_500),
                    Pair.of("method", method)
            );
            final SuperNotifier plugin = new SuperNotifier();

            final boolean result = plugin.postNotification("trigger", _executionData, config);
            assert(!result);
        });
    }

    @Test()
    public void canGetDescription() {
        val plugin = new SuperNotifier();
        val descript = plugin.getDescription();

        assertEquals(SuperNotifier.SERVICE_PROVIDER_NAME, descript.getName());
        assertEquals(SuperNotifier.SERVICE_TITLE, descript.getTitle());
        assertEquals(SuperNotifier.SERVICE_PROVIDER_DESCRIPTION, descript.getDescription());
        val props = descript.getProperties();
        assertEquals(4, props.size());

        final Map<String, Property> map = props.stream()
                .collect(Collectors.toMap(Property::getName, Function.identity()));
        assert(map.containsKey(SuperNotifier.HTTP_BODY));
        assert(map.containsKey(SuperNotifier.HTTP_CONTENT_TYPE));
        assert(map.containsKey(SuperNotifier.HTTP_METHOD));
        assert(map.containsKey(SuperNotifier.HTTP_URL));
    }

}
