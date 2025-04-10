// src/main/java/ninjas/cs490Project/dto/RegistrationRequest.java
package ninjas.cs490Project.dto;

public class RegistrationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    // Constructors, getters, and setters
    public RegistrationRequest() {}

    public RegistrationRequest(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    // getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}