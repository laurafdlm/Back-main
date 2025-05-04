package edu.uclm.esi.fakeaccountsbe.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.uclm.esi.fakeaccountsbe.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByToken(String token); // Define el método personalizado
	Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);	
	
}