package com.example.minitrello.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

// Affiche les URLs utiles une fois l'application démarrée
@Component
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment environment;

    public StartupLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logUrls() {
        String port = environment.getProperty("local.server.port", "8080");
        String baseUrl = "http://localhost:" + port;
        log.info("""

                ----------------------------------------------------------
                  API     : {}/api
                  Scalar  : {}/scalar
                  Swagger : {}/swagger-ui.html
                ----------------------------------------------------------""",
                baseUrl, baseUrl, baseUrl);
    }
}
