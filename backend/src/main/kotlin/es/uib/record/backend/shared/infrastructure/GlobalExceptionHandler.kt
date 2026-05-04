package es.uib.record.backend.shared.infrastructure

import es.uib.record.backend.model.ErrorResponse
import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ResponseEntity<ErrorResponse> {
        val httpStatus = when (e.type) {
            ErrorType.NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorType.CONFLICT -> HttpStatus.CONFLICT
            ErrorType.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
        }

        val response = ErrorResponse(
            message = e.message,
            code = e.code,
            params = e.params.ifEmpty { null }
        )

        return ResponseEntity.status(httpStatus).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            message = "Validation Failed: ${e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }}",
            code = "",
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            message = "Invalid email or password",
            code = ""
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }
}