package com.nequi.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class NequiAuth implements Serializable {
    private final String DEFAULT_AUTH_GRANT_TYPE = "client_credentials";

    private static NequiAuth instance;
    private String token, tokenType;
    private String authUri, authGranType = DEFAULT_AUTH_GRANT_TYPE;
    private String clientId, clientSecret;
    private Date tokenExpiresAt;

    private NequiAuth() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static NequiAuth getInstance() {
        if (instance == null) {
            instance = new NequiAuth();
        }

        return instance;
    }

    /**
     * Sets the HTTP URI for the OAuth service
     * @param authUri Endpoint URI
     * @return NequiAuth
     */
    public NequiAuth withAuthUri(String authUri) {
        this.authUri = authUri;

        return this;
    }

    /**
     * Sets the grant type for the OAuth service
     * @param authGranType Auth grant type
     * @return NequiAuth
     */
    public NequiAuth withAuthGranType(String authGranType) {
        this.authGranType = authGranType;

        return this;
    }

    /**
     * Sets the client id used in header authentication for the OAuth service
     * @param clientId  Client id provided by Nequi
     * @return NequiAuth
     */
    public NequiAuth withClientId(String clientId) {
        this.clientId = clientId;

        return this;
    }

    /**
     * Sets the client secret used in header authentication for the OAuth service
     * @param clientSecret Client secret provided by Nequi
     * @return NequiAuth
     */
    public NequiAuth withClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;

        return this;
    }

    /**
     * Loads all needed vars from environment vars
     * @return NequiAuth
     */
    public NequiAuth fromEnvVars() {
        this.clientId = System.getenv("NEQUI_CLIENT_ID");
        this.clientSecret = System.getenv("NEQUI_CLIENT_SECRET");
        this.authUri = System.getenv("NEQUI_AUTH_URI");
        this.authGranType = System.getenv("NEQUI_AUTH_GRANT_TYPE");

        return this;
    }

    /**
     * Loads all needed vars passed by arguments
     * @param clientId Client id provided by Nequi
     * @param clientSecret Client secret provided by Nequi
     * @param authUri Auth endpoint URI
     * @param authGranType Auth grant type
     * @return NequiAuth
     */
    public NequiAuth with(String clientId, String clientSecret, String authUri, String authGranType) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authUri = authUri;
        this.authGranType = authGranType;

        return this;
    }

    /**
     * Loads all needed vars passed by arguments
     * @param clientId Client id provided by Nequi
     * @param clientSecret Client secret provided by Nequi
     * @param authUri Auth endpoint URI
     * @return NequiAuth
     */
    public NequiAuth with(String clientId,  String clientSecret, String authUri) {
        return this.with(clientId, clientSecret, authUri, DEFAULT_AUTH_GRANT_TYPE);
    }

    /**
     * Calls the OAuth service and stores the token information
     * @throws Exception
     */
    private void auth() throws Exception {
        HttpURLConnection connection = null;

        if (this.authUri == null || this.clientId == null || this.clientSecret == null) {
            throw new Exception("Invalid credentials, please provide a valid AuthUri, ClientId and ClientSecret.");
        }

        try {
            URL url = new URL(String.format("%s?grant_type=%s", this.authUri, this.authGranType));
            connection = (HttpURLConnection) url.openConnection();

            String basicAuth = String.format("%s:%s", this.clientId, this.clientSecret);
            String auth = Base64.encodeBase64String(basicAuth.getBytes());

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", String.format("Basic %s", auth));

            int status = connection.getResponseCode();

            if (status == 200) {
                // Load HTTP response
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    content.append(inputLine);
                }

                bufferedReader.close();

                // Parse response to JSON and save information
                JsonObject tokenData = new JsonParser().parse(content.toString()).getAsJsonObject();

                this.token = tokenData.get("access_token").getAsString();
                this.tokenType = tokenData.get("token_type").getAsString();

                long tokenExpiresSecs = tokenData.get("expires_in").getAsLong();
                this.tokenExpiresAt = new Date(System.currentTimeMillis() + (tokenExpiresSecs * 1000));
            } else {
                throw new Exception(
                    String.format("Error %d: Unable to auth with Conecta Nequi, please check the information sent.", status)
                );
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Checks if the current token is valid
     * @return True if the current token is valid
     */
    private boolean isValidToken() {
        if (this.tokenExpiresAt == null) {
            return false;
        }

        return new Date().before(this.tokenExpiresAt);
    }

    /**
     * Get a valid token
     * @param full Indicates if the token includes the token type
     * @return Token(If the value full is passed in true, it will return the token type and token joined)
     * @throws Exception
     */
    public String getToken(boolean full) throws Exception {
        if (!this.isValidToken()) {
            this.auth();
        }

        return full ? String.format("%s %s", this.tokenType, this.token) : this.token;
    }

    /**
     * Get a valid token
     * @return Token without token type
     * @throws Exception
     */
    public String getToken() throws Exception {
        return this.getToken(true);
    }

    /**
     * Make singleton from serialize and deserialize operation
     * @return NequiAuth
     */
    protected NequiAuth readResolve() {
        return getInstance();
    }
}
