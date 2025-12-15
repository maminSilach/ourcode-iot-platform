package com.example.apiorchestrator.configuration;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
@RequiredArgsConstructor
public class FeignConfiguration {

    @Value("${security.oauth2.client.registration.keycloak.provider}")
    private final String providerKey;

    @Value("${security.oauth2.client.registration.keycloak.client-id}")
    private final String clientId;

    @Value("${security.oauth2.client.registration.keycloak.client-secret}")
    private final String clientSecret;

    @Value("${security.oauth2.client.provider.keycloak.token-uri}")
    private final String tokenUri;

    @Bean
    public Decoder decoder() {
        return new JacksonDecoder();
    }

    @Bean
    public Encoder encoder() {
        return new JacksonEncoder();
    }

    @Bean
    public OAuth2AccessTokenInterceptor defaultOAuth2AccessTokenInterceptor() {
        var registration = getClientRegistration();
        var clientRegistrationRepository = new InMemoryClientRegistrationRepository(registration);
        var clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        );

        return new OAuth2AccessTokenInterceptor(providerKey, clientManager);
    }

    private ClientRegistration getClientRegistration() {
        return ClientRegistration
                .withRegistrationId(providerKey)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tokenUri(tokenUri)
                .build();
    }
}