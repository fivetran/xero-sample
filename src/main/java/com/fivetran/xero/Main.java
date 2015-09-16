package com.fivetran.xero;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;

public class Main {

    public static void main(String[] args) throws JsonProcessingException {
        // This works
        System.out.println(JSON.writeValueAsString(getAccounts()));

        // This doesn't
        System.out.println(JSON.writeValueAsString(getAccountsWithQuery()));
    }

    // REPLACE THESE STRINGS WITH YOUR OWN PARAMETERS!!!
    public static final String CONSUMER_KEY = "[consumer key from xero developer home]";
    public static final String CONSUMER_SECRET = "[consumer secret from xero developer home]";
    public static final String OAUTH_TOKEN = "[oauth token from xero authentication process]";
    public static final String OAUTH_TOKEN_SECRET = "[oauth token secret from xero authentication process]";

    /**
     * Root of Xero API
     */
    public static final String BASE_URL = "https://api.xero.com/api.xro/2.0";

    private static final Client HTTP = createHttpClient();
    private static final ObjectMapper JSON = createObjectMapper();

    /**
     * Creates an HTTP client that will compute OAuth 1 signatures and deserialize JSON responses using Jackson.
     * We can use this client to communicate with Xero.
     * All requests and responses will be logged in detail using LateLoggingFilter.
     */
    private static Client createHttpClient() {
        return ClientBuilder.newBuilder()
                            .register(new JacksonJsonProvider(JSON))
                            .register(new LateLoggingFilter())
                            .register(new OAuth1Signature(CONSUMER_KEY, CONSUMER_SECRET, OAUTH_TOKEN, OAUTH_TOKEN_SECRET))
                            .build();
    }

    /**
     * Configures Jackson, a JSON-reading framework.
     */
    private static ObjectMapper createObjectMapper() {
        // If you want to make global changes to how we deserialize json,
        // such as converting underscore_case to camelCase, this is the place to do it
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // These settings will aggressively fail when the JSON response doesn't match the class definition
        // If the target API is inconsistent, you can weaken these
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        return mapper;
    }

    private static List<Account> getAccounts() {
        URI target = URI.create(BASE_URL + "/Accounts");
        AccountPage page = HTTP.target(target)
                               .request(MediaType.APPLICATION_JSON_TYPE)
                               .get(AccountPage.class);

        return page.Accounts;
    }

    private static List<Account> getAccountsWithQuery() {
        URI target = URI.create(BASE_URL + "/Accounts");
        AccountPage page = HTTP.target(target)
                               .queryParam("page", 1)
                               .request(MediaType.APPLICATION_JSON_TYPE)
                               .get(AccountPage.class);

        return page.Accounts;
    }

    /**
     * Jackson will convert Xero's JSON response into this type.
     */
    public static class AccountPage {
        public String Id;
        public String Status;
        public String ProviderName;
        public String DateTimeUTC;
        public List<Account> Accounts;
    }

    /**
     * Jackson will convert Xero's JSON response into this type.
     */
    public static class Account {
        public String AccountID;

        public String Code;
        public String Name;
        public String Type;
        public String Description;
        public String TaxType;
        public Boolean EnablePaymentsToAccount;
        public Boolean ShowInExpenseClaims;
        public String Class;
        public String Status;

        public String SystemAccount;
        public String BankAccountType;
        public String BankAccountNumber;
        public String CurrencyCode;
        public String ReportingCode;
        public String ReportingCodeName;

        public Boolean HasAttachments;
    }
}
