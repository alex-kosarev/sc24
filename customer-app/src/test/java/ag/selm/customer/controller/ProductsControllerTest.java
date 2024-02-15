package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.entity.FavouriteProduct;
import ag.selm.customer.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @Mock
    ProductsClient productsClient;

    @Mock
    FavouriteProductsClient favouriteProductsClient;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsListPage_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();

        doReturn(Flux.fromIterable(List.of(
                new Product(1, "Отфильтрованный товар №1", "Описание отфильтрованного товара №1"),
                new Product(2, "Отфильтрованный товар №2", "Описание отфильтрованного товара №2"),
                new Product(3, "Отфильтрованный товар №3", "Описание отфильтрованного товара №3")
        ))).when(this.productsClient).findAllProducts("фильтр");

        // when
        StepVerifier.create(this.controller.getProductsListPage(model, "фильтр"))
                // then
                .expectNext("customer/products/list")
                .verifyComplete();

        assertEquals("фильтр", model.getAttribute("filter"));
        assertEquals(List.of(
                        new Product(1, "Отфильтрованный товар №1", "Описание отфильтрованного товара №1"),
                        new Product(2, "Отфильтрованный товар №2", "Описание отфильтрованного товара №2"),
                        new Product(3, "Отфильтрованный товар №3", "Описание отфильтрованного товара №3")),
                model.getAttribute("products"));

        verify(this.productsClient).findAllProducts("фильтр");
        verifyNoMoreInteractions(this.productsClient);
        verifyNoInteractions(this.favouriteProductsClient);
    }

    @Test
    void getFavouriteProductsPage_ReturnsFavouriteProductsPage() {
        // given
        var model = new ConcurrentModel();

        doReturn(Flux.fromIterable(List.of(
                new Product(1, "Отфильтрованный товар №1", "Описание отфильтрованного товара №1"),
                new Product(2, "Отфильтрованный товар №2", "Описание отфильтрованного товара №2"),
                new Product(3, "Отфильтрованный товар №3", "Описание отфильтрованного товара №3")
        ))).when(this.productsClient).findAllProducts("фильтр");

        doReturn(Flux.fromIterable(List.of(
                new FavouriteProduct(UUID.fromString("a16f0218-cbaf-11ee-9e6c-6b0fa3631587"), 1),
                new FavouriteProduct(UUID.fromString("a42ff37c-cbaf-11ee-8b1d-cb00912914b5"), 3)
        ))).when(this.favouriteProductsClient).findFavouriteProducts();

        // when
        StepVerifier.create(this.controller.getFavouriteProductsPage(model, "фильтр"))
                // then
                .expectNext("customer/products/favourites")
                .verifyComplete();

        assertEquals("фильтр", model.getAttribute("filter"));
        assertEquals(List.of(
                        new Product(1, "Отфильтрованный товар №1", "Описание отфильтрованного товара №1"),
                        new Product(3, "Отфильтрованный товар №3", "Описание отфильтрованного товара №3")),
                model.getAttribute("products"));

        verify(this.productsClient).findAllProducts("фильтр");
        verify(this.favouriteProductsClient).findFavouriteProducts();
        verifyNoMoreInteractions(this.productsClient, this.favouriteProductsClient);

    }
}