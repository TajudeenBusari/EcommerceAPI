package com.tjtechy.order_service.repository;

import com.tjtechy.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

//TODO: Convert this interface to a reactive repository
/**
 * public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
 *     Flux<Order> findByCustomerEmail(String customerEmail);
 *
 *     Flux<Order> findByOrderStatusNot(String orderStatus);
 *
 *     Flux<Order> findByOrderStatusIgnoreCase(String orderStatus);
 * }
 *
 * This aligns with the reactive programming paradigm and ensures better scalability and performance.
 *  * However, if migrating is not feasible in the short term,
 *  * wrapping blocking calls (Option 2) is a temporary workaround
 *  * Switch from Spring Data JPA to Spring Data R2DBC by updating your pom.xml
 *  Check the README.md file for the dependencies to add to your pom.xml
 */

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

//  @EntityGraph(attributePaths = {"orderItems", "orderItems.order"})
//  Optional<Order> findById(Long orderId);
  //Mono<Order> findByIdMono(Long orderId);

  /**
   * Retrieve all orders by customer email
   * @param customerEmail
   * @return
   */
  List<Order>findByCustomerEmail(String customerEmail);

  /**
   * Retrieve all orders except those with status "CANCELLED"
   * The naming convention of the method is important
   * because Spring Data JPA will automatically generate the query
   * based on the method name.
   * StatusNot is a keyword that tells Spring Data JPA to exclude
   * orders with the specified status.
   * @param orderStatus
   * @return
   */
  List<Order>findByOrderStatusNot(String orderStatus);

  /**
   * Retrieve all orders by status
   * Ignore case ensures that the query is case-insensitive
   * @param orderStatus
   * @return
   */
  List<Order> findByOrderStatusIgnoreCase(String orderStatus);

  /**
   * Delete an orderItem by id
   * @param orderItemId
   * This method is used to delete order items by id when order is
   * updated. It is annotated with @Modifying and @Transactional
   * @Transactional because it involves database operations and
   * @Modifying because it modifies the database by deleting an order item.
   * So, when update is done, the old order items are deleted and new ones are added.
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM OrderItem oi WHERE oi.orderItemId = :orderItemId")
  void deleteOrderItemById(@Param("orderItemId")Long orderItemId);
}
