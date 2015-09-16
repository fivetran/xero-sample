package com.fivetran.xero;

import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;
import org.glassfish.jersey.uri.UriComponent;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class OAuth1Util {
    private static final Logger LOG = Logger.getLogger(OAuth1Util.class.getName());

    /**
     * Assembles request base string for which a digital signature is to be
     * generated/verified, per section 9.1.3 of the OAuth 1.0 specification.
     */
    public static String baseString(String requestMethod,
                              URI requestUri,
                              final OAuth1Parameters params) {
        // HTTP request method
        final StringBuilder builder = new StringBuilder(requestMethod.toUpperCase());

        // request URL, see section 3.4.1.2 http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.4.1.2
        builder.append('&').append(UriComponent.encode(requestUri.toASCIIString(),
                                                       UriComponent.Type.UNRESERVED));

        // normalized request parameters, see section 3.4.1.3.2 http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.4.1.3.2
        builder.append('&').append(UriComponent.encode(normalizeParameters(params),
                                                       UriComponent.Type.UNRESERVED));

        String base = builder.toString();

        LOG.info(base);

        return base;
    }

    /**
     * Collects, sorts and concetenates the request parameters into a
     * normalized string, per section 9.1.1. of the OAuth 1.0 specification.
     */
    private static String normalizeParameters(OAuth1Parameters params) {

        final ArrayList<String[]> list = new ArrayList<String[]>();

        // parameters in the OAuth HTTP authorization header
        for (final String key : params.keySet()) {

            // exclude realm and oauth_signature parameters from OAuth HTTP authorization header
            if (key.equals(OAuth1Parameters.REALM) || key.equals(OAuth1Parameters.SIGNATURE)) {
                continue;
            }

            final String value = params.get(key);

            // Encode key and values as per section 3.6 http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.6
            if (value != null) {
                addParam(key, value, list);
            }
        }

        // sort name-value pairs by name
        Collections.sort(list, new Comparator<String[]>() {
            @Override
            public int compare(final String[] t, final String[] t1) {
                final int c = t[0].compareTo(t1[0]);
                return c == 0 ? t[1].compareTo(t1[1]) : c;
            }
        });

        final StringBuilder buf = new StringBuilder();

        // append each name-value pair, delimited with ampersand
        for (final Iterator<String[]> i = list.iterator(); i.hasNext(); ) {
            final String[] param = i.next();
            buf.append(param[0]).append("=").append(param[1]);
            if (i.hasNext()) {
                buf.append('&');
            }
        }

        return buf.toString();
    }

    private static void addParam(final String key, final String value, final List<String[]> list) {
        list.add(new String[] {
                UriComponent.encode(key, UriComponent.Type.UNRESERVED),
                value == null ? "" : UriComponent.encode(value, UriComponent.Type.UNRESERVED)
        });
    }
}
