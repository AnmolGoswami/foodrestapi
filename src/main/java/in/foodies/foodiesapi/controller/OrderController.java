package in.foodies.foodiesapi.controller;

import in.foodies.foodiesapi.dto.PlaceOrderRequest;
import in.foodies.foodiesapi.dto.RazorpayPaymentResponse;
import in.foodies.foodiesapi.entity.OrderEntity;
import in.foodies.foodiesapi.entity.User;
import in.foodies.foodiesapi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<OrderEntity> placeOrder(@AuthenticationPrincipal User user, @RequestBody PlaceOrderRequest request) {
        OrderEntity order = orderService.placeOrder(user, request);
        return ResponseEntity.status(201).body(order); // Changed to 201
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestBody RazorpayPaymentResponse response) {
        try {
            boolean verified = orderService.verifyPayment(response);
            return ResponseEntity.ok("Payment Verified Successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed: " + e.getMessage());
        }
    }

    @GetMapping("/user-orders")
    public ResponseEntity<List<OrderEntity>> getUserOrders(@AuthenticationPrincipal User user) {
        List<OrderEntity> orders = orderService.getUserOrders(user);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderEntity>> getAllOrders() {
        List<OrderEntity> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderEntity> getOrderById(@PathVariable Long orderId) {
        OrderEntity order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/admin/update-status/{orderId}")
    public ResponseEntity<OrderEntity> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        OrderEntity updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }
}