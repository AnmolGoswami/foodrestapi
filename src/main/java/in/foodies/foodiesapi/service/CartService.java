package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.entity.CartEntity;
import in.foodies.foodiesapi.entity.User;

import java.util.List;

public interface CartService {
// It helps to add the item in the cart
    public CartEntity addToCart(User user,Long foodId,int quantity);
// Getting the list of items
    public List<CartEntity> getUserCart(User user);
//updating the quantity
    public CartEntity updateQuantity(User user, Long foodId, int quantity);

    //remove the item from cart
    public void removeItem(User user,Long foodId);


}
