package in.foodies.foodiesapi.repositary;

import in.foodies.foodiesapi.entity.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<FoodEntity,Long> {
    Optional<FoodEntity> findById(Long id);
}
