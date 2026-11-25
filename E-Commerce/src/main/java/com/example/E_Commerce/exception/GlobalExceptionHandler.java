package com.example.E_Commerce.exception;

import com.example.E_Commerce.dto.ErrorApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para devolver respuestas JSON consistentes sin exponer stacktraces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApiResponse> manejarValidacionArgumentos(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fieldError.getField(), mensajeOValorPorDefecto(fieldError.getDefaultMessage()));
        }
        for (ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            errores.putIfAbsent(objectError.getObjectName(), mensajeOValorPorDefecto(objectError.getDefaultMessage()));
        }
        return responderConValidaciones(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene errores de validación.",
                request,
                errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorApiResponse> manejarViolacionRestricciones(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String rutaPropiedad = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "";
            int ultimoPunto = rutaPropiedad.lastIndexOf('.');
            String campo = ultimoPunto >= 0 && ultimoPunto < rutaPropiedad.length() - 1
                    ? rutaPropiedad.substring(ultimoPunto + 1)
                    : (rutaPropiedad.isBlank() ? "parametro" : rutaPropiedad);
            errores.putIfAbsent(campo, mensajeOValorPorDefecto(violation.getMessage()));
        }
        return responderConValidaciones(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene parámetros inválidos.",
                request,
                errores);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorApiResponse> manejarValidacionMetodo(
            HandlerMethodValidationException ex,
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            String parametro = result.getMethodParameter().getParameterName();
            String campo = parametro != null && !parametro.isBlank() ? parametro : "parametro";
            String mensaje = result.getResolvableErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .filter(m -> m != null && !m.isBlank())
                    .findFirst()
                    .orElse("Valor inválido.");
            errores.putIfAbsent(campo, mensaje);
        }
        return responderConValidaciones(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene parámetros inválidos.",
                request,
                errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApiResponse> manejarJsonIlegible(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud JSON es inválido o está mal formado.",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApiResponse> manejarTipoParametroInvalido(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un formato inválido.",
                request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorApiResponse> manejarParametroFaltante(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getParameterName() + "' es obligatorio.",
                request);
    }

    @ExceptionHandler({ ReglaNegocioException.class, IllegalArgumentException.class })
    public ResponseEntity<ErrorApiResponse> manejarReglaNegocio(
            RuntimeException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, mensajeSeguro(ex.getMessage(), "Solicitud inválida."), request);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorApiResponse> manejarCredencialesInvalidas(
            CredencialesInvalidasException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, mensajeSeguro(ex.getMessage(), "Credenciales inválidas."), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorApiResponse> manejarAutenticacion(
            AuthenticationException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "Credenciales inválidas o autenticación requerida.", request);
    }

    @ExceptionHandler(AccesoDenegadoNegocioException.class)
    public ResponseEntity<ErrorApiResponse> manejarAccesoDenegadoNegocio(
            AccesoDenegadoNegocioException ex,
            HttpServletRequest request) {
        return responder(
                HttpStatus.FORBIDDEN,
                mensajeSeguro(ex.getMessage(), "No tienes permisos para realizar esta acción."),
                request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorApiResponse> manejarAccesoDenegado(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.", request);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorApiResponse> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {
        return responder(
                HttpStatus.NOT_FOUND,
                mensajeSeguro(ex.getMessage(), "El recurso solicitado no existe."),
                request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorApiResponse> manejarRutaNoEncontrada(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "El recurso solicitado no existe.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorApiResponse> manejarMetodoNoSoportado(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return responder(
                HttpStatus.METHOD_NOT_ALLOWED,
                "El método HTTP " + ex.getMethod() + " no está permitido para esta ruta.",
                request);
    }

    @ExceptionHandler(ConflictoNegocioException.class)
    public ResponseEntity<ErrorApiResponse> manejarConflictoNegocio(
            ConflictoNegocioException ex,
            HttpServletRequest request) {
        return responder(
                HttpStatus.CONFLICT,
                mensajeSeguro(ex.getMessage(), "La operación genera un conflicto con el estado actual."),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> manejarErrorInesperado(
            Exception ex,
            HttpServletRequest request) {
        logger.error("Error interno no controlado en {}", request.getRequestURI(), ex);
        return responder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor.",
                request);
    }

    private ResponseEntity<ErrorApiResponse> responder(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request) {
        ErrorApiResponse body = ErrorApiResponse.of(
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<ErrorApiResponse> responderConValidaciones(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request,
            Map<String, String> erroresValidacion) {
        ErrorApiResponse body = ErrorApiResponse.conValidaciones(
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI(),
                erroresValidacion);
        return ResponseEntity.status(status).body(body);
    }

    private String mensajeOValorPorDefecto(String mensaje) {
        return mensaje != null && !mensaje.isBlank() ? mensaje : "Valor inválido.";
    }

    private String mensajeSeguro(String mensaje, String respaldo) {
        return mensaje != null && !mensaje.isBlank() ? mensaje : respaldo;
    }
}
