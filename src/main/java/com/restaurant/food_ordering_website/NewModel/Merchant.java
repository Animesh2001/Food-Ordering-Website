package com.restaurant.food_ordering_website.NewModel;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Data
@Table(name = "Merchant")
public class Merchant {

    @Id
    @OneToOne
    @JoinColumn(name = "merchant_id")
    private User user;

    @Column(name = "total_credit")
    private Double totalCredit;

    @Column(name = "total_credit_unit", length = 3)
    private String totalCreditUnit;
}
