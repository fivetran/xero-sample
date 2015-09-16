package com.fivetran.xero;

import org.glassfish.jersey.filter.LoggingFilter;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

@Priority(Integer.MAX_VALUE)
class LateLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(LateLoggingFilter.class.getName());

    LoggingFilter delegate = new LoggingFilter(LOG, true);

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        delegate.filter(requestContext);
    }

    @Override
    public void filter(ClientRequestContext requestContext,
                       ClientResponseContext responseContext) throws IOException {
        delegate.filter(requestContext, responseContext);
    }
}
