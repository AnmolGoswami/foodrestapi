package in.foodies.foodiesapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userAddress;
    private String phoneNumber;
    private String email;

    private double amount;

    private String paymentStatus;
    @JsonProperty("razorpayOrderId")
    private String razorpayOrderId;
    private String razorpaySignature;
    private String orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> orderItems ;



    @ManyToOne
    @JsonIgnore
    private User user;




}
