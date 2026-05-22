package ufpb.dcx.house.manager.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ufpb.dcx.house.manager.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
