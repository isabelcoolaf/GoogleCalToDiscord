package edu.wsu.cs320.googleapi;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.UserCredentials;
import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

public class GoogleOAuthManager {

    private final String clientID;
    private final String clientSecret;
    private String refreshToken;
    private final String configPath;

    private String accessToken;

    private static volatile String callbackResponse = "";
    private static volatile Error callbackException = null;

    public GoogleOAuthManager(String clientID, String clientSecret, String refreshToken, String configPath) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.configPath = configPath;

        this.accessToken = "";

        // This will validate the refresh token and try to fetch a new access token.
        // If this succeeds, isAuthenticated should be true.
        try {
            UserCredentials credentials = this.getCredentials();
            if (credentials != null) {
                this.accessToken = credentials.refreshAccessToken().getTokenValue();
            }
        } catch (IOException ignored) {}

    }

    public boolean isAuthenticated() {
        return !accessToken.isEmpty() && !refreshToken.isEmpty();
    }

    public UserCredentials getCredentials() {
        try {
            return UserCredentials.newBuilder()
                    .setClientId(this.clientID)
                    .setClientSecret(this.clientSecret)
                    .setRefreshToken(this.refreshToken)
                    .build();
        } catch (Exception ignored) {
            return null;
        }
    }

    public void storeCredentials() throws IOException {
        ConfigManager manager = new ConfigManager(this.configPath);
        manager.put(ConfigValues.GOOGLE_CLIENT_ID, this.clientID);
        manager.put(ConfigValues.GOOGLE_CLIENT_SECRET, this.clientSecret);
        manager.put(ConfigValues.GOOGLE_REFRESH_TOKEN, this.refreshToken);
    }

    public void invokeFlow() throws Exception {
        int port;
        try (ServerSocket sock = new ServerSocket(0)) {
            port = sock.getLocalPort();
        }
        invokeFlow(port);
    }

    public void invokeFlow(int port) throws Exception {
        String oauthURL = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + this.clientID + "&redirect_uri=http://localhost:" + port + "&response_type=code&scope=https://www.googleapis.com/auth/calendar&access_type=offline&prompt=select_account";

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                if (!req.getRequestURI().equals("/")) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                String err = req.getParameter("error");
                if (err != null && !err.isEmpty()) {
                    resp.getWriter().write(err);
                    callbackException = new Error(err);
                    return;
                }
                String scope = req.getParameter("scope");
                if (scope == null || !scope.contains("calendar")) {
                    err = "Missing scope. Re-authorize with all scopes checked.";
                    resp.getWriter().write(err);
                    callbackException = new Error(err);
                    return;
                }
                callbackResponse = req.getParameter("code");
                if (callbackResponse == null || callbackResponse.isEmpty()) {
                    err = "Didn't receive code from Google. Try again?";
                    resp.getWriter().write(err);
                    callbackException = new Error(err);
                    return;
                }
                resp.getWriter().write("Authentication successful! You may close this window.");
            }
        }), "/");

        server.start();

        Desktop.getDesktop().browse(new URI(oauthURL));
        while ((callbackResponse == null || callbackResponse.isEmpty()) && callbackException == null) {
            Thread.sleep(100);
        }
        server.stop();
        if (callbackException != null) {
            Error error = callbackException;
            callbackException = null;
            throw error;
        }
        GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                this.clientID,
                this.clientSecret,
                callbackResponse,
                "http://localhost:" + port
        ).execute();
        this.refreshToken = response.getRefreshToken();
        this.accessToken = response.getAccessToken();
        this.storeCredentials();
    }

}
