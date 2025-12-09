package com.saiteja.demo.service;

import com.saiteja.demo.model.Ride;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RideAnalyticsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    // API 10: Get rides count grouped by date
    public List<Map<String, Object>> getRidesPerDay() {
        GroupOperation group = Aggregation.group("$createdAt")
                .count().as("totalRides");

        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.DESC, "_id"));

        Aggregation aggregation = Aggregation.newAggregation(group, sort);

        List<Map> results = mongoTemplate.aggregate(aggregation, "rides", Map.class)
                .getMappedResults();

        List<Map<String, Object>> response = new ArrayList<>();
        for (Map result : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", result.get("_id"));
            item.put("totalRides", result.get("totalRides"));
            response.add(item);
        }
        return response;
    }

    // API 11: Get driver summary (rides, earnings, etc)
    public Map<String, Object> getDriverSummary(String driverId) {
        MatchOperation match = Aggregation.match(
                Criteria.where("driverId").is(driverId)
        );

        GroupOperation group = Aggregation.group()
                .count().as("totalRides")
                .sum("fareAmount").as("totalEarnings")
                .avg("distanceKm").as("avgDistance");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        Map result = mongoTemplate.aggregate(aggregation, "rides", Map.class)
                .getUniqueMappedResult();

        Map<String, Object> response = new HashMap<>();
        if (result != null) {
            response.put("totalRides", result.get("totalRides"));
            response.put("totalEarnings", result.get("totalEarnings"));
            response.put("avgDistance", result.get("avgDistance"));
        }
        return response;
    }

    // API 12: Get user spending analysis
    public Map<String, Object> getUserSpending(String userId) {
        MatchOperation match = Aggregation.match(
                Criteria.where("userId").is(userId)
                        .and("status").is("COMPLETED")
        );

        GroupOperation group = Aggregation.group()
                .sum("fareAmount").as("totalSpent")
                .count().as("completedRides")
                .avg("fareAmount").as("avgFarePerRide");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        Map result = mongoTemplate.aggregate(aggregation, "rides", Map.class)
                .getUniqueMappedResult();

        Map<String, Object> response = new HashMap<>();
        if (result != null) {
            response.put("totalSpent", result.get("totalSpent"));
            response.put("completedRides", result.get("completedRides"));
            response.put("avgFarePerRide", result.get("avgFarePerRide"));
        }
        return response;
    }

    // API 13: Get status summary (count rides by status)
    public List<Map<String, Object>> getStatusSummary() {
        GroupOperation group = Aggregation.group("$status")
                .count().as("count");

        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.DESC, "count"));

        Aggregation aggregation = Aggregation.newAggregation(group, sort);

        List<Map> results = mongoTemplate.aggregate(aggregation, "rides", Map.class)
                .getMappedResults();

        List<Map<String, Object>> response = new ArrayList<>();
        for (Map result : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", result.get("_id"));
            item.put("count", result.get("count"));
            response.add(item);
        }
        return response;
    }
}