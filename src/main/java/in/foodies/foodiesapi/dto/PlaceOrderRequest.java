package in.foodies.foodiesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequest {

    private String email;
    private String phone;
    private String address;
    private int totalAmount;
    private List<Item> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static  class Item{
        private Long id;
        private int quantity;

    }
}
