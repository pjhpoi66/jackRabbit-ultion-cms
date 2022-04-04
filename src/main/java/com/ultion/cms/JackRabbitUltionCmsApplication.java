package com.ultion.cms;

import com.ultion.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.time.LocalDateTime;

@SpringBootApplication
@ImportResource("classpath:application-context.xml")
@Slf4j
@RequiredArgsConstructor
public class JackRabbitUltionCmsApplication {

    private final UserRepository repository;
    static String buildDesc = "2022.02.04 - 박중호 : Ultion CMS project init";

    public static void main(String[] args) {



        SpringApplication.run(JackRabbitUltionCmsApplication.class, args);
        log.info("=========================================================");
        log.info("Build Instance : {}", JackRabbitUltionCmsApplication.class.getName());
        log.info("Build Time : {}", LocalDateTime.now());
        log.info("Build Description : {}", buildDesc);
        log.info("=========================================================");


    }

}
