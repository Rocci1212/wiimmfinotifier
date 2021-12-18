package wiimmfi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exceptions.FlareSolverrException;
import kernel.Config;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FlareSolverrGatewayManager {
    public static FlareSolverrGatewayManager instance = new FlareSolverrGatewayManager();

    private FlareSolverrGatewayManager() {
    }

    public void recreateBrowserSession() throws IOException, InterruptedException {
        try {
            destroyBrowserSession();
        } catch (final FlareSolverrException e) {
            System.err.println("[FlareSolverr] On Browser Session Destroy : " + e.getMessage());
        }
        createBrowserSession();
    }

    private void createBrowserSession() throws IOException, InterruptedException {
        var values = new HashMap<String, String>() {{
            put("cmd", "sessions.create");
            put("session", Config.flareSolverrSession);
        }};
        final JsonObject jsonObject = makeRequest(values);
        final String status = jsonObject.get("status").getAsString();
        if (!status.equalsIgnoreCase("ok")) {
            final String errorMessage = jsonObject.get("message").getAsString();
            throw new FlareSolverrException(errorMessage);
        }
    }

    private void destroyBrowserSession() throws IOException, InterruptedException {
        var values = new HashMap<String, String>() {{
            put("cmd", "sessions.destroy");
            put("session", Config.flareSolverrSession);
        }};
        final JsonObject jsonObject = makeRequest(values);
        final String status = jsonObject.get("status").getAsString();
        if (!status.equalsIgnoreCase("ok")) {
            final String errorMessage = jsonObject.get("message").getAsString();
            throw new FlareSolverrException(errorMessage);
        }
    }

    public Document accessWiimmfiUsingFlareSolverr() throws IOException, InterruptedException {
        var values = new HashMap<String, String>() {{
            put("cmd", "request.get");
            put("url", Config.wiimmfiFullGamesListPath);
            put("session", Config.flareSolverrSession);
            put("maxTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(Config.flareSolverrRequestMaxTimeout)));
        }};
        final JsonObject jsonObject = makeRequest(values);
        final String status = jsonObject.get("status").getAsString();
        if (!status.equalsIgnoreCase("ok")) {
            final String errorMessage = jsonObject.get("message").getAsString();
            throw new FlareSolverrException(errorMessage);
        }
        final Document doc = Jsoup.parse(jsonObject.get("solution").getAsJsonObject().get("response").getAsString());
        doc.setBaseUri(Config.wiimmfiBaseUrl);
        return doc;
    }

    private JsonObject makeRequest(Map<String, String> values) throws IOException, InterruptedException {
        var objectMapper = new ObjectMapper();
        final String requestBody = objectMapper
                .writeValueAsString(values);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(Config.flareSolverrUrl))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
