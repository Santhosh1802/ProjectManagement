package com.Santhosh1802.ProjectManagement.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Value("${server.tomcat.basedir:target/tomcat}")
    private String tomcatBaseDir;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            File baseDir = new File(tomcatBaseDir);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            factory.setBaseDirectory(baseDir);

            File docBase = new File(baseDir, "docbase");
            if (!docBase.exists()) {
                docBase.mkdirs();
            }
            factory.setDocumentRoot(docBase);
        };
    }
}
