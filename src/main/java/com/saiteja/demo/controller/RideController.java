package com.saiteja.demo.controller;

import com.saiteja.demo.dto.CreateRideRequest;
import com.saiteja.demo.dto.RideResponse;
import com.saiteja.demo.service.RideService;
import com.saiteja.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class RideController {

    @Autowired
    private RideService rideService;

    @Autowired
    private JwtUtil jwtUtil;

    // Passenger: Request a ride
    @PostMapping("/rides")
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody CreateRideRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId = extractUserId(token);

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
        String driverId = extractUserId(token);

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
        String userId = extractUserId(token);

        List<RideResponse> rides = rideService.getUserRides(userId);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    private String extractUserId(String token) {
        return jwtUtil.extractUsername(token);
    }
}
