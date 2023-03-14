package de.rnd7.groheondustomqtt.grohe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class GroheTokenLogin {

    private static final String LOGIN_URL = "https://idp2-apigw.cloud.grohe.com/v3/iot/oidc/login";
    private static final String APIGW_URL = "https://idp2-apigw.cloud.grohe.com";

    private static final Pattern ACTION_PATTERN = Pattern.compile("action=\"([^\"]*)\"");

    private final String username;
    private final String password;

    public GroheTokenLogin(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public JSONObject login() throws IOException {
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build()) {
            final HttpGet get = new HttpGet(LOGIN_URL);
            try (CloseableHttpResponse response = httpclient.execute(get)) {
                final String page = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return this.login(httpclient, this.formTargetOf(page));
            }
        }
    }

    private String formTargetOf(final String page) throws IOException {
        final Matcher matcher = ACTION_PATTERN.matcher(page);

        if (matcher.find()) {
            return StringEscapeUtils.unescapeHtml4(matcher.group(1));
        } else {
            throw new IOException("Unexpected result from Grohe API (login form target url not found)");
        }
    }

    private JSONObject login(final CloseableHttpClient httpclient, final String actionUrl) throws IOException {
        final HttpPost post = new HttpPost(actionUrl);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("X-Requested-With", "XMLHttpRequest");
        post.setHeader("referer", actionUrl);
        post.setHeader("origin", APIGW_URL);

        post.setEntity(new StringEntity(this.buildLoginRequest()));

        try (CloseableHttpResponse response = httpclient.execute(post)) {
            return this.fetchToken(httpclient, this.fetchLocation(response));
        }
    }

    private String fetchLocation(final CloseableHttpResponse response) throws IOException {
        if (response.getStatusLine().getStatusCode() != 302) {
            final String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            throw new IOException("Unexpected code while fetching login location. Check your login credentials. Code: " + response.getStatusLine().getStatusCode() + " Body: " + body);
        }

        final Header[] locations = response.getHeaders("Location");
        if (locations.length == 0) {
            throw new IOException("Login location not found.");
        }

        return locations[0]
            .getValue()
            .replace("ondus://", "https://");
    }

    private String buildLoginRequest() {
        return URLEncodedUtils.format(Arrays.asList(new BasicNameValuePair("username", this.username), new BasicNameValuePair("password", this.password)), StandardCharsets.UTF_8);
    }

    private JSONObject fetchToken(final CloseableHttpClient httpclient, final String location) throws IOException {
        final HttpGet getTokens = new HttpGet(location);

        try (CloseableHttpResponse tokenResponse = httpclient.execute(getTokens)) {
            final String tokenJson = EntityUtils.toString(tokenResponse.getEntity(), StandardCharsets.UTF_8);
            return new JSONObject(tokenJson);
        }
    }
}
