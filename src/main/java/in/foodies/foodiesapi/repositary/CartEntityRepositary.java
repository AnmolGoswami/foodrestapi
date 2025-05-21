package in.foodies.foodiesapi.repositary;

import in.foodies.foodiesapi.entity.CartEntity;
import in.foodies.foodiesapi.entity.FoodEntity;
import in.foodies.foodiesapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartEntityRepositary extends JpaRepository<CartEntity , Long> {


    //It fetches all the list of foods
    Optional<CartEntity> findByUserAndFood(User user, FoodEntity food); // still needed for add/update
    List<CartEntity> findAllByUserAndFood(User user, FoodEntity food);  // ✅ used in removeItem
    List<CartEntity> findByUser(User user);
}
