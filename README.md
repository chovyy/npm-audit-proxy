# npm-audit-proxy

Proxy for the [`npm audit`](https://docs.npmjs.com/cli/v7/commands/npm-audit) API on https://registry.npmjs.org/. Web application that redirects all calls to `/-/npm/v1/security/audits` to https://registry.npmjs.org/-/npm/v1/security/audits.

This is particular useful in cases where you have a private proxy registry for npm that does not support `npm audit`, such as [Nexus Repository OSS](https://de.sonatype.com/products/repository-oss). 

[Nexus Repository OSS only supports `npm audit` in conjuction with the paid services Nexus Firewall or Nexus Lifecycle.](https://blog.sonatype.com/new-in-nexus-repository-3.23-nexus-intelligence-via-npm-audit). 
Alternatively, [you can call `npm audit --registry=https://registry.npmjs.org` to get your audit reports directly from registry.npmjs.org](https://stackoverflow.com/questions/57427279/how-to-configure-nexus-repository-manager-to-support-npm-audit), but that only works if your npm client has direct internet access.
If this is not the case for all your npm clients, **npm-audit-proxy** provides a solution.

## Set-up

![grafik](https://user-images.githubusercontent.com/2318123/118049711-72e82600-b37e-11eb-9111-de63ea7b6ac2.png)

Typically, you have your private registry behind some sort of gateway, [for example an Apache HTTP Server with Reverse Proxy configuration](https://help.sonatype.com/repomanager3/installation/run-behind-a-reverse-proxy). Just let your gateway pass all calls `/-/npm/v1/security/audits/**` to **npm-audit-proxy** instead of your private registry. **npm-audit-proxy**  will redirect it to https://registry.npmjs.org/-/npm/v1/security/audits/... 

You can try to pass all calls to `/-/npm/v1/security/audits/**` directly to https://registry.npmjs.org/-/npm/v1/security/audits/... from your gateway, but my tests with a `ProxyPass    /-/npm/v1/security/audits   https://registry.npmjs.org/-/npm/v1/security/audits` in my Apache configuration ended up with a 403 error, so I created **npm-audit-proxy** as another redirection.

## Technology

**npm-audit-proxy** is build in Java and based on [Spring Boot](https://spring.io/projects/spring-boot) and [Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html/). It just consists of one simple starter and configuration class setting up the route: [NpmAuditProxyApplication.java](src/main/java/de/chovy/npmauditproxy/NpmAuditProxyApplication.java)

```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
            .route("npm_audit", r -> r.path("/-/npm/v1/security/audits/**")
            .uri("https://registry.npmjs.org/-/npm/v1/security/audits"))
            .build();
}
```

## Build

Build **npm-audit-proxy** with Maven:

```bash
mvn clean package
```
## Installation

Please refer to https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html on how to install Spring Boot applications. The file [systemd/npm-audit-proxy.service](systemd/npm-audit-proxy.service) might be helpful if you choose to start **npm-audit-proxy** by systemd.

If you have a Nexus Repository OSS running [behind an Apache HTTP Server as reverse proxy](https://help.sonatype.com/repomanager3/installation/run-behind-a-reverse-proxy), add the following line to your VirtualHost configuration:

```
ProxyPass    /repository/npm/-/npm/v1/security/audits    http://localhost:8082/-/npm/v1/security/audits    ttl=120
```

This assumes that there is a proxy repository to http://registry.npmjs.org/ named "npm" configured in Nexus. Otherwise, change the base path `/repository/npm/` to the actual path of your npm proxy repository in Nexus.
Also, it assumes, that **npm-audit-proxy** is running on port 8082 on the same machine as Apache httpd. Otherwise, change `localhost:8082` to the actual host name and port.

The port on which **npm-audit-proxy** listens can be configured in the [application.properties](/src/main/resources/application.properties) file or passed as command line argument when starting **npm-audit-proxy**: `--server.port=<port>`. The default is 8082. 
