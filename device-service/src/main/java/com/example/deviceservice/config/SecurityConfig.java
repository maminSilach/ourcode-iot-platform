package com.example.deviceservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.client.client-id}")
    private String clientId;

    @Bean
    public SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .oauth2ResourceServer(it ->
                        it.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");

        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(getJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter));

        return jwtAuthenticationConverter;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> getJwtGrantedAuthoritiesConverter(JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) {
        return jwt -> {
            Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
            Map<String, Map<String, Object>> context = getContextFromResourceAccess(jwt);

            Collection<GrantedAuthority> extractRolesFromContext = extractRolesFromContext(context);

            return Stream
                    .concat(authorities.stream(), extractRolesFromContext.stream())
                    .toList();
        };
    }

    private Map<String, Map<String, Object>> getContextFromResourceAccess(Jwt jwt) {
        var resourceAccessClaim = jwt.getClaimAsMap("resource_access");
        var resourceAccessClaimOptional = Optional.ofNullable(resourceAccessClaim);
        var claim = resourceAccessClaimOptional.orElse(emptyMap());

        return Map.of(
                clientId,
                (Map<String, Object>) claim.getOrDefault(clientId, emptyMap())
        );
    }

    private Collection<GrantedAuthority> extractRolesFromContext(Map<String, Map<String, Object>> context) {
        return context.values()
                .stream()
                .map(x -> {
                            var rolesList = (Collection<String>) x.get("roles");
                            return Objects.requireNonNullElse(rolesList, Collections.<String>emptyList());
                        }
                )
                .flatMap(Collection::stream)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(
                        toSet()
                );
    }
}
