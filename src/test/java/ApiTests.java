
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@DisplayName("JSONPlaceholder API Tests")
public class ApiTests { // Removed 'extends BaseTest' as setup() is now directly in this class

    @BeforeAll
    public static void setup() {
        // Set the base URI for all requests
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }

    @Test
    @DisplayName("Verify GET /posts returns 200 OK and contains expected data")
    @Description("This test verifies that the /posts endpoint returns a 200 OK status code, " +
            "JSON content type, and a non-empty array with valid post structures.")
    void testGetPosts() {
        performGetPostsRequest();
    }

    @Step("Performing GET /posts request and validating response")
    private void performGetPostsRequest() {
        given()
                .when()
                .get("/posts")
                .then()
                .statusCode(200) // Validate status code
                .contentType(ContentType.JSON) // Validate content type
                .body("size()", greaterThan(0)) // Validate response body is not empty
                .body("[0].id", notNullValue()) // Validate first post has an ID
                .body("[0].title", notNullValue()); // Validate first post has a title
    }

    @Test
    @DisplayName("Verify GET /posts/{id} returns 200 OK for a specific post")
    @Description("This test verifies that fetching a single post by ID returns a 200 OK status " +
            "and the correct post details.")
    void testGetSinglePost() {
        performGetSinglePostRequest(1);
    }

