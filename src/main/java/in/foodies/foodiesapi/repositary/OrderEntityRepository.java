package in.foodies.foodiesapi.repositary;

import in.foodies.foodiesapi.entity.OrderEntity;
import in.foodies.foodiesapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderEntityRepository extends JpaRepository<OrderEntity,Long> {

    List<OrderEntity> findByUser(User user);

    Optional<OrderEntity> findByRazorpayOrderId(String orderId);
}
