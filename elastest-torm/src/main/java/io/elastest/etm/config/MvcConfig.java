/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.etm.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${registry.contextPath}")
    private String registryContextPath;

    @Value("${et.shared.folder}")
    private String etSharedFolder;

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler(registryContextPath + "/**")
                .addResourceLocations("file:" + etSharedFolder);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedMethods("*").allowedOrigins("*");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureContentNegotiation(
            ContentNegotiationConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureDefaultServletHandling(
            DefaultServletHandlerConfigurer configurer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addReturnValueHandlers(
            List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        // TODO Auto-generated method stub

    }

    @Override
    public void extendMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureHandlerExceptionResolvers(
            List<HandlerExceptionResolver> exceptionResolvers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void extendHandlerExceptionResolvers(
            List<HandlerExceptionResolver> exceptionResolvers) {
        // TODO Auto-generated method stub

    }

    @Override
    public Validator getValidator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        // TODO Auto-generated method stub
        return null;
    }

}