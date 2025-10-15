/*
 * Copyright (C) 2024 Rémi Venant
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package rtApi.configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 *
 * @author Rémi Venant
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableAsync
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Log LOG = LogFactory.getLog(WebSocketConfig.class);

    private final Optional<CorsConfiguration> corsConfiguration;

    private final ExternalBrokerConfigurationProperties externalBrokerConfigurationProperties;

    @Autowired
    public WebSocketConfig(Optional<CorsConfiguration> corsConfiguration,
            ExternalBrokerConfigurationProperties externalBrokerConfigurationProperties) {
        this.corsConfiguration = corsConfiguration;
        this.externalBrokerConfigurationProperties = externalBrokerConfigurationProperties;
    }

    @PostConstruct
    private void init() {
        LOG.info("WEB SOCKET CONFIGURATION");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        if (this.externalBrokerConfigurationProperties.getHost() == null || this.externalBrokerConfigurationProperties.getHost().isBlank()) {
            LOG.warn("CONFIGURING BROKER WITH Simple appliction Broker!");
            registry.enableSimpleBroker("/topic", "/queue"); //Topic will be use to broadcast message, queue to unicast
            registry.setPathMatcher(new AntPathMatcher("."));
        } else {
            LOG.info("CONFIGURING BROKER WITH External broker.");
            registry.setPathMatcher(new AntPathMatcher("."));
            registry.enableStompBrokerRelay("/topic", "/exchange")
                    .setRelayHost(this.externalBrokerConfigurationProperties.getHost())
                    .setRelayPort(this.externalBrokerConfigurationProperties.getPort())
                    .setSystemLogin(this.externalBrokerConfigurationProperties.getLogin())
                    .setSystemPasscode(this.externalBrokerConfigurationProperties.getPassword())
                    .setClientLogin(this.externalBrokerConfigurationProperties.getLogin())
                    .setClientPasscode(this.externalBrokerConfigurationProperties.getPassword());
        }
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        StompWebSocketEndpointRegistration reg = registry.addEndpoint("/api/websocket");
        this.configureAllowedOriginForRegistration(reg);
        reg.withSockJS();
    }

    private StompWebSocketEndpointRegistration configureAllowedOriginForRegistration(StompWebSocketEndpointRegistration reg) {
        if (this.corsConfiguration.isPresent()) {
            LOG.warn("CONFIGURE WEBSOCKET WITH CORS CONFIGURATION");
            final CorsConfiguration corsConfig = this.corsConfiguration.get();
            List<String> origins = corsConfig.getAllowedOrigins();
            if (origins != null) {
                String[] allowedOrigins = new String[origins.size()];
                allowedOrigins = origins.toArray(allowedOrigins);
                reg.setAllowedOrigins(allowedOrigins);
            }
            origins = corsConfig.getAllowedOriginPatterns();
            if (origins != null) {
                String[] allowedOrigins = new String[origins.size()];
                allowedOrigins = origins.toArray(allowedOrigins);
                reg.setAllowedOriginPatterns(allowedOrigins);
            }
        }
        return reg;
    }

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

}
