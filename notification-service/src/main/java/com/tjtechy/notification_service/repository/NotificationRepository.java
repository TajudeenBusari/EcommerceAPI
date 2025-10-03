/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */
package com.tjtechy.notification_service.repository;

import com.tjtechy.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  /**
   * With deleteAllBy... method, Spring Data JPA will generate a query that deletes all records
   * where the sentAt field is less than or equal to the specified date.
   * A DELETE or UPDATE query returns an int (or a long) indicating the number of rows affected by the query, or
   * you can ignore the result and use void as the return type.
   * So, in spring Data JPA, the return type of such methods can be void, int, or long.
   * It cannot be a boolean bcos the underlying JPA does not return a boolean value for delete operations but
   * the number of rows affected(count).
   * DELETE FROM notification WHERE sent_at <= '2025-08-01';
   * This has multiple implementations in the NotificationServiceImpl class.
   * @param cuffOffDate
   * @return
   */
  long deleteAllBySentAtLessThanEqual(LocalDate cuffOffDate);
}
