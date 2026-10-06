package com.attend.repo;
import com.attend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RecordRepo extends JpaRepository<AttendanceRecord,Long> {
  boolean existsBySessionIdAndStudentId(Long s, Long u);
  List<AttendanceRecord> findBySessionId(Long s);
}
