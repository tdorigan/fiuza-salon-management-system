package com.nci.fiuza.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}") //reads app.upload.dir, or uses "uploads" by default
    private String uploadDir; //stores the base upload folder path

    //overrides the spring mvc method used to add custom resource handlers
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //converts "uploads" into an absolute file url, so spring can serve files from it
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        //tells spring that browser requests starting with /uploads/ should be handled here
        registry.addResourceHandler("/uploads/**")

                //tells spring where the actual files are stored on the server
                .addResourceLocations(uploadPath);

    }

}
