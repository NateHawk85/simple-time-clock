# Simple Time Clock

This is an example of a simple time clock that users can clock in and out of via REST API endpoints.

### Requirements

- Java 8+
- Maven 3

### How to Run
Clone this project to your machine and run `mvn clean install` to gather the required dependencies. Once that has completed, run the following command to start
up the server and begin accepting requests: `mvn spring-boot:run`.

From there, everything should be up and running. After the first startup, a `users_db.json` file will be created in the `src/main/resources` directory. This
file will store any interactions and requests for Users.

### API Documentation
API docs can be found [here](https://github.com/natehawk85/simple-time-clock/blob/main/API.md).

Additionally, available API operations can be found via the bundled Postman collection.