package in.foodies.foodiesapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private  int quantity;

    private double price;

    private String category;

    private String imageUrl;

    private String description;

    private String name;

    @ManyToOne
    private FoodEntity food;

    @ManyToOne
    private OrderEntity order;
}
