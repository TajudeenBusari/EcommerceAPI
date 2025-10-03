/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.controller;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/notification")
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * @return
   * The method is not exposed to the public, can only be accessed by the Admin.
   * This endpoint fetches all notifications from the system.
   * It returns a Result object containing a list of notifications.
   */
  @Operation(summary = "Fetch all notifications (Admin only)",
          description = "Retrieve all notifications from the system. This endpoint is restricted to admin users.",
  responses = {@ApiResponse(responseCode = "200", description = "Get All Success")})
  @GetMapping
  public Result getAllNotifications(){
    var nofications = notificationService.getAllNotifications();
    return new Result("Notifications fetched successfully", true, nofications, StatusCode.SUCCESS);
  }

  /**
   * @param notificationId
   * @return
   * The method is not exposed to the public, can only be accessed by the Admin.
   * This endpoint fetches a single notification by its ID.
   * It returns a Result object containing the notification details if found.
   * If the notification is not found, it should ideally return a run time exception.
   */
  @Operation(summary = "Fetch a notification by ID (Admin only)",
          description = "Retrieve a specific notification using its ID. This endpoint is restricted to admin users.",
          responses = {@ApiResponse(responseCode = "200", description = "Get By ID Success"),
                  @ApiResponse(responseCode = "404", description = "Notification Not Found")})
  @GetMapping("/{notificationId}")
  public Result getNotifications(@PathVariable Long notificationId){
    var notification = notificationService.getNotificationById(notificationId);
    return new Result("Notification fetched successfully", true, notification, StatusCode.SUCCESS);
  }

  /**
   * @param notificationId
   * @return
   * The method is not exposed to the public, can only be accessed by the Admin.
   * It returns a Result object indicating the success of the operation.
   */
  @Operation(summary = "Delete a notification by ID (Admin only)",
          description = "Remove a specific notification using its ID. This endpoint is restricted to admin users.",
          responses = {@ApiResponse(responseCode = "200", description = "Delete By ID Success"),
                  @ApiResponse(responseCode = "404", description = "Notification Not Found")})
  @DeleteMapping("/{notificationId}")
  public Result removeNotificationById(@PathVariable Long notificationId){
    notificationService.removeNotification(notificationId);
    return new Result("Notification removed successfully", true, StatusCode.SUCCESS);
  }

  /**
   * This is strictly for Admin use only. Meant to bulk delete notifications that are older than 30 days.
   * @return
   */
  @Operation(summary = "Bulk delete notifications older than or equal to 30 days (Admin only)",
          description = "Remove all notifications that are older than or equal to 30 days. This endpoint is restricted to admin users.",
          responses = {@ApiResponse(responseCode = "200", description = "Bulk Delete Success"),
                  @ApiResponse(responseCode = "404", description = "No notifications found that are 30 days or older")})
  @DeleteMapping("/cleanup")
  public Result removeAllNotifications(){
    boolean deleted = notificationService.bulkRemoveNotificationsBySentAtLessThanEqual();

    if (!deleted) {
      return new Result("No notifications found that are 30 days or older", false, StatusCode.NOT_FOUND);
    }
    return new Result("Notifications of 30 days or older successfully deleted", true, StatusCode.SUCCESS);
  }

  /**
   * This is strictly for Admin use only. Meant to bulk delete notifications by id irrespective of their sent date.
   * @param notificationIds
   * @return
   */
  @Operation(summary = "Bulk delete notifications by IDs (Admin only)",
          description = "Remove multiple notifications using their IDs. This endpoint is restricted to admin users.",
          responses = {@ApiResponse(responseCode = "200", description = "Bulk Delete Success"),
                  @ApiResponse(responseCode = "404", description = "One or more notifications not found")})
  @DeleteMapping("/bulk-delete")
  public Result bulkRemoveNotifications(@RequestBody List<Long> notificationIds){
    notificationService.bulkRemoveNotification(notificationIds);
    return new Result("Notifications removed successfully", true, StatusCode.SUCCESS);
  }

}
