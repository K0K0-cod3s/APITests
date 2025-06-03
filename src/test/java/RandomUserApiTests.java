import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*; // For Hamcrest matchers like equalTo, hasKey, notNullValue

public class RandomUserApiTests {

    private static final String BASE_URL = "https://randomuser.me";

    @Test
    @DisplayName("Verify basic GET request to Random User Generator API")
    void testBasicGetRandomUser() {
        // Set the base URI for all requests in this test class (optional but good practice)
        RestAssured.baseURI = BASE_URL;

        // Perform the GET request and get the response
        Response response = RestAssured.given()
                .log().method().log().uri() // Logs the HTTP method and URI before sending the request
                .when()
                .get("/api/");

        // Print the full response details for debugging (optional)
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body:\n" + response.getBody().asString());

        // Validate status code (200 OK)
        response.then()
                .log().status() // Logs the status code after receiving the response
                .statusCode(200);

        // Validate basic response body content using Hamcrest matchers
        // The API returns a 'results' array containing user objects
        response.then()
                .log().body() // Logs the response body after validation
                .body("results", is(notNullValue())) // Ensure 'results' key exists and is not null
                .body("results", isA(java.util.ArrayList.class)) // Ensure 'results' is a list
                .body("results.size()", greaterThan(0)) // Ensure the 'results' list is not empty
                .body("results[0].gender", is(notNullValue())) // Check for 'gender' in the first user
                .body("results[0].name.first", is(notNullValue())) // Check for 'name.first'
                .body("results[0].name.last", is(notNullValue()))  // Check for 'name.last'
                .body("results[0].email", is(notNullValue()))      // Check for 'email'
                .body("results[0].login.username", is(notNullValue())) // Check for 'login.username'
                .body("results[0].phone", is(notNullValue()));     // Check for 'phone'

        System.out.println("Basic GET request test case passed successfully!");
    }
}