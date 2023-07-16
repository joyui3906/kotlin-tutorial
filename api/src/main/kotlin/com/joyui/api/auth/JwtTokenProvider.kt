package com.joyui.api.auth

import com.joyui.api.auth.property.JwtProperty
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.util.ObjectUtils
import java.security.Key
import java.util.*
import javax.servlet.http.HttpServletRequest

@Component
class JwtTokenProvider(
    adminAccountService: AdminAccountService,
    jwtProperty: JwtProperty
) {
    private val userDetailsService: UserDetailsService
    private val key: Key
    private val expiryTimeMilliseconds: Long

    init {
        userDetailsService = adminAccountService
        key = Keys.hmacShaKeyFor(jwtProperty.secret.toByteArray())
        expiryTimeMilliseconds = jwtProperty.expiration
    }

    fun createToken(account: AdminAccount): String {
        val claims: Claims = Jwts.claims().setSubject(account.getEmail())
        claims.put("roles", account.getRoles())
        claims.put(JWT_LANGUAGE_FIELD, Locale.KOREA.toLanguageTag())
        claims.put(JWT_TIMEZONE_FIELD, "Asia/Seoul")
        val now = Date()
        return Jwts.builder()
            .setClaims(claims) // 데이터
            .setIssuedAt(now) // 토큰 발행일자
            .setExpiration(Date(now.time + expiryTimeMilliseconds))
            .signWith(key)
            .compact()
    }

    fun getAuthentication(token: String?): Authentication {
        val adminAccount: AdminAccount = userDetailsService.loadUserByUsername(getUsername(token)) as AdminAccount
        return UsernamePasswordAuthenticationToken(adminAccount, "", adminAccount.getAuthorities())
    }

    val guestAuthentication: Authentication
        get() = UsernamePasswordAuthenticationToken(null, null, null)

    fun getUsername(token: String?): String {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject()
    }

    fun resolveToken(request: HttpServletRequest): String? {
        val headerValue: String = request.getHeader(JWT_TOKEN_HEADER) ?: return null
        return if (headerValue.startsWith(JWT_TOKEN_HEADER_PREFIX)) {
            headerValue.substring(JWT_TOKEN_HEADER_PREFIX.length)
        } else null
    }

    fun validateToken(jwtToken: String?): Boolean {
        return if (ObjectUtils.isEmpty(jwtToken)) false else try {
            val claims: Jws<Claims> = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken)
            !claims.getBody().getExpiration().before(Date())
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val JWT_TOKEN_HEADER = "Authorization"
        const val JWT_TOKEN_HEADER_PREFIX = "Bearer "
        private const val JWT_LANGUAGE_FIELD = "lang"
        private const val JWT_TIMEZONE_FIELD = "gmt"
    }
}