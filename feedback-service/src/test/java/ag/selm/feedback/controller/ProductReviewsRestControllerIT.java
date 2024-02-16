package ag.selm.feedback.controller;

import ag.selm.feedback.entity.ProductReview;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@Slf4j
@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
class ProductReviewsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        this.reactiveMongoTemplate.insertAll(List.of(
                new ProductReview(UUID.fromString("bd7779c2-cb05-11ee-b5f3-df46a1249898"), 1, 1,
                        "Отзыв №1", "user-1"),
                new ProductReview(UUID.fromString("be424abc-cb05-11ee-ab16-2b747e61f570"), 1, 3,
                        "Отзыв №2", "user-2"),
                new ProductReview(UUID.fromString("be77f95a-cb05-11ee-91a3-1bdc94fa9de4"), 1, 5,
                        "Отзыв №3", "user-3")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.reactiveMongoTemplate.remove(ProductReview.class).all().block();
    }

    @Test
    void findProductReviewsByProductId_ReturnsReviews() {
        // given
        // when
        this.webTestClient.mutateWith(mockJwt())
                .mutate().filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.info("========== REQUEST ==========");
                    log.info("{} {}", clientRequest.method(), clientRequest.url());
                    clientRequest.headers().forEach((header, value) -> log.info("{}: {}", header, value));
                    log.info("======== END REQUEST ========");
                    return Mono.just(clientRequest);
                }))
                .build()
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("""
                        [
                            {
                                "id": "bd7779c2-cb05-11ee-b5f3-df46a1249898",
                                "productId": 1,
                                "rating": 1,
                                "review": "Отзыв №1",
                                "userId": "user-1"
                            },
                            {"id": "be424abc-cb05-11ee-ab16-2b747e61f570", "productId": 1, "rating": 3, "review": "Отзыв №2", "userId": "user-2"},
                            {"id": "be77f95a-cb05-11ee-91a3-1bdc94fa9de4", "productId": 1, "rating": 5, "review": "Отзыв №3", "userId": "user-3"}
                        ]""");
    }

    @Test
    void findProductReviewsByProductId_UserIsNotAuthenticated_ReturnsNotAuthorized() {
        // given
        // when
        this.webTestClient
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void createProductReview_RequestIsValid_ReturnsCreatedProductReview() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "На пяторочку!"
                        }""")
                // then
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "На пяторочку!",
                            "userId": "user-tester"
                        }""").jsonPath("$.id").exists()
                .consumeWith(document("feedback/product_reviews/create_product_review",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("productId").type("int").description("Идентификатор товара"),
                                fieldWithPath("rating").type("int").description("Оценка"),
                                fieldWithPath("review").type("string").description("Отзыв")
                        ),
                        responseFields(
                                fieldWithPath("id").type("uuid").description("Идентификатор отзыва"),
                                fieldWithPath("productId").type("int").description("Идентификатор товара"),
                                fieldWithPath("rating").type("int").description("Оценка"),
                                fieldWithPath("review").type("string").description("Отзыв"),
                                fieldWithPath("userId").type("string").description("Идентификатор пользователя")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION)
                                        .description("Ссылка на созданный отзыв о товаре")
                        )));
    }

    @Test
    void createProductReview_RequestIsInvalid_ReturnsBadRequest() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null,
                            "rating": -1,
                            "review": "Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat."
                        }""")
                // then
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json("""
                        {
                            "errors": [
                                "Товар не указан",
                                "Оценка меньше 1",
                                "Размер отзыва не должен превышать 1000 символов"
                            ]
                        }""");
    }

    @Test
    void createProductReview_UserIsNotAuthenticated_ReturnsNotAuthorized() {
        // given

        // when
        this.webTestClient
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "На пяторочку!"
                        }""")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }
}