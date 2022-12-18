package com.example.demo2.security;
import com.example.demo2.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokens {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID token;
    @ManyToOne
    private User user;
    private Date startTime;
    private int ttl;
}
