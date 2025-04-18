package ninjas.cs490Project.service.oauth;

import ninjas.cs490Project.dto.LinkedInOAuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LinkedInOAuthService {

    @Value("${linkedin.client.id}")
    private String clientId;

    @Value("${linkedin.client.secret}")
    private String clientSecret;

    @Value("${linkedin.redirect.uri}")
    private String redirectUri;

    private final String TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    private final String USER_INFO_URL = "https://api.linkedin.com/v2/userinfo";

    public String exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);
        
        return (String) response.getBody().get("access_token");
    }

    public LinkedInOAuthUser getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> profileResponse = restTemplate.exchange(
            USER_INFO_URL, 
            HttpMethod.GET, 
            entity, 
            Map.class
        );

        LinkedInOAuthUser user = new LinkedInOAuthUser();
        Map<String, Object> profileData = profileResponse.getBody();
        
        // Extract user info from the response
        user.setEmail((String) profileData.get("email"));
        user.setFirstName((String) profileData.get("given_name"));
        user.setLastName((String) profileData.get("family_name"));
        user.setId((String) profileData.get("sub"));
        user.setProfilePictureUrl((String) profileData.get("picture"));

        return user;
    }
} 