RideShare Backend
A Spring Boot REST API for ride-sharing application with JWT authentication and MongoDB.
Features

User & Driver registration/login with JWT
Request rides (Passenger)
View pending rides (Driver)
Accept rides (Driver)
Complete rides
Input validation & global exception handling

Tech Stack

Spring Boot 3.3.4
MongoDB Atlas
JWT Authentication
Java 17

Setup

Clone repository
Update application.yml with MongoDB URI
Run: mvn spring-boot:run
API runs on http://localhost:8081

API Endpoints
MethodEndpointRolePOST/api/auth/registerPublicPOST/api/auth/loginPublicPOST/api/v1/ridesUSERGET/api/v1/user/ridesUSERGET/api/v1/driver/rides/requestsDRIVERPOST/api/v1/driver/rides/{id}/acceptDRIVERPOST/api/v1/rides/{id}/completeUSER/DRIVER
Testing
Register:
bashcurl -X POST http://localhost:8081/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"john","password":"1234","role":"ROLE_USER"}'
Login & Get Token:
bashcurl -X POST http://localhost:8081/api/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"john","password":"1234"}'
Author
Saiteja
