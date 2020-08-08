package com.github.redouane59.twitter.helpers;

import com.github.redouane59.twitter.TwitterClient;
import lombok.CustomLog;
import org.apache.http.client.utils.URIBuilder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

@CustomLog
public class RequestHelperV2 extends AbstractRequestHelper {

    private int sleepTime = 5;
    public String bearerToken;
    private HttpClient httpClient = HttpClient.newHttpClient();

    public RequestHelperV2(String token){
        bearerToken = token;
    }

    public <T> Optional<T> executeGetRequestWithParameters(String url, Map<String, String> parameters, Class<T> classType) {
        T result = null;
        try {
            URIBuilder builder = new URIBuilder(url);
            for(Map.Entry<String, String> e : parameters.entrySet()){
                builder.addParameter(e.getKey(), e.getValue());
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(builder.build())
                    .headers("Authorization", "Bearer " + bearerToken)
                    .build();
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String stringResponse = response.body().toString();
            if (response.statusCode()==429){
                this.wait(sleepTime, stringResponse, url);
                return this.executeGetRequestWithParameters(url, parameters, classType);
            }
            LOGGER.info(()->stringResponse);
            result = TwitterClient.OBJECT_MAPPER.readValue(stringResponse, classType);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return Optional.ofNullable(result);
    }

}
