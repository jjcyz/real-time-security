package dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class.
 */
@SpringBootApplication(scanBasePackages = "dashboard")
public class SecurityDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityDashboardApplication.class, args);
    }
}
