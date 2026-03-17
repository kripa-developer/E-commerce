package com.novacart.cart.web;

import com.novacart.cart.dto.AddToCartRequest;
import com.novacart.cart.dto.CartResponse;
import com.novacart.cart.dto.WishlistResponse;
import com.novacart.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ---- Cart endpoints ----

    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart(Authentication auth) {
        return ResponseEntity.ok(cartService.getCart(auth.getName()));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<CartResponse> addToCart(Authentication auth, @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(auth.getName(), request));
    }

    @PatchMapping("/cart/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(Authentication auth,
                                                        @PathVariable Long itemId,
                                                        @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(auth.getName(), itemId, quantity));
    }

    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(Authentication auth, @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeFromCart(auth.getName(), itemId));
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart(Authentication auth) {
        cartService.clearCart(auth.getName());
        return ResponseEntity.noContent().build();
    }

    // ---- Wishlist endpoints ----

    @GetMapping("/wishlist")
    public ResponseEntity<WishlistResponse> getWishlist(Authentication auth) {
        return ResponseEntity.ok(cartService.getWishlist(auth.getName()));
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<Void> addToWishlist(Authentication auth, @PathVariable Long productId) {
        cartService.addToWishlist(auth.getName(), productId);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<Void> removeFromWishlist(Authentication auth, @PathVariable Long productId) {
        cartService.removeFromWishlist(auth.getName(), productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/wishlist/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(Authentication auth, @PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("inWishlist", cartService.isInWishlist(auth.getName(), productId)));
    }
}
