package com.attend.repo;
import com.attend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepo extends JpaRepository<User,Long> { Optional<User> findByIdentifier(String i); }
