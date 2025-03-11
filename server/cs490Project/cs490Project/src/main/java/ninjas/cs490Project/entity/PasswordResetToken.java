package ninjas.cs490Project.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    // Getters and Setters

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getToken(){
        return token;
    }
    public void setToken(String token){
        this.token = token;
    }
    public User getUser(){
        return user;
    }
    public void setUser(User user){
        this.user = user;
    }
    public LocalDateTime getExpiryDate(){
        return expiryDate;
    }
    public void setExpiryDate(LocalDateTime expiryDate){
        this.expiryDate = expiryDate;
    }
}