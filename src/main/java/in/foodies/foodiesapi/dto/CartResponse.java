package in.foodies.foodiesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long foodId;
    private String name;
    private String imageUrl;
    private double price;
    private int quantity;


}
