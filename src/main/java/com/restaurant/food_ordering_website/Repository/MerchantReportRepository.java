package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.MerchantReport;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.print.DocFlavor;

public interface MerchantReportRepository extends JpaRepository<MerchantReport, Integer> {

    MerchantReport findByMerchantId(int merchantId);
}
