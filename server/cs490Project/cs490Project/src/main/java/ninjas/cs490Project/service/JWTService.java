package ninjas.cs490Project.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import ninjas.cs490Project.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JWTService {

    private final Key key;

    // We'll read the 'jwt.secret' from application.properties
    // and ensure it’s large enough for HS256
    public JWTService(@Value("${jwt.secret}") String secretString) {
        // Convert the string to bytes. Must be >= 256 bits => >= 32 bytes
        byte[] keyBytes = secretString.getBytes();
        // Or if you used a Base64 string, decode it first
        // e.g. byte[] keyBytes = Decoders.BASE64.decode(secretString);

        this.key = Keys.hmacShaKeyFor(keyBytes);
        // If 'secretString' is < 32 chars, you'll get WeakKeyException
    }

    public String generateToken(User user) {
        // Example: token is valid for 24 hours
        long now = System.currentTimeMillis();
        long expirationMillis = 24 * 60 * 60 * 1000; // 1 day
        Date expiry = new Date(now + expirationMillis);

        return Jwts.builder()
                .setSubject(user.getEmail())      // or user.getUsername(), etc.
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // Will throw if invalid or expired
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            // Could log the error
            return false;
        }
    }

    public String extractUsername(String token) {
        // We assume 'sub' in the JWT is the username/email
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
