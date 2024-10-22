package com.example.users.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.users.register.RegistationRequest;
import com.example.users.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);
	Optional<User> findByEmail(String email);

}
