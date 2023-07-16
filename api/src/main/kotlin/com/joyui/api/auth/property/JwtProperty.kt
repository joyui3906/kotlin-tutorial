package com.joyui.api.auth.property

import lombok.Getter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Getter
@Configuration
class JwtProperty {
    @Value("\${jwt.secret}")
    val secret: String = ""

    @Value("\${jwt.expiration}")
    val expiration: Long = 0
}