package ek.zhou.ekspider;

import ek.zhou.ekspider.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class EkspiderApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(EkspiderApplication.class, args);


    }

}
