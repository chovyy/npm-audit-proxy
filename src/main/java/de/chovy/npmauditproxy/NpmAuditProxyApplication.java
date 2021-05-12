package de.chovy.npmauditproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NpmAuditProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(NpmAuditProxyApplication.class, args);
	}

	/**
	 * Configures the forwarding.
	 * 
	 * @see 
	 * <a href="https://spring.io/projects/spring-cloud-gateway">
	 *   https://spring.io/projects/spring-cloud-gateway
	 * </a>
	 */
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("npm_audit", r -> r.path("/-/npm/v1/security/audits/**")
				.uri("https://registry.npmjs.org/-/npm/v1/security/audits"))
				.build();
	}
}
