package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.entity.CartEntity;
import in.foodies.foodiesapi.entity.FoodEntity;
import in.foodies.foodiesapi.entity.User;
import in.foodies.foodiesapi.repositary.CartEntityRepositary;
import in.foodies.foodiesapi.repositary.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartEntityRepositary cartEntityRepositary;

    @Autowired
    private FoodRepository foodRepository;

    @Override
    @Transactional
    public CartEntity addToCart(User user, Long foodId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        FoodEntity food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found"));

        CartEntity item = cartEntityRepositary.findByUserAndFood(user, food)
                .orElse(new CartEntity());

        item.setUser(user);
        item.setFood(food);
        item.setQuantity(item.getQuantity() + quantity);
        return cartEntityRepositary.save(item);
    }

    @Override
    @Transactional
    public List<CartEntity> getUserCart(User user) {
        return cartEntityRepositary.findByUser(user);
    }

    @Override
    @Transactional
    public CartEntity updateQuantity(User user, Long foodId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        FoodEntity food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found"));

        CartEntity item = cartEntityRepositary.findByUserAndFood(user, food)
                .orElse(null);

        if (item == null) {
            if (quantity == 0) {
                return null;
            }
            item = new CartEntity();
            item.setUser(user);
            item.setFood(food);
            item.setQuantity(quantity);
        } else {
            if (quantity == 0) {
                cartEntityRepositary.delete(item);
                return null;
            }
            item.setQuantity(quantity);
        }

        return cartEntityRepositary.save(item);
    }

    @Override
    @Transactional
    public void removeItem(User user, Long foodId) {
        FoodEntity food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found"));

        List<CartEntity> items = cartEntityRepositary.findAllByUserAndFood(user, food);

        if (items.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        cartEntityRepositary.deleteAll(items);
    }
}
