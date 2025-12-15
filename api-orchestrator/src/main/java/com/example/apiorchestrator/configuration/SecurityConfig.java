package com.example.apiorchestrator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.client.client-id}")
    private String clientId;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/swagger-ui.html").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
                    JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
                    Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);
                    Map<String, Map<String, Object>> context = getContextFromResourceAccess(jwt);
                    Collection<GrantedAuthority> rolesFromContext = extractRolesFromContext(context);

                    return Stream.concat(authorities.stream(), rolesFromContext.stream())
                            .collect(toSet());
                }
        );

        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
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
                .collect(toSet());
    }
}