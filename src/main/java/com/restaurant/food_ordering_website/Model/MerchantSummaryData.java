package com.restaurant.food_ordering_website.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSummaryData {

   private int merchantId;
   List<SummaryData> list;


}
