package ca.gbc.productservice;

import ca.gbc.productservice.dto.ProductRequest;
import ca.gbc.productservice.model.Product;
import ca.gbc.productservice.repository.ProductRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.math.BigDecimal;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    @LocalServerPort
    private int port;
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port; // Adjust if your server runs on a different port
        productRepository.deleteAll(); // Clear the database before each test
    }

    @Test
    void testCreateProduct() {
        ProductRequest request = new ProductRequest("Sample Product", "Test Description", BigDecimal.valueOf(9.99));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Sample Product"))
                .body("description", equalTo("Test Description"))
                .body("price", equalTo(9.99f));
    }

    @Test
    void testGetAllProducts() {
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(19.99))
                .build();
        productRepository.save(product);

        given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].name", equalTo("Test Product"))
                .body("[0].description", equalTo("Test Description"))
                .body("[0].price", equalTo(19.99f));
    }

    @Test
    void testUpdateProduct() {
        Product product = Product.builder()
                .name("Old Product")
                .description("Old Description")
                .price(BigDecimal.valueOf(5.99))
                .build();
        product = productRepository.save(product);

        ProductRequest updateRequest = new ProductRequest("Updated Product", "Updated Description", BigDecimal.valueOf(15.99));

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/products/" + product.getId())
                .then()
                .statusCode(204);

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals("Updated Description", updatedProduct.getDescription());
        assertEquals(BigDecimal.valueOf(15.99), updatedProduct.getPrice());
    }

    @Test
    void testDeleteProduct() {
        Product product = Product.builder()
                .name("Product to Delete")
                .description("To be deleted")
                .price(BigDecimal.valueOf(7.99))
                .build();
        product = productRepository.save(product);

        given()
                .when()
                .delete("/api/products/" + product.getId())
                .then()
                .statusCode(204);

        assertEquals(0, productRepository.count());
    }
}

