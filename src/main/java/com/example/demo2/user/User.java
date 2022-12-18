package com.example.demo2.user;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

@Entity(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseModel {
    private String name;
    @NonNull
    private String password;
    @Column(unique = true)
    private int mobileNumber;
    @Column(unique = true, updatable = false)
    @NonNull
    private String email;
}
