package org.libelektra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = "org.libelektra")
public class ValidatorMain {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(ValidatorMain.class, args);
    }

}
