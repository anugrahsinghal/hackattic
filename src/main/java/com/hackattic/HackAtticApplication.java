package com.hackattic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.hackattic.problems.backup_restore.BackupRestoreProblem;
import com.hackattic.problems.jotting_jwt.JWTProblem;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.net.URL;
import java.util.Optional;

@Slf4j
@SpringBootApplication
public class HackAtticApplication implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    JWTProblem problem;

    public static void main(String[] args) {
        SpringApplication.run(HackAtticApplication.class, args);
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application is ready");
        problem.solveProblem();
    }
}