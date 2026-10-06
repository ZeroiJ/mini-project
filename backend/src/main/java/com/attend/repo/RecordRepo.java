package com.attend.repo;
import com.attend.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RecordRepo extends JpaRepository<Record,Long> {
  boolean existsBySessionIdAndStudentId(Long s, Long u);
  List<Record> findBySessionId(Long s);
}
