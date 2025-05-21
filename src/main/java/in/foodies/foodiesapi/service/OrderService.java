package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.dto.PlaceOrderRequest;
import in.foodies.foodiesapi.dto.RazorpayPaymentResponse;
import in.foodies.foodiesapi.entity.OrderEntity;
import in.foodies.foodiesapi.entity.User;

import java.util.List;

public interface OrderService {


    public OrderEntity placeOrder(User user, PlaceOrderRequest request);

    public boolean verifyPayment(RazorpayPaymentResponse response);

    public List<OrderEntity> getUserOrders(User user);

    public List<OrderEntity> getAllOrders();


    public OrderEntity getOrderById(Long orderId);

    public OrderEntity updateOrderStatus(Long orderId,String Status);
}
