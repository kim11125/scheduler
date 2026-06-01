package com.scheduler.backend.domain.schedule.repository;

import com.scheduler.backend.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s WHERE s.user.id = :userId AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Schedule> findByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT s FROM Schedule s WHERE s.user.id = :userId AND s.date = :date AND s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Schedule> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.user.id = :userId AND YEAR(s.date) = :year AND MONTH(s.date) = :month AND s.deletedAt IS NULL ORDER BY s.date ASC")
    List<Schedule> findByUserIdAndYearMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT s FROM Schedule s WHERE s.deletedAt IS NULL ORDER BY s.createdAt DESC")
    List<Schedule> findAllNotDeleted();

    Optional<Schedule> findByIdAndDeletedAtIsNull(Long id);
}
