package com.novacart.cart.service;

import com.novacart.cart.domain.Cart;
import com.novacart.cart.domain.CartItem;
import com.novacart.cart.domain.WishlistItem;
import com.novacart.cart.dto.AddToCartRequest;
import com.novacart.cart.repository.CartRepository;
import com.novacart.cart.repository.WishlistRepository;
import com.novacart.product.domain.Product;
import com.novacart.product.domain.ProductStatus;
import com.novacart.product.repository.ProductRepository;
import com.novacart.user.domain.User;
import com.novacart.user.domain.UserRole;
import com.novacart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product inStockProduct;
    private Product outOfStockProduct;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = spy(new User("user@novacart.com", "$2a$10$hash", UserRole.CUSTOMER, true));
        doReturn(1L).when(user).getId();

        inStockProduct = spy(new Product("iPhone 17", "iphone-17", "desc", "short",
                new BigDecimal("112000"), new BigDecimal("114000"), "Apple", 10, null, "SKU001", 200));
        doReturn(1L).when(inStockProduct).getId();

        outOfStockProduct = spy(new Product("Old Phone", "old-phone", "desc", "short",
                new BigDecimal("5000"), new BigDecimal("6000"), "Nokia", 0, null, "SKU002", 100));
        doReturn(2L).when(outOfStockProduct).getId();

        cart = new Cart(user);
    }

    // ════════════════════════════════════════════════
    // GET CART
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("getCart()")
    class GetCart {

        @Test
        @DisplayName("should return existing cart")
        void shouldReturnExistingCart() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(cartRepository.findByUserWithItems(user)).thenReturn(Optional.of(cart));

            var result = cartService.getCart("user@novacart.com");

            assertThat(result).isNotNull();
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create new cart when none exists")
        void shouldCreateNewCartWhenNoneExists() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(cartRepository.findByUserWithItems(user)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            var result = cartService.getCart("user@novacart.com");

            assertThat(result).isNotNull();
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("should throw 404 when user not found")
        void shouldThrow404WhenUserNotFound() {
            when(userRepository.findByEmail("unknown@novacart.com")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> cartService.getCart("unknown@novacart.com"));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }

    // ════════════════════════════════════════════════
    // ADD TO CART
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("addToCart()")
    class AddToCart {

        @Test
        @DisplayName("should add new item to cart")
        void shouldAddNewItemToCart() {
            AddToCartRequest request = new AddToCartRequest(1L, 2);
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(inStockProduct));
            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
            when(cartRepository.save(cart)).thenReturn(cart);

            var result = cartService.addToCart("user@novacart.com", request);

            assertThat(result).isNotNull();
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("should increase quantity when item already in cart")
        void shouldIncreaseQuantityWhenItemAlreadyInCart() {
            AddToCartRequest request = new AddToCartRequest(1L, 2);
            CartItem existingItem = new CartItem(cart, inStockProduct, 3);
            cart.getItems().add(existingItem);

            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(inStockProduct));
            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
            when(cartRepository.save(cart)).thenReturn(cart);

            cartService.addToCart("user@novacart.com", request);

            assertThat(existingItem.getQuantity()).isEqualTo(5); // 3 + 2
        }

        @Test
        @DisplayName("should throw 400 when product is out of stock")
        void shouldThrow400WhenProductOutOfStock() {
            AddToCartRequest request = new AddToCartRequest(2L, 1);
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(2L)).thenReturn(Optional.of(outOfStockProduct));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> cartService.addToCart("user@novacart.com", request));

            assertThat(ex.getStatusCode().value()).isEqualTo(400);
            assertThat(ex.getReason()).isEqualTo("Product is out of stock");
        }

        @Test
        @DisplayName("should throw 400 when requested quantity exceeds stock")
        void shouldThrow400WhenQuantityExceedsStock() {
            AddToCartRequest request = new AddToCartRequest(1L, 15); // stock is 10
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(inStockProduct));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> cartService.addToCart("user@novacart.com", request));

            assertThat(ex.getStatusCode().value()).isEqualTo(400);
            assertThat(ex.getReason()).contains("10 items available");
        }

        @Test
        @DisplayName("should throw 400 when combined quantity exceeds stock")
        void shouldThrow400WhenCombinedQuantityExceedsStock() {
            AddToCartRequest request = new AddToCartRequest(1L, 8);
            CartItem existingItem = new CartItem(cart, inStockProduct, 5); // already 5 in cart
            cart.getItems().add(existingItem);

            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(inStockProduct));
            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> cartService.addToCart("user@novacart.com", request));

            assertThat(ex.getStatusCode().value()).isEqualTo(400);
            assertThat(ex.getReason()).isEqualTo("Cannot add more than available stock");
        }

        @Test
        @DisplayName("should create new cart when user has none")
        void shouldCreateNewCartWhenNoneExists() {
            AddToCartRequest request = new AddToCartRequest(1L, 1);
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(inStockProduct));
            when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            cartService.addToCart("user@novacart.com", request);

            verify(cartRepository, times(2)).save(any(Cart.class)); // once for create, once for update
        }
    }

    // ════════════════════════════════════════════════
    // UPDATE CART ITEM
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("updateCartItem()")
    class UpdateCartItem {

        @Test
        @DisplayName("should update item quantity")
        void shouldUpdateItemQuantity() {
            CartItem item = spy(new CartItem(cart, inStockProduct, 2));
            doReturn(10L).when(item).getId();
            cart.getItems().add(item);
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(cartRepository.findByUserWithItems(user)).thenReturn(Optional.of(cart));
            when(cartRepository.save(cart)).thenReturn(cart);

            assertDoesNotThrow(() -> cartService.updateCartItem("user@novacart.com", 10L, 5));
            assertThat(item.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("should remove item when quantity is 0")
        void shouldRemoveItemWhenQuantityIsZero() {
            CartItem item = spy(new CartItem(cart, inStockProduct, 3));
            doReturn(10L).when(item).getId();
            cart.getItems().add(item);
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(cartRepository.findByUserWithItems(user)).thenReturn(Optional.of(cart));
            when(cartRepository.save(cart)).thenReturn(cart);

            cartService.updateCartItem("user@novacart.com", 10L, 0);

            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("should throw 404 when cart not found")
        void shouldThrow404WhenCartNotFound() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(cartRepository.findByUserWithItems(user)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> cartService.updateCartItem("user@novacart.com", 1L, 2));

            assertThat(ex.getStatusCode().value()).isEqualTo(404);
            assertThat(ex.getReason()).isEqualTo("Cart not found");
        }
    }

    // ════════════════════════════════════════════════
    // CLEAR CART
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("clearCart()")
    class ClearCart {

        @Test
        @DisplayName("should clear all items from cart")
        void shouldClearAllItemsFromCart() {
            cart.getItems().add(new CartItem(cart, inStockProduct, 2));
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

            cartService.clearCart("user@novacart.com");

            assertThat(cart.getItems()).isEmpty();
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("should do nothing when cart does not exist")
        void shouldDoNothingWhenCartDoesNotExist() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> cartService.clearCart("user@novacart.com"));
            verify(cartRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════
    // WISHLIST
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("Wishlist operations")
    class Wishlist {

        @Test
        @DisplayName("should add product to wishlist when not already present")
        void shouldAddToWishlistWhenNotPresent() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(inStockProduct));
            when(wishlistRepository.existsByUserAndProductId(user, 1L)).thenReturn(false);

            cartService.addToWishlist("user@novacart.com", 1L);

            verify(wishlistRepository).save(any(WishlistItem.class));
        }

        @Test
        @DisplayName("should not add duplicate product to wishlist")
        void shouldNotAddDuplicateToWishlist() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(productRepository.findById(1L)).thenReturn(Optional.of(inStockProduct));
            when(wishlistRepository.existsByUserAndProductId(user, 1L)).thenReturn(true);

            cartService.addToWishlist("user@novacart.com", 1L);

            verify(wishlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("should remove product from wishlist")
        void shouldRemoveFromWishlist() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));

            cartService.removeFromWishlist("user@novacart.com", 1L);

            verify(wishlistRepository).deleteByUserAndProductId(user, 1L);
        }

        @Test
        @DisplayName("should return true when product is in wishlist")
        void shouldReturnTrueWhenInWishlist() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(wishlistRepository.existsByUserAndProductId(user, 1L)).thenReturn(true);

            boolean result = cartService.isInWishlist("user@novacart.com", 1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when product is not in wishlist")
        void shouldReturnFalseWhenNotInWishlist() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(user));
            when(wishlistRepository.existsByUserAndProductId(user, 1L)).thenReturn(false);

            boolean result = cartService.isInWishlist("user@novacart.com", 1L);

            assertThat(result).isFalse();
        }
    }
}