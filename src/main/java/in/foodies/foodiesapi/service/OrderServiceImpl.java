package in.foodies.foodiesapi.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.foodies.foodiesapi.dto.PlaceOrderRequest;
import in.foodies.foodiesapi.dto.RazorpayPaymentResponse;
import in.foodies.foodiesapi.entity.FoodEntity;
import in.foodies.foodiesapi.entity.OrderEntity;
import in.foodies.foodiesapi.entity.OrderItem;
import in.foodies.foodiesapi.entity.User;
import in.foodies.foodiesapi.repositary.FoodRepository;
import in.foodies.foodiesapi.repositary.OrderEntityRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderEntityRepository orderRepo;

    @Autowired
    private FoodRepository foodRepository;

    @Value("${razorpay.api.key}")
    private String key;

    @Value("${razorpay.api.secret}")
    private String secret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            razorpayClient = new RazorpayClient(key, secret);
            logger.info("Razorpay client initialized successfully");
        } catch (RazorpayException e) {
            logger.error("Failed to initialize Razorpay client", e);
            throw new RuntimeException("Failed to initialize Razorpay Client", e);
        }
    }
    @Override
    public OrderEntity placeOrder(User user, PlaceOrderRequest request) {
        logger.info("Placing order for user: {}", user.getEmail());
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setUserAddress(request.getAddress());
        order.setPhoneNumber(request.getPhone());
        order.setEmail(request.getEmail());
        order.setPaymentStatus("PENDING");
        order.setOrderStatus("CREATED");

        List<OrderItem> orderItems = new ArrayList<>();
        double calculatedSubTotal = 0;

        // Calculate subtotal for order items (for storage, not Razorpay amount)
        for (PlaceOrderRequest.Item item : request.getItems()) {
            FoodEntity food = foodRepository.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("Food item not found: " + item.getId()));
            OrderItem orderItem = new OrderItem();
            orderItem.setFood(food);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setName(food.getName());
            orderItem.setCategory(food.getCategory());
            orderItem.setDescription(food.getDescription());
            orderItem.setImageUrl(food.getImageUrl());

            double itemTotal = food.getPrice() * item.getQuantity();
            orderItem.setPrice(itemTotal);
            calculatedSubTotal += itemTotal;

            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        // Use totalAmount from request for Razorpay order
        double totalAmount = request.getTotalAmount();
        order.setAmount(totalAmount);
        order.setOrderItems(orderItems);

        // Optional: Validate totalAmount against calculatedSubTotal
        double expectedSubTotal = calculatedSubTotal;
        double shipping = expectedSubTotal == 0 ? 0 : 10; // Match frontend logic
        double tax = expectedSubTotal * 0.1; // Match frontend logic
        double expectedTotal = expectedSubTotal + shipping + tax;
        if (Math.abs(totalAmount - expectedTotal) > 0.01) {
            logger.warn("Total amount mismatch: received={}, expected={}", totalAmount, expectedTotal);
            throw new RuntimeException("Invalid total amount: does not match expected total");
        }

        // Creating Razorpay order
        JSONObject options = new JSONObject();
        options.put("amount", (int) (totalAmount * 100)); // Use request's totalAmount
        options.put("currency", "INR");
        options.put("receipt", UUID.randomUUID().toString());

        try {
            Order razorOrder = razorpayClient.orders.create(options);
            String razorpayOrderId = razorOrder.get("id");
            order.setRazorpayOrderId(razorpayOrderId);
            logger.info("Razorpay order created: {}", razorpayOrderId);
        } catch (RazorpayException e) {
            logger.error("Failed to create Razorpay order", e);
            throw new RuntimeException("Failed to create the Razorpay order", e);
        }

        OrderEntity savedOrder = orderRepo.save(order);
        logger.info("Order saved with ID: {}", savedOrder.getId());
        return savedOrder;
    }
    @Override
    public boolean verifyPayment(RazorpayPaymentResponse response) {
        logger.info("Verifying payment: orderId={}, paymentId={}",
                response.getRazorpayOrderId(), response.getRazorpayPaymentId());

        // Validate payment response
        if (response.getRazorpayOrderId() == null || response.getRazorpayPaymentId() == null ||
                response.getRazorpaySignature() == null) {
            logger.error("Invalid payment data: orderId={}, paymentId={}, signature={}",
                    response.getRazorpayOrderId(), response.getRazorpayPaymentId(),
                    response.getRazorpaySignature());
            throw new RuntimeException("Invalid payment data: orderId, paymentId, or signature is null");
        }

        try {
            String payload = response.getRazorpayOrderId() + "|" + response.getRazorpayPaymentId();
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Hex.encodeHexString(hash);

            if (!expectedSignature.equals(response.getRazorpaySignature())) {
                logger.error("Signature mismatch: expected={}, received={}",
                        expectedSignature, response.getRazorpaySignature());
                throw new RuntimeException("Invalid payment signature: Signature mismatch");
            }

            // Update order status after successful verification
            OrderEntity order = orderRepo.findByRazorpayOrderId(response.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found for orderId: " +
                            response.getRazorpayOrderId()));
            order.setPaymentStatus("PAID");
            order.setOrderStatus("CONFIRMED");
            orderRepo.save(order);
            logger.info("Payment verified and order updated: orderId={}", response.getRazorpayOrderId());

            return true;

        } catch (Exception e) {
            logger.error("Payment verification failed", e);
            throw new RuntimeException("Payment verification failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OrderEntity> getUserOrders(User user) {
        logger.info("Fetching orders for user: {}", user.getEmail());
        return orderRepo.findByUser(user);
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        logger.info("Fetching all orders");
        return orderRepo.findAll();
    }

    @Override
    public OrderEntity getOrderById(Long orderId) {
        logger.info("Fetching order by ID: {}", orderId);
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    @Override
    public OrderEntity updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order status for orderId: {} to {}", orderId, status);
        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setOrderStatus(status);
        OrderEntity updatedOrder = orderRepo.save(order);
        logger.info("Order status updated: orderId={}", orderId);
        return updatedOrder;
    }
}