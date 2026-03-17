package com.novacart.cart.service;

import com.novacart.cart.domain.Cart;
import com.novacart.cart.domain.CartItem;
import com.novacart.cart.domain.WishlistItem;
import com.novacart.cart.dto.AddToCartRequest;
import com.novacart.cart.dto.CartResponse;
import com.novacart.cart.dto.WishlistResponse;
import com.novacart.cart.repository.CartRepository;
import com.novacart.cart.repository.WishlistRepository;
import com.novacart.product.domain.Product;
import com.novacart.product.repository.ProductRepository;
import com.novacart.user.domain.User;
import com.novacart.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, WishlistRepository wishlistRepository,
                       ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse getCart(String email) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse addToCart(String email, AddToCartRequest request) {
        User user = getUser(email);
        Product product = getProduct(request.productId());

        if (!product.isInStock()) {
            throw new ResponseStatusException(BAD_REQUEST, "Product is out of stock");
        }
        if (product.getStockQuantity() < request.quantity()) {
            throw new ResponseStatusException(BAD_REQUEST, "Only " + product.getStockQuantity() + " items available");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.productId()))
                .findFirst().orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + request.quantity();
            if (newQty > product.getStockQuantity()) {
                throw new ResponseStatusException(BAD_REQUEST, "Cannot add more than available stock");
            }
            existingItem.setQuantity(newQty);
        } else {
            cart.getItems().add(new CartItem(cart, product, request.quantity()));
        }

        cart.touch();
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateCartItem(String email, Long itemId, int quantity) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (quantity > item.getProduct().getStockQuantity()) {
                throw new ResponseStatusException(BAD_REQUEST, "Only " + item.getProduct().getStockQuantity() + " available");
            }
            item.setQuantity(quantity);
        }

        cart.touch();
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeFromCart(String email, Long itemId) {
        return updateCartItem(email, itemId, 0);
    }

    @Transactional
    public void clearCart(String email) {
        User user = getUser(email);
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.getItems().clear();
            cart.touch();
            cartRepository.save(cart);
        });
    }

    // --- Wishlist ---

    @Transactional
    public WishlistResponse getWishlist(String email) {
        User user = getUser(email);
        return WishlistResponse.from(wishlistRepository.findByUserOrderByAddedAtDesc(user));
    }

    @Transactional
    public void addToWishlist(String email, Long productId) {
        User user = getUser(email);
        Product product = getProduct(productId);

        if (!wishlistRepository.existsByUserAndProductId(user, productId)) {
            wishlistRepository.save(new WishlistItem(user, product));
        }
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        User user = getUser(email);
        wishlistRepository.deleteByUserAndProductId(user, productId);
    }

    public boolean isInWishlist(String email, Long productId) {
        User user = getUser(email);
        return wishlistRepository.existsByUserAndProductId(user, productId);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
    }
}
