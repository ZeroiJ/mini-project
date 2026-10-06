package com.attend.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="attendance_records",
  uniqueConstraints=@UniqueConstraint(columnNames={"sessionId","studentId"}))
public class Record {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public Long sessionId; public Long studentId;
  public double lat; public double lng; public double distanceMeters;
  public String status = "PRESENT";
  public Instant submittedAt = Instant.now();
}
