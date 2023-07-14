package com.joyui.api.config

import com.joyui.api.auth.CustomAccessDeniedHandler
import com.joyui.api.auth.JwtAuthenticationFilter
import com.joyui.api.auth.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(jwtTokenProvider: JwtTokenProvider) : WebSecurityConfigurerAdapter() {
    private val jwtTokenProvider: JwtTokenProvider

    init {
        this.jwtTokenProvider = jwtTokenProvider
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors()
            .and().httpBasic().disable()
            .csrf().disable()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
            .and()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.POST, "/api/v1/login").permitAll() // 로그인
            .mvcMatchers(HttpMethod.POST, "/api/v1/auth").permitAll() // 가입
            .mvcMatchers(HttpMethod.GET, "/api/v1/code/**").permitAll() // 코드
            .mvcMatchers(HttpMethod.POST, "/api/v1/**-sheet").permitAll() // 시트 import
            //                .mvcMatchers(HttpMethod.GET, "/password-validate").permitAll() // 비밀번호 유효성 검사
            //                .mvcMatchers(HttpMethod.GET, "/health").permitAll() // health check
            //                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
            .anyRequest().hasRole("ADMIN")
            .and()
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(
            "/v2/api-docs",
            "/swagger-resources/**",
            "/",
            "/swagger-ui/index.html",
            "/webjars/**",
            "/swagger-ui/**"
        )
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler {
        return CustomAccessDeniedHandler()
    }
}