package com.saiteja.demo.controller;

import com.saiteja.demo.dto.CreateRideRequest;
import com.saiteja.demo.dto.RideResponse;
import com.saiteja.demo.service.RideService;
import com.saiteja.demo.service.RideQueryService;
import com.saiteja.demo.service.RideAnalyticsService;
import com.saiteja.demo.model.Ride;
import com.saiteja.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class RideController {

    @Autowired
    private RideService rideService;

    @Autowired
    private RideQueryService queryService;

    @Autowired
    private RideAnalyticsService analyticsService;

    @Autowired
    private JwtUtil jwtUtil;

    // ============ BASIC RIDE OPERATIONS ============

    // Passenger: Request a ride
    @PostMapping("/rides")
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody CreateRideRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUsername(token);

        RideResponse response = rideService.createRide(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Driver: View pending rides
    @GetMapping("/driver/rides/requests")
    public ResponseEntity<List<RideResponse>> getPendingRides(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String role = jwtUtil.extractRole(token);

        if (!role.equals("ROLE_DRIVER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<RideResponse> rides = rideService.getPendingRides();
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // Driver: Accept ride
    @PostMapping("/driver/rides/{rideId}/accept")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable String rideId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String role = jwtUtil.extractRole(token);
        String driverId = jwtUtil.extractUsername(token);

        if (!role.equals("ROLE_DRIVER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        RideResponse response = rideService.acceptRide(rideId, driverId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Complete ride
    @PostMapping("/rides/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(
            @PathVariable String rideId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        jwtUtil.validateToken(token);

        RideResponse response = rideService.completeRide(rideId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // User: Get their rides
    @GetMapping("/user/rides")
    public ResponseEntity<List<RideResponse>> getUserRides(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUsername(token);

        List<RideResponse> rides = rideService.getUserRides(userId);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // ============ QUERY APIs (Search, Filter, Sort) ============

    // API 1: Search by pickup OR drop location
    @GetMapping("/rides/search")
    public ResponseEntity<List<Ride>> searchByLocation(@RequestParam String text) {
        List<Ride> rides = queryService.searchByLocation(text);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 2: Filter rides by distance range
    @GetMapping("/rides/filter-distance")
    public ResponseEntity<List<Ride>> filterByDistance(
            @RequestParam Double min,
            @RequestParam Double max) {
        List<Ride> rides = queryService.filterByDistance(min, max);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 3: Filter rides by date range
    @GetMapping("/rides/filter-date-range")
    public ResponseEntity<List<Ride>> filterByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<Ride> rides = queryService.filterByDateRange(start, end);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 4: Sort rides by fare
    @GetMapping("/rides/sort")
    public ResponseEntity<List<Ride>> sortByFare(@RequestParam String order) {
        List<Ride> rides = queryService.sortByFare(order);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 5: Get all rides for a user
    @GetMapping("/rides/user/{userId}")
    public ResponseEntity<List<Ride>> getUserRidesByUserId(@PathVariable String userId) {
        List<Ride> rides = queryService.findByUserId(userId);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 6: Get rides for user filtered by status
    @GetMapping("/rides/user/{userId}/status/{status}")
    public ResponseEntity<List<Ride>> getUserRidesByStatus(
            @PathVariable String userId,
            @PathVariable String status) {
        List<Ride> rides = queryService.findByUserIdAndStatus(userId, status);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 7: Get driver's active rides
    @GetMapping("/driver/{driverId}/active-rides")
    public ResponseEntity<List<Ride>> getActiveRidesByDriver(@PathVariable String driverId) {
        List<Ride> rides = queryService.findActiveRidesByDriver(driverId);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 8: Filter by status AND keyword search
    @GetMapping("/rides/filter-status")
    public ResponseEntity<List<Ride>> filterByStatusAndKeyword(
            @RequestParam String status,
            @RequestParam String search) {
        List<Ride> rides = queryService.filterByStatusAndKeyword(status, search);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 9: Advanced search with pagination and sorting
    @GetMapping("/rides/advanced-search")
    public ResponseEntity<Page<Ride>> advancedSearch(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Ride> rides = queryService.advancedSearch(search, status, sort, order, page, size);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // API 14: Get rides on specific date
    @GetMapping("/rides/date/{date}")
    public ResponseEntity<List<Ride>> getRidesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Ride> rides = queryService.getRidesByDate(date);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    // ============ ANALYTICS APIs (Aggregation) ============

    // API 10: Get rides per day
    @GetMapping("/analytics/rides-per-day")
    public ResponseEntity<List<Map<String, Object>>> getRidesPerDay() {
        List<Map<String, Object>> data = analyticsService.getRidesPerDay();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    // API 11: Get driver summary
    @GetMapping("/analytics/driver/{driverId}/summary")
    public ResponseEntity<Map<String, Object>> getDriverSummary(@PathVariable String driverId) {
        Map<String, Object> summary = analyticsService.getDriverSummary(driverId);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    // API 12: Get user spending
    @GetMapping("/analytics/user/{userId}/spending")
    public ResponseEntity<Map<String, Object>> getUserSpending(@PathVariable String userId) {
        Map<String, Object> spending = analyticsService.getUserSpending(userId);
        return new ResponseEntity<>(spending, HttpStatus.OK);
    }

    // API 13: Get status summary
    @GetMapping("/analytics/status-summary")
    public ResponseEntity<List<Map<String, Object>>> getStatusSummary() {
        List<Map<String, Object>> summary = analyticsService.getStatusSummary();
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
}