package com.joyui.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*
import java.util.Set

@Configuration
@EnableSwagger2 //@Profile({"!prd"})

class SwaggerConfig : WebMvcConfigurer {
    @Value("\${swagger.domain}")
    private val swaggerDomain: String? = null

    @Value("\${swagger.url}")
    private val swaggerUrl: String? = null
    @Bean
    fun docket(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .securityContexts(securityContextList)
            .securitySchemes(securitySchemeList)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.joyui"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo)
            .produces(Set.of("application/json"))
            .host(swaggerDomain)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false)
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "$swaggerUrl/swagger-ui/index.html")
    }

    private val securityContextList: List<SecurityContext>
        private get() {
            val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
            authorizationScopes[0] = AuthorizationScope("global", "Bearer Token")
            val securityReference =
                SecurityReference.builder().reference("Authorization").scopes(authorizationScopes).build()
            val securityContext = SecurityContext.builder().securityReferences(Arrays.asList(securityReference)).build()
            return Arrays.asList(securityContext)
        }
    private val securitySchemeList: List<SecurityScheme>
        private get() = Arrays.asList<SecurityScheme>(ApiKey("Authorization", "Authorization", "header"))
    private val apiInfo: ApiInfo
        private get() {
            val description = "<h1>REST API for GIGPL Admin</h1>" +
                    "<h3>인증방법</h3>" +
                    "<p> - 로그인 API로 토큰 발급</p>" +
                    "<p> - Authorize 버튼 클릭</p>" +
                    "<p> - <b>Bearer [발급받은 토큰]</b> 입력 후 Authorize 버튼 클릭 (예) Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxM... </p>" +
                    "<p> - 이후 Swagger가 모든 요청의 Request Header에 자동으로 인증을 위한 토큰을 셋팅합니다. </p>" +
                    "<img src=\"https://storage.googleapis.com/gigpl_dev/guide/swagger_login_guide.gif\" style=\"width: 500px;\">"
            return ApiInfo(
                "GIGPL Admin API DOCUMENT",
                description,
                "1.0.0",
                "https://gigplanner.com",
                Contact("contact", "https://gigplanner.kr", "contact@gigplanner.kr"),
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0", emptyList()
            )
        }
}