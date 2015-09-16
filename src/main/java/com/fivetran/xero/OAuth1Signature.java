package com.fivetran.xero;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.glassfish.jersey.oauth1.signature.HmaSha1Method;
import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;
import org.glassfish.jersey.oauth1.signature.OAuth1Request;
import org.glassfish.jersey.oauth1.signature.OAuth1Secrets;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

class OAuth1Signature implements ClientRequestFilter {
    private final String consumerKey, consumerSecret, oauthToken, oauthTokenSecret;

    OAuth1Signature(String consumerKey, String consumerSecret, String oauthToken, String oauthTokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
    }

    @Override
    public void filter(ClientRequestContext r) throws IOException {
        URI uri = r.getUri();
        URI noQuery = UriBuilder.fromUri(uri).replaceQuery("").build();
        OAuth1Parameters params = new OAuth1Parameters();

        params.setConsumerKey(consumerKey);
        params.setToken(oauthToken);
        params.setSignatureMethod("HMAC-SHA1");
        params.setTimestamp();
        params.setNonce();
        params.setVersion();

        List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, "UTF-8");

        for (NameValuePair q : queryParams)
            params.put(q.getName(), q.getValue());

        OAuth1Secrets secrets = new OAuth1Secrets().consumerSecret(consumerSecret)
                                                   .tokenSecret(oauthTokenSecret);

        String signature = new HmaSha1Method().sign(OAuth1Util.baseString(r.getMethod().toUpperCase(), noQuery, params), secrets);

        params.setSignature(signature);

        params.writeRequest(new OAuth1Request() {
            @Override
            public String getRequestMethod() {
                throw new UnsupportedOperationException();
            }

            @Override
            public URL getRequestURL() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<String> getParameterNames() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<String> getParameterValues(String name) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<String> getHeaderValues(String name) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addHeaderValue(String name, String value) throws IllegalStateException {
                r.getHeaders().putSingle(name, value);
            }
        });
    }
}
