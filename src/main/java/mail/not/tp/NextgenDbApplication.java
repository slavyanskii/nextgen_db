package mail.not.tp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NextgenDbApplication {

    public static final Logger logger = LogManager.getLogger(NextgenDbApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NextgenDbApplication.class, args);
    }
}
