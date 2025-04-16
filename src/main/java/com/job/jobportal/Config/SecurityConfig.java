package com.job.jobportal.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        @Autowired
        private OAuth2SuccessHandler oauthhandler;
        @Autowired
        private JwtFilter jwtFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> {
                                        auth.requestMatchers("/", "/error", "/actuator/health").permitAll();
                                        auth.requestMatchers("/jobseeker/auth/**").permitAll();
                                        auth.requestMatchers("/oauth2/**").permitAll();
                                        auth.requestMatchers("/login/oauth2/**").permitAll();
                                        auth.requestMatchers("/auth/google/url").permitAll();
                                        auth.requestMatchers("/jobprovider/**").hasAuthority("JOBPROVIDER");
                                        auth.requestMatchers("/jobseeker/**").hasAnyAuthority("JOBSEEKER",
                                                        "JOBPROVIDER");
                                        auth.requestMatchers("/dashboard", "/provider-dashboard").authenticated();
                                        auth.anyRequest().authenticated();
                                })
                                .oauth2Login(oauth -> oauth
                                                .successHandler(oauthhandler)
                                                .authorizationEndpoint(authorization -> authorization
                                                                .baseUri("/oauth2/authorization"))
                                                .redirectionEndpoint(redirection -> redirection
                                                                .baseUri("/login/oauth2/code/*")))
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID", "AUTH_TOKEN"))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(
                                "https://job-app-frontend-q5zd.vercel.app",
                                "https://job-app-frontend-q5zd.vercel.app",
                                "https://accounts.google.com"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Requested-With",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers"));
                configuration.setExposedHeaders(List.of(
                                "Authorization",
                                "Content-Disposition"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}