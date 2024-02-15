package ag.selm.feedback.service;

import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.repository.ProductReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultProductReviewsServiceTest {

    @Mock
    ProductReviewRepository productReviewRepository;

    @InjectMocks
    DefaultProductReviewsService service;

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        // given
        doAnswer(invocation -> Mono.justOrEmpty(invocation.getArguments()[0])).when(this.productReviewRepository)
                .save(any());

        // when
        StepVerifier.create(this.service.createProductReview(1, 3, "Ну, на троечку",
                        "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"))
                // then
                .expectNextMatches(productReview ->
                        productReview.getProductId() == 1 && productReview.getRating() == 3 &&
                                productReview.getUserId().equals("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c") &&
                                productReview.getReview().equals("Ну, на троечку") && productReview.getId() != null)
                .verifyComplete();

        verify(this.productReviewRepository)
                .save(argThat(productReview ->
                        productReview.getProductId() == 1 && productReview.getRating() == 3 &&
                                productReview.getUserId().equals("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c") &&
                                productReview.getReview().equals("Ну, на троечку") && productReview.getId() != null));
    }

    @Test
    void findProductReviewsByProduct_ReturnsProductReviews() {
        // given
        doReturn(Flux.fromIterable(List.of(
                new ProductReview(UUID.fromString("bd7779c2-cb05-11ee-b5f3-df46a1249898"), 1, 1,
                        "Отзыв №1", "user-1"),
                new ProductReview(UUID.fromString("be424abc-cb05-11ee-ab16-2b747e61f570"), 1, 3,
                        "Отзыв №2", "user-2"),
                new ProductReview(UUID.fromString("be77f95a-cb05-11ee-91a3-1bdc94fa9de4"), 1, 5,
                        "Отзыв №3", "user-3")
        ))).when(this.productReviewRepository).findAllByProductId(1);

        // when
        StepVerifier.create(this.service.findProductReviewsByProduct(1))
                // then
                .expectNext(
                        new ProductReview(UUID.fromString("bd7779c2-cb05-11ee-b5f3-df46a1249898"), 1, 1,
                                "Отзыв №1", "user-1"),
                        new ProductReview(UUID.fromString("be424abc-cb05-11ee-ab16-2b747e61f570"), 1, 3,
                                "Отзыв №2", "user-2"),
                        new ProductReview(UUID.fromString("be77f95a-cb05-11ee-91a3-1bdc94fa9de4"), 1, 5,
                                "Отзыв №3", "user-3")
                )
                .verifyComplete();
    }
}