    @Step("Performing GET /posts/{id} request for ID {0} and validating response")
    private void performGetSinglePostRequest(int postId) {
        given()
                .pathParam("id", postId) // Set path parameter
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(postId)) // Validate specific ID
                .body("title", notNullValue())
                .body("userId", equalTo(1));
    }

    @Test
    @DisplayName("Verify POST /posts creates a new post and returns 201 Created")
    @Description("This test verifies that a POST request to /posts successfully creates a new post " +
            "and returns a 201 Created status with the correct data.")
    void testCreatePost() {
        String title = "foo";
        String body = "bar";
        int userId = 1;
        String requestBody = String.format("{\"title\": \"%s\", \"body\": \"%s\", \"userId\": %d}", title, body, userId);

        Response response = performCreatePostRequest(requestBody, title, body, userId);
        System.out.println("Created Post ID: " + response.jsonPath().getInt("id"));
    }

    @Step("Performing POST /posts request with title '{1}', body '{2}', userId {3} and validating response")
    private Response performCreatePostRequest(String requestBody, String expectedTitle, String expectedBody, int expectedUserId) {
        return given()
                .contentType(ContentType.JSON) // Set content type for request
                .body(requestBody) // Set request body
                .when()
                .post("/posts")
                .then()
                .statusCode(201) // Validate status code for creation
                .contentType(ContentType.JSON)
                .body("id", notNullValue()) // Validate new ID is generated
                .body("title", equalTo(expectedTitle)) // Validate title
                .body("body", equalTo(expectedBody)) // Validate body
                .body("userId", equalTo(expectedUserId)) // Validate userId
                .extract().response(); // Extract response for further assertions if needed
    }

    @Test
    @DisplayName("Verify PUT /posts/{id} updates an existing post and returns 200 OK")
    @Description("This test verifies that a PUT request to /posts/{id} successfully updates an " +
            "existing post and returns a 200 OK status with the updated data.")
    void testUpdatePost() {
        int postId = 1;
        String updatedTitle = "updated title";
        String updatedBody = "updated body";
        int userId = 1; // userId is typically not updated in JSONPlaceholder PUT
        String requestBody = String.format("{\"id\": %d, \"title\": \"%s\", \"body\": \"%s\", \"userId\": %d}",
                postId, updatedTitle, updatedBody, userId);

        performUpdatePostRequest(postId, requestBody, updatedTitle, updatedBody);
    }

    @Step("Performing PUT /posts/{id} request for ID {0} with updated title '{2}', body '{3}' and validating response")
    private void performUpdatePostRequest(int postId, String requestBody, String expectedTitle, String expectedBody) {
        given()
                .pathParam("id", postId)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/posts/{id}")
                .then()
                .statusCode(200) // Validate status code for update
                .contentType(ContentType.JSON)
                .body("id", equalTo(postId))
                .body("title", equalTo(expectedTitle))
                .body("body", equalTo(expectedBody));
    }

    @Test
    @DisplayName("Verify DELETE /posts/{id} deletes a post and returns 200 OK")
    @Description("This test verifies that a DELETE request to /posts/{id} successfully deletes " +
            "a post and returns a 200 OK status.")
    void testDeletePost() {
        performDeletePostRequest(1);
    }

    @Step("Performing DELETE /posts/{id} request for ID {0} and validating response")
    private void performDeletePostRequest(int postId) {
        given()
                .pathParam("id", postId)
                .when()
                .delete("/posts/{id}")
                .then()
                .statusCode(200); // Validate status code for deletion
    }

    @Test
    @DisplayName("Verify GET /posts/{id} response against JSON schema")
    @Description("This test validates the structure of the response for GET /posts/{id} against " +
            "a predefined JSON schema.")
    void testJsonSchemaValidation() {
        // You need to create a JSON schema file in src/test/resources/schemas/post-schema.json
        performJsonSchemaValidation(1, "schemas/post-schema.json");
    }

    @Step("Performing GET /posts/{id} request for ID {0} and validating against schema '{1}'")
    private void performJsonSchemaValidation(int postId, String schemaPath) {
        given()
                .pathParam("id", postId)
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("post-schema.json")); // Validate against schema
    }

    @Test
    @DisplayName("Verify GET /posts/{id}/comments returns 200 OK and comments for a post")
    @Description("This test verifies that the /posts/{id}/comments endpoint returns a 200 OK status " +
            "and a non-empty array of comments associated with the specified post ID.")
    void testGetCommentsForPost() {
        performGetCommentsForPostRequest(1);
    }

    @Step("Performing GET /posts/{id}/comments request for post ID {0} and validating comments")
    private void performGetCommentsForPostRequest(int postId) {
        given()
                .pathParam("id", postId)
                .when()
                .get("/posts/{id}/comments")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].postId", equalTo(postId))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].email", notNullValue())
                .body("[0].body", notNullValue());
    }

    @Test
    @DisplayName("Verify GET /users returns 200 OK and contains user data")
    @Description("This test verifies that the /users endpoint returns a 200 OK status, " +
            "JSON content type, and a non-empty array with valid user structures.")
    void testGetUsers() {
        performGetUsersRequest();
    }

    @Step("Performing GET /users request and validating response")
    private void performGetUsersRequest() {
        given()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].username", notNullValue())
                .body("[0].email", notNullValue());
    }

    @Test
    @DisplayName("Verify GET /posts/{id} returns 404 Not Found for a non-existent post")
    @Description("This test verifies that requesting a non-existent post ID returns a 404 Not Found status.")
    void testGetNonExistentPost() {
        performGetNonExistentPostRequest(99999);
    }

    @Step("Performing GET /posts/{id} request for non-existent ID {0} and validating 404 status")
    private void performGetNonExistentPostRequest(int postId) {
        given()
                .pathParam("id", postId) // A very high ID to ensure it doesn't exist
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(404); // Expect 404 Not Found
    }

    @Test
    @DisplayName("Verify POST /posts with missing title creates a post (JSONPlaceholder behavior)")
    @Description("This test verifies the behavior of the /posts endpoint when a POST request " +
            "is made with a missing 'title' field. JSONPlaceholder is expected to create " +
            "the post and return a null title.")
    void testCreatePostWithMissingTitle() {
        String bodyContent = "a body without a title";
        int userId = 1;
        String requestBody = String.format("{\"body\": \"%s\", \"userId\": %d}", bodyContent, userId);

        performCreatePostWithMissingTitleRequest(requestBody, bodyContent, userId);
    }

    @Step("Performing POST /posts request with missing title, body '{1}', userId {2} and validating response")
    private void performCreatePostWithMissingTitleRequest(String requestBody, String expectedBody, int expectedUserId) {
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("title", isEmptyOrNullString()) // JSONPlaceholder will return null for missing title
                .body("body", equalTo(expectedBody))
                .body("userId", equalTo(expectedUserId));
    }
}
