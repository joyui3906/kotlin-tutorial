package com.joyui.api.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) : GenericFilterBean() {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val token = jwtTokenProvider.resolveToken(request as HttpServletRequest)
        if (jwtTokenProvider.validateToken(token)) {
            request.setAttribute("token", token)
            SecurityContextHolder.getContext().authentication = jwtTokenProvider.getAuthentication(token)
        } else {
            SecurityContextHolder.getContext().authentication = jwtTokenProvider.guestAuthentication
        }
        filterChain.doFilter(request, response)
    }
}