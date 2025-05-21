package in.foodies.foodiesapi.controller;

import in.foodies.foodiesapi.dto.CartRequest;
import in.foodies.foodiesapi.dto.CartResponse;
import in.foodies.foodiesapi.entity.CartEntity;
import in.foodies.foodiesapi.entity.User;
import in.foodies.foodiesapi.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {

    @Autowired
    private CartService cartService;

    public CartController() {
        System.out.println("CartController initialized");
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@AuthenticationPrincipal User user, @RequestBody CartRequest request) {
        try {
            System.out.println("Adding to cart for user: " + user.getUsername() + ", foodId: " + request.getFoodId() + ", quantity: " + request.getQuantity());
            cartService.addToCart(user, request.getFoodId(), request.getQuantity());
            return ResponseEntity.ok("Item added to cart");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CartResponse>> getCartItems(@AuthenticationPrincipal User user) {
        try {
            System.out.println("Fetching cart for user: " + user.getUsername());
            List<CartEntity> cartItems = cartService.getUserCart(user);
            List<CartResponse> response = cartItems.stream().map(item ->
                    new CartResponse(
                            item.getFood().getId(),
                            item.getFood().getName(),
                            item.getFood().getImageUrl(),
                            item.getFood().getPrice(),
                            item.getQuantity()
                    )
            ).toList();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Transactional
    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@AuthenticationPrincipal User user, @RequestBody CartRequest cartRequest) {
        try {
            System.out.println("Updating quantity for user: " + user.getUsername() + ", foodId: " + cartRequest.getFoodId() + ", quantity: " + cartRequest.getQuantity());
            CartEntity updatedItem = cartService.updateQuantity(user, cartRequest.getFoodId(), cartRequest.getQuantity());

            if (updatedItem == null) {
                return ResponseEntity.ok("Item removed from cart or no update needed");
            }

            CartResponse response = new CartResponse(
                    updatedItem.getFood().getId(),
                    updatedItem.getFood().getName(),
                    updatedItem.getFood().getImageUrl(),
                    updatedItem.getFood().getPrice(),
                    updatedItem.getQuantity()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeItem(@AuthenticationPrincipal User user, @RequestParam Long foodId) {
        try {
            System.out.println("Removing item for user: " + user.getUsername() + ", foodId: " + foodId);
            cartService.removeItem(user, foodId);
            return ResponseEntity.ok("Item removed from cart");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}