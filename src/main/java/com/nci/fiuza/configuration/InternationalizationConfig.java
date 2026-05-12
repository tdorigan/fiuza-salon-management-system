package com.nci.fiuza.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

//code adapted from: Code Java - Spring Boot Internationalization (i18n) Examples (https://www.youtube.com/watch?v=jHh7mdmHdus)

//makes this class a spring configuration class for internationalization, spring scans it at initialization and register the bean methods
@Configuration
public class InternationalizationConfig implements WebMvcConfigurer { //implements WebMvcConfigurer so i can customize spring mvc behavior to register an interceptor to change language

    @Bean //spring bean of type LocaleResolver
    public LocaleResolver localeResolver() {
        //to store the chosen locale in the user HTTP session
        SessionLocaleResolver slr = new SessionLocaleResolver();
        //set the default language to english
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean //declaring a spring bean of type LocaleChangeInterceptor, it will run before the controller method executes
    public LocaleChangeInterceptor localeChangeInterceptor() {
        //creates the interceptor
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        //defines the parameter name used to change language
        lci.setParamName("lang");
        //preventing invalid parameter, in this case it used the default language
        lci.setIgnoreInvalidLocale(true);
        //returns the interceptor
        return lci;
    }

    //overrides the method from WebMvcConfigurer, it's called during startup
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registers the interceptor into spring mvc
        //for every request spring runs the interceptor before the controllers, if request contains "?lang=XXXX" it updates the locale in the session
        registry.addInterceptor(localeChangeInterceptor());
    }

}
