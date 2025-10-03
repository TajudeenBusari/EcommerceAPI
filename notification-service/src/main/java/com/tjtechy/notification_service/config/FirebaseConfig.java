/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.*;

@Configuration
public class FirebaseConfig {

  @Value("${FIREBASE_CONFIG_PATH:firebase-service-account.json}")
  private String firebaseConfigPath;

  private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  @PostConstruct
  public void initFireBase() throws IOException {
    logger.info("*********FirebaseConfigPath***********: {}", firebaseConfigPath);
    File file = new File(firebaseConfigPath);
    logger.info("Exists: {}, Absolute Path: {}, Readable: {}", file.exists(), file.getAbsolutePath(), file.canRead());

    if(!file.exists()) {
      throw new FileNotFoundException("Firebase configuration file not found at path: " + firebaseConfigPath);
    }
    //works locally and in Docker container
    if (FirebaseApp.getApps().isEmpty()) {
      try(FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath)){
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);
        logger.info("Firebase has been initialized successfully.");
      } catch (Exception e) {
        logger.error("Failed to initialize firebase: {}", firebaseConfigPath,e);
        throw e;
      }
    }

    //works in docker container, BUT I HAVE NOT TESTED IT LOCALLY
    /**
     * After I deleted the image, container, removed all unused volumes and networks
     * and rebuilt the project, it worked perfectly.
     * The error of mark/reset vanished.
     * I guess the issue was with the Docker caching mechanism.
     * It might have cached a previous state of the file or something related to the stream.
     * By removing everything and rebuilding, I forced Docker to fetch the latest state of the file
     * and create a fresh stream, which resolved the issue.
     * Don't REMOVE THIS:
     */
//    if (FirebaseApp.getApps().isEmpty()) {
//      byte[] jsonBytes = Files.readAllBytes(Paths.get(firebaseConfigPath));
//      try(ByteArrayInputStream serviceAccount = new ByteArrayInputStream(jsonBytes)){
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//        FirebaseApp.initializeApp(options);
//        logger.info("Firebase has been initialized successfully.");
//      } catch (Exception e) {
//        logger.error("Failed to initialize firebase: {}", firebaseConfigPath,e);
//        throw e;
//      }
//    }
  }
}
