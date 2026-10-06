package com.attend.repo;
import com.attend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SessionRepo extends JpaRepository<Session,Long> { Optional<Session> findByToken(String t); }
