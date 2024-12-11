package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "merchant_report")
@Data
public class MerchantReport {
    @Id
    private int merchantId;

    private String filePath;
}
