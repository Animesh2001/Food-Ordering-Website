package com.restaurant.food_ordering_website.NewModel;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 20, nullable = false)
    private String lastName;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20, unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "alternate_phone_number", length = 20, unique = true)
    private String alternatePhoneNumber;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;
}
