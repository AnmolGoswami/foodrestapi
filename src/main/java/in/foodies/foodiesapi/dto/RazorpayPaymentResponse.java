package in.foodies.foodiesapi.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayPaymentResponse {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}