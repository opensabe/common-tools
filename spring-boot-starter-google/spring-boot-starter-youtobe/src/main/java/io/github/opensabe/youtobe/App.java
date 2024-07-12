package io.github.opensabe.youtobe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 这个类不需要启动，这个仅是给单测使用，为了IOC容器能够自动装配
 */
@SpringBootApplication(scanBasePackages = "io.github.opensabe.youtobe")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
