/*-
 * #%L
 * Email Manager AppJars - Demo
 * %%
 * Copyright (C) 2023 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.emailmanager.demo;

import com.appjars.AppJarsAutoConfiguration;
import com.appjars.emailmanager.EmailManagerAutoConfiguration;
import com.appjars.emailmanager.demo.util.EmailGenerator;
import com.appjars.emailmanager.demo.views.MainLayout;
import com.appjars.emailmanager.flow.util.RouteConfigurer;
import com.appjars.emailmanager.service.EmailService;
import com.appjars.emailmanager.service.MailSenderService;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;

/**
 * The entry point of the Spring Boot application.
 *
 * <p>Use the @PWA annotation make the application installable on phones, tablets and some desktop
 * browsers.
 */
@SpringBootApplication
@ComponentScan(
        basePackageClasses = {EmailManagerAutoConfiguration.class, AppJarsAutoConfiguration.class})
@EnableVaadin({"com.appjars.emailmanager.flow", "com.appjars.emailmanager.demo"})
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css")
@PWA(
        name = "Email Manager Demo",
        shortName = "Email Manager Demo",
        offlineResources = {"images/logo.png"})
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    // By default, pending emails will be sent every 30 seconds
    private static final String DEFAULT_EMAILTASK_CRONEXP = "0/30 * * * * *";

    private final Logger applicationLogger = LoggerFactory.getLogger(this.getClass());

    private final Environment env;

    private final EmailService emailService;

    private final MailSenderService mailSenderTask;

    final RouteConfigurer routeConfigurer;

    public Application(Environment env, EmailService emailService, MailSenderService mailSenderTask, RouteConfigurer routeConfigurer) {
        this.env = env;
        this.emailService = emailService;
        this.mailSenderTask = mailSenderTask;
        this.routeConfigurer = routeConfigurer;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    private void init() {
        routeConfigurer.setViewsRouterLayout(MainLayout.class);

        // Create task scheduler
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.initialize();

        if (emailService.findAll().isEmpty()) {
            EmailGenerator.sampleEmails().forEach(emailService::save);
        }

        // Get the cron expression from application.properties and validate it
        // If it's null or invalid, use the default one
        String cronExpression = env.getProperty("email.task.cronexpression");
        if (cronExpression != null) {
            if (CronExpression.isValidExpression(cronExpression)) {
                taskScheduler.schedule(mailSenderTask, new CronTrigger(cronExpression));
                applicationLogger.info("Using configured Cron-Expression: {}", cronExpression);
            } else {
                taskScheduler.schedule(mailSenderTask, new CronTrigger(DEFAULT_EMAILTASK_CRONEXP));
                applicationLogger.error("Configured Cron-Expression is invalid. Using default instead.");
            }
        } else {
            taskScheduler.schedule(mailSenderTask, new CronTrigger(DEFAULT_EMAILTASK_CRONEXP));
            applicationLogger.info("Configured Cron-Expression is not set. Using default.");
        }
    }
}
