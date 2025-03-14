package ninjas.cs490Project.dto;

public class GoogleOAuthUser {

    private String email;

    /**
     * The full display name (e.g., "John Doe").
     */
    private String name;

    /**
     * The given name (e.g., "John").
     */
    private String firstName;

    /**
     * The family name (e.g., "Doe").
     */
    private String lastName;

    private String pictureUrl;

    // -----------------------------------------------------
    // Getters and Setters
    // -----------------------------------------------------

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * A single field for the full display name, if Google returns one.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A separate field for the first/given name.
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * A separate field for the last/family name.
     */
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
