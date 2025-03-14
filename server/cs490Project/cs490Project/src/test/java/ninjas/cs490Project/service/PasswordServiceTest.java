package ninjas.cs490Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void testPasswordHashing() {
        String plainPassword = "mySecurePassword123";
        
        // Test that hashing produces a non-null result
        String hashedPassword = passwordService.hashPassword(plainPassword);
        assertNotNull(hashedPassword);
        
        // Test that hashing produces different hashes for the same password (due to salt)
        String anotherHash = passwordService.hashPassword(plainPassword);
        assertNotEquals(hashedPassword, anotherHash);
        
        // Test that the original password verifies against its hash
        assertTrue(passwordService.verifyPassword(plainPassword, hashedPassword));
        
        // Test that a wrong password fails verification
        assertFalse(passwordService.verifyPassword("wrongPassword", hashedPassword));
    }

    @Test
    void testEmptyPassword() {
        String emptyPassword = "";
        
        // Test that empty password can be hashed (though in practice this should be validated)
        String hashedEmpty = passwordService.hashPassword(emptyPassword);
        assertNotNull(hashedEmpty);
        
        // Verify empty password against its hash
        assertTrue(passwordService.verifyPassword(emptyPassword, hashedEmpty));
        
        // Verify non-empty password fails against empty password hash
        assertFalse(passwordService.verifyPassword("somePassword", hashedEmpty));
    }
}
