package log4shell.victim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GreetingApplication {

    public static void main(String[] args) {
        //this emulates old java behaviour
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");

        SpringApplication.run(GreetingApplication.class, args);
    }
}