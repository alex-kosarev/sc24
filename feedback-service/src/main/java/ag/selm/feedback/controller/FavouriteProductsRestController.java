package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.NewFavouriteProductPayload;
import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.service.FavouriteProductsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("feedback-api/favourite-products")
@RequiredArgsConstructor
public class FavouriteProductsRestController {

    private final FavouriteProductsService favouriteProductsService;

    @GetMapping
    public Flux<FavouriteProduct> findFavouriteProducts(Mono<JwtAuthenticationToken> authenticationTokenMono) {
        return authenticationTokenMono.flatMapMany(token ->
                this.favouriteProductsService.findFavouriteProducts(token.getToken().getSubject()));
    }

    @GetMapping("by-product-id/{productId:\\d+}")
    public Mono<FavouriteProduct> findFavouriteProductByProductId(Mono<JwtAuthenticationToken> authenticationTokenMono,
                                                                  @PathVariable("productId") int productId) {
        return authenticationTokenMono.flatMap(token ->
                this.favouriteProductsService.findFavouriteProductByProduct(productId, token.getToken().getSubject()));
    }

    @PostMapping
    public Mono<ResponseEntity<FavouriteProduct>> addProductToFavourites(
            Mono<JwtAuthenticationToken> authenticationTokenMono,
            @Valid @RequestBody Mono<NewFavouriteProductPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        return Mono.zip(authenticationTokenMono, payloadMono)
                .flatMap(tuple -> this.favouriteProductsService
                        .addProductToFavourites(tuple.getT2().productId(), tuple.getT1().getToken().getSubject()))
                .map(favouriteProduct -> ResponseEntity
                        .created(uriComponentsBuilder.replacePath("feedback-api/favourite-products/{id}")
                                .build(favouriteProduct.getId()))
                        .body(favouriteProduct));
    }

    @DeleteMapping("by-product-id/{productId:\\d+}")
    public Mono<ResponseEntity<Void>> removeProductFromFavourites(Mono<JwtAuthenticationToken> authenticationTokenMono,
                                                                  @PathVariable("productId") int productId) {
        return authenticationTokenMono
                .flatMap(token -> this.favouriteProductsService
                        .removeProductFromFavourites(productId, token.getToken().getSubject()))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
