package in.foodies.foodiesapi.repositary;

import in.foodies.foodiesapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepositary extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
