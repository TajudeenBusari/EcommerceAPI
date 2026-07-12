### This test-helper module is for testing only. It will be added as a dependency to other modules for testing purposes only
    <dependency>
        <groupId>com.tjtechy</groupId>
        <artifactId>test-helper</artifactId>
        <version>${project.version}</version>
        <scope>test</scope>
    </dependency>
### Maven only makes it available during test-compile and test-runtime
### It is not included in the production JAR, Docker image, runtime classpath
### So when services are packaged, none of the following is included:
   - TestJwtGenerator.java
   - private-test.pem
   - private-test.pem
   - TestConfiguration.java
