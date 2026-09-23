package com.urbanojvr.monex;

import com.urbanojvr.monex.config.DataDirectoryInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonexApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MonexApplication.class);
        application.addListeners(new DataDirectoryInitializer());
        application.run(args);
    }

}
