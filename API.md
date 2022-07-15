*NOTE*: All API operations assume a base context path of "http://localhost:8080/simple-time-clock"

### Create User
- Endpoint: POST "/user/{userId}"
- Success:
    - Status: 201 CREATED
    - Body: The created User object in the database
- Failure (When a User already exists with the given userId):
    - Status: 409 CONFLICT
    - Body: "User already exists"

### Update User
- Endpoint: POST "/user/{userId}/update"
- Optional Parameters:
    - name: String
    - role: Role \[Administrator, NonAdministrator]
- Success:
    - Status: 202 ACCEPTED
    - Body: The updated User object in the database
- Failure (When a User does not exist with the given userId):
    - Status: 404 NOT FOUND
    - Body: "User not found"

### Start Shift for User
- Endpoint: POST "/user/{userId}/startShift"
- Success:
    - Status: 202 ACCEPTED
    - Body: Empty
- Failure (When a User does not exist with the given userId):
    - Status: 404 NOT FOUND
    - Body: "User not found"
- - Failure (When the User has already started their shift):
    - Status: 409 CONFLICT
    - Body: "Work shift is in progress"

### End Shift for User
- Endpoint: POST "/user/{userId}/endShift"
- Success:
    - Status: 202 ACCEPTED
    - Body: Empty
- Failure (When a User does not exist with the given userId):
    - Status: 404 NOT FOUND
    - Body: "User not found"
- Failure (When the User has not started their shift):
    - Status: 409 CONFLICT
    - Body: "Work shift has not started"
- Failure (When the User is on a break):
    - Status: 409 CONFLICT
    - Body: "Break is in progress"

### Start Break for User
- Endpoint: POST "/user/{userId}/startBreak"
- Optional Parameters:
    - breakType: BreakType \[Break, Lunch] (will default to Break if not defined)
- Success:
    - Status: 202 ACCEPTED
    - Body: Empty
- Failure (When a User does not exist with the given userId):
    - Status: 404 NOT FOUND
    - Body: "User not found"
- Failure (When the User has not started their shift):
    - Status: 409 CONFLICT
    - Body: "Work shift has not started"
- Failure (When the User is on a break):
    - Status: 409 CONFLICT
    - Body: "Break is in progress"

### End Break for User
- Endpoint: POST "/user/{userId}/endBreak"
- Success:
    - Status: 202 ACCEPTED
    - Body: Empty
- Failure (When a User does not exist with the given userId):
    - Status: 404 NOT FOUND
    - Body: "User not found"
- Failure (When the User has not started their break):
    - Status: 409 CONFLICT
    - Body: "Break has not started"

### ADMIN ONLY - Find User Activity
- Endpoint: GET "/admin/{adminUserId}/userActivity"
- Optional Parameters (that filter User results):
    - userIdToView: String (only filters when specified, exact match ignoring case)
    - priorWorkShiftsThreshold: int (defaults to 0)
    - priorBreaksThreshold: int (defaults to 0)
    - isCurrentlyOnBreak: boolean (only filters when specified as "true")
    - isCurrentlyOnLunch: boolean (only filters when specified as "true")
    - roleToView: Role \[Administrator, NonAdministrator] (only filters when specified)
- Additional Optional Parameters (that filter which shift/break results to return for Users)
    - shiftBeginsBefore: LocalDateTime (format: yyyy-MM-dd HH:mm)
    - shiftBeginsAfter: LocalDateTime (format: yyyy-MM-dd HH:mm)
    - breakBeginsBefore: LocalDateTime (format: yyyy-MM-dd HH:mm)
    - breakBeginsAfter: LocalDateTime (format: yyyy-MM-dd HH:mm)
- Success:
    - Status: 202 ACCEPTED
    - Body: Empty
- Failure (When a User does not exist with the given userId):
    - Status: 404 NOT FOUND
    - Body: "User not found"
- Failure (When the User has not started their break):
    - Status: 409 CONFLICT
    - Body: "Break has not started"
