package discord.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class DiscordAuthController {
    private final Gson gson = new Gson();
    private final HttpClient http = HttpClient.newHttpClient();

    @Value("${discord.client-id}")
    private String clientId;

    @Value("${discord.client-secret}")
    private String clientSecret;

    @PostMapping("/api/discord/token")
    public Map<String, String> exchangeToken(@RequestBody Map<String, String> body)
            throws IOException, InterruptedException {
        String code = body.get("code");

        String form = "client_id" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=authorization_code" +
                "&code=" + code;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject discordResponse = gson.fromJson(response.body(), JsonObject.class);

        return Map.of("access_token", discordResponse.get("access_token").getAsString());
    }

}
