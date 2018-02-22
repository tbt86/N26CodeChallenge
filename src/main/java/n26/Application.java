package n26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /*

    TODO: log config, write readme, assumptions, create github
   write consistency tradeoff


Assumption: value and timestamp is not unique
Ignored timezones and assumed everything is UTC
I will ignore transactions with timestamps from the future
all timestamps are in ms

if there are no statistics return 204
     */
}