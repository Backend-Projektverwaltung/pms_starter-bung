package de.szut.pms.employee;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class EmployeeClientConfig {

    @Bean
    public RestClient employeeRestClient(
            @Value("${employee-service.base-url}") String baseUrl) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {

                    Authentication authentication =
                            SecurityContextHolder.getContext().getAuthentication();

                    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                        request.getHeaders().setBearerAuth(
                                jwtAuth.getToken().getTokenValue()
                        );
                    }

                    return execution.execute(request, body);
                })
                .build();
    }
}