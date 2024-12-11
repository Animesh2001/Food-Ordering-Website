package com.restaurant.food_ordering_website.Writer;

import com.restaurant.food_ordering_website.Model.MerchantSummaryData;
import com.restaurant.food_ordering_website.Model.SummaryData;
import com.restaurant.food_ordering_website.Service.ExcelService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RestaurantDataWriter implements ItemWriter<MerchantSummaryData> {

    private final ExcelService excelService;

    public RestaurantDataWriter(ExcelService excelService) {
        this.excelService = excelService;
    }


    @Override
    public void write(Chunk<? extends MerchantSummaryData> chunks) throws Exception {
        // Iterate over each list of RestaurantData in the chunk

        Map<Integer, List<SummaryData>> merchantDataMap = new HashMap<>();

        for (MerchantSummaryData merchantSummaryData : chunks) {

            int merchantId = merchantSummaryData.getMerchantId();
            List<SummaryData> restaurantDataList = merchantSummaryData.getList();
            merchantDataMap.put(merchantId, restaurantDataList);
        }


        for (Map.Entry<Integer, List<SummaryData>> entry : merchantDataMap.entrySet()) {
            String merchantId = String.valueOf(entry.getKey());
            List<SummaryData> restaurantDataList = entry.getValue();
            System.out.println("Writing to excel for merchantId "+ merchantId);
            excelService.writeToExcel(merchantId, restaurantDataList);
        }
    }

}

