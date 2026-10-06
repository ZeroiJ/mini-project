package com.attend.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="attendance_sessions")
public class Session {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(unique=true) public String token;
  public String subject;
  public double classroomLat; public double classroomLng;
  public int radiusMeters = 100;
  public Instant startsAt = Instant.now();
  public Instant endsAt;
  public Long createdBy;
}
