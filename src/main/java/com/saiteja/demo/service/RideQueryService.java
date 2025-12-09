package com.saiteja.demo.service;

import com.saiteja.demo.model.Ride;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RideQueryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    // API 1: Search by pickup OR drop location (case-insensitive)
    public List<Ride> searchByLocation(String text) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("pickupLocation").regex(text, "i"),
                Criteria.where("dropLocation").regex(text, "i")
        );
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }

    // API 2: Filter rides by distance range (min to max km)
    public List<Ride> filterByDistance(Double min, Double max) {
        Criteria criteria = Criteria.where("distanceKm").gte(min).lte(max);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }

    // API 3: Filter rides by date range
    public List<Ride> filterByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        Criteria criteria = Criteria.where("createdAt")
                .gte(startDateTime)
                .lte(endDateTime);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }

    // API 4: Sort rides by fare (asc or desc)
    public List<Ride> sortByFare(String order) {
        Sort.Direction direction = order.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Query query = new Query().with(Sort.by(direction, "fareAmount"));
        return mongoTemplate.find(query, Ride.class);
    }

    // API 5: Get all rides for a user
    public List<Ride> findByUserId(String userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }

    // API 6: Get rides for user filtered by status
    public List<Ride> findByUserIdAndStatus(String userId, String status) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").is(status);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }

    // API 7: Get driver's active rides
    public List<Ride> findActiveRidesByDriver(String driverId) {
        Criteria criteria = Criteria.where("driverId").is(driverId)
                .and("status").is("ACCEPTED");
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }

    // API 8: Filter by status AND keyword search
    public List<Ride> filterByStatusAndKeyword(String status, String search) {
        Criteria criteria = new Criteria()
                .and("status").is(status)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("pickupLocation").regex(search, "i"),
                                Criteria.where("dropLocation").regex(search, "i")
                        )
                );
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }

    // API 9: Advanced search with pagination and sorting
    public Page<Ride> advancedSearch(String search, String status,
                                     String sortField, String order,
                                     int page, int size) {
        Criteria criteria = new Criteria();

        // Add search criteria
        if (search != null && !search.isEmpty()) {
            criteria.andOperator(
                    new Criteria().orOperator(
                            Criteria.where("pickupLocation").regex(search, "i"),
                            Criteria.where("dropLocation").regex(search, "i")
                    )
            );
        }

        // Add status criteria
        if (status != null && !status.isEmpty()) {
            criteria.and("status").is(status);
        }

        // Sorting
        Sort.Direction direction = order.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField != null ? sortField : "createdAt");

        // Pagination
        Pageable pageable = PageRequest.of(page, size, sort);
        Query query = new Query(criteria).with(pageable);

        long total = mongoTemplate.count(new Query(criteria), Ride.class);
        List<Ride> rides = mongoTemplate.find(query, Ride.class);

        return new PageImpl<>(rides, pageable, total);
    }

    // API 14: Get rides on specific date
    public List<Ride> getRidesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Criteria criteria = Criteria.where("createdAt")
                .gte(startOfDay)
                .lte(endOfDay);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Ride.class);
    }
}