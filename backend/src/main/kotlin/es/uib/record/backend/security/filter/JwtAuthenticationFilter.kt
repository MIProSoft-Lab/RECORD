package es.uib.record.backend.security.filter

import es.uib.record.backend.security.open.JwtService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.servletPath
        if (path == "/auth/login" || path == "/auth/register" || path == "/auth/refresh") {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwtToken = authHeader.substringAfter("Bearer ")

        try {
            val userEmail = this.jwtService.extractEmail(jwtToken)

            if (userEmail != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(userEmail)

                if (this.jwtService.isTokenValid(jwtToken, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (e: ExpiredJwtException) {
            println("JWT token expired")
        } catch (e: JwtException) {
            println("JWT token invalid: ${e.message}")
        } catch (e: Exception) {
            println("Error processing JWT token: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}