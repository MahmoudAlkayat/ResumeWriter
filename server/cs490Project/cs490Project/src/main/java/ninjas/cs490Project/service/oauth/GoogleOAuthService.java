package ninjas.cs490Project.service.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import ninjas.cs490Project.dto.GoogleOAuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleOAuthService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";

    /**
     * Exchanges an authorization code for tokens using Google’s token endpoint.
     */
    public GoogleTokenResponse exchangeCodeForTokens(String code) throws IOException {
        return new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                TOKEN_SERVER_URL,
                clientId,
                clientSecret,
                code,
                redirectUri
        ).execute();
    }

    /**
     * Verifies the ID token and extracts user information (email, first name, last name, etc.).
     */
    public GoogleOAuthUser getUserFromIdToken(String idTokenString)
            throws GeneralSecurityException, IOException {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Google often provides given_name and family_name
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            // If given_name/family_name are missing, fallback to "name"
            if (firstName == null) {
                // "name" might be the user's full name
                firstName = (String) payload.get("name");
            }
            // If you want to store just a single name, you can combine them,
            // or keep them separate if you prefer.

            String pictureUrl = (String) payload.get("picture");

            // Build the DTO
            GoogleOAuthUser user = new GoogleOAuthUser();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPictureUrl(pictureUrl);

            return user;
        } else {
            throw new RuntimeException("Invalid ID token");
        }
    }
}
