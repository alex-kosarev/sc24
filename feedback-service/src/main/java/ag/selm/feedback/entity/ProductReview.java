package ag.selm.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("product_review")
public class ProductReview {

    @Id
    private UUID id;

    private int productId;

    private int rating;

    private String review;

    private String userId;
}
