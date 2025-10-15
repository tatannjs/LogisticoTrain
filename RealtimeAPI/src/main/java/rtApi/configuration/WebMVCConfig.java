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

import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author Rémi Venant
 */
@Configuration
@EnableWebMvc
public class WebMVCConfig implements WebMvcConfigurer {

    private static final Log LOG = LogFactory.getLog(WebMVCConfig.class);

    private final Optional<CorsConfiguration> corsConfiguration;

    @Autowired
    public WebMVCConfig(Optional<CorsConfiguration> corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (this.corsConfiguration.isPresent()) {
            LOG.warn("CONFIGURE REST WITH CORS CONFIGURATION");
            final CorsConfiguration corsConfig = this.corsConfiguration.get();
            CorsRegistration corsReg = registry.addMapping("/api/rest/**");

            if (corsConfig.getAllowedOrigins() != null) {
                corsReg.allowedOrigins(corsConfig.getAllowedOrigins()
                        .toArray(String[]::new));
            }
            if (corsConfig.getAllowedOriginPatterns() != null) {
                corsReg.allowedOriginPatterns(corsConfig.getAllowedOriginPatterns()
                        .toArray(String[]::new));
            }
            if (corsConfig.getAllowedMethods() != null) {
                corsReg.allowedMethods(corsConfig.getAllowedMethods()
                        .toArray(String[]::new));
            }
            if (corsConfig.getAllowedHeaders() != null) {
                corsReg.allowedHeaders(corsConfig.getAllowedHeaders()
                        .toArray(String[]::new));
            }
            if (corsConfig.getExposedHeaders() != null) {
                corsReg.exposedHeaders(corsConfig.getExposedHeaders()
                        .toArray(String[]::new));
            }
            if (corsConfig.getAllowCredentials()) {
                corsReg.allowCredentials(true);
            }
            if (corsConfig.getMaxAge() != null) {
                corsReg.maxAge(corsConfig.getMaxAge());
            }
        }
    }

}
