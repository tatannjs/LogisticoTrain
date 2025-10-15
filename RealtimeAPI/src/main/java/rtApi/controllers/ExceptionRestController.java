/*
 * Copyright (C) 2024 Rémi Venant
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package rtApi.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Rémi Venant
 */
@ControllerAdvice(annotations = {RestController.class})
public class ExceptionRestController {

    private static final Log LOG = LogFactory.getLog(ExceptionWSController.class);

    private void logError(Throwable ex) {
        LOG.warn(ex.getClass().getName() + ": " + ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleAccessDenied(HttpServletRequest request, AccessDeniedException ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.UNAUTHORIZED;
        final String error = "Accès non autorisé";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleResourceNotFound(HttpServletRequest request, NoSuchElementException ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.NOT_FOUND;
        final String error = "Ressource introuvable";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleBadArgument(HttpServletRequest request, IllegalArgumentException ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final String error = "Requête invalide";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleDuplicateKey(HttpServletRequest request, DuplicateKeyException ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.CONFLICT;
        final String error = "Information déjà existante";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final String error = "Requête invalide";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMediaTypeNotSupported(HttpServletRequest request, HttpMediaTypeNotSupportedException ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        final String error = "Requête invalide";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMethodUnsupported(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        final String error = "Méthode non supportée.";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(Throwable.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleOtherException(HttpServletRequest request, Throwable ex) {
        logError(ex);
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        final String error = "Erreur non gérée : " + ex.getClass().getCanonicalName();
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    private static ErrorMessage createErrorMessage(HttpStatus status, String error, String message, HttpServletRequest request) {
        return new ErrorMessage(ZonedDateTime.now(), status.value(), error, message, request.getServletPath());
    }

    public static class ErrorMessage {

        private ZonedDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public ErrorMessage() {
        }

        public ErrorMessage(ZonedDateTime timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
