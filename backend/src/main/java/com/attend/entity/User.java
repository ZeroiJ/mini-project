package com.attend.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="users")
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public String name;
  @Column(unique=true) public String identifier;
  public String passwordHash;
  public String role; // PROFESSOR | STUDENT
}
