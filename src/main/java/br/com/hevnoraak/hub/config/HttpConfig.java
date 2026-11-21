package br.com.hevnoraak.hub.config;

import java.time.Duration;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/** Timeouts curtos: fonte lenta vira 503 com "tentarNovamente", e o orquestrador refaz via job assíncrono. */
@Configuration
class HttpConfig {

    @Bean
    RestClientCustomizer timeouts() {
        return builder -> {
            var factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(3));
            factory.setReadTimeout(Duration.ofSeconds(10));
            builder.requestFactory(factory);
        };
    }
}
