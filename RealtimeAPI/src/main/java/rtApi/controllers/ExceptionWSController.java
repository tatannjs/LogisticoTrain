/*
 * Copyright (C) 2023 Remi Venant.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package rtApi.controllers;

import jakarta.validation.ConstraintViolationException;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 *
 * @author Remi Venant
 */
@ControllerAdvice(annotations = {MessageMapping.class})
public class ExceptionWSController {

    private static final Log LOG = LogFactory.getLog(ExceptionWSController.class);

    private void logError(Throwable ex) {
        LOG.warn(ex.getClass().getName() + ": " + ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/exchange/amq.direct/errors", broadcast = false)
    public ErrorMessage handleAccessDenied(AccessDeniedException ex) {
        logError(ex);
        final String error = "Accès non autorisé";
        return createErrorMessage(error, ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/exchange/amq.direct/errors", broadcast = false)
    public ErrorMessage handleResourceNotFound(NoSuchElementException ex) {
        logError(ex);
        final String error = "Ressource introuvable";
        return createErrorMessage(error, ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/exchange/amq.direct/errors", broadcast = false)
    public ErrorMessage handleBadArgument(IllegalArgumentException ex) {
        logError(ex);
        final String error = "Requête invalide";
        return createErrorMessage(error, ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/exchange/amq.direct/errors", broadcast = false)
    public ErrorMessage handleDuplicateKey(DuplicateKeyException ex) {
        logError(ex);
        final String error = "Information déjà existante";
        return createErrorMessage(error, ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/exchange/amq.direct/errors", broadcast = false)
    public ErrorMessage handleConstraintViolationException(ConstraintViolationException ex) {
        logError(ex);
        final String error = "Requête invalide";
        return createErrorMessage(error, ex.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/exchange/amq.direct/errors", broadcast = false)
    public ErrorMessage handleOtherException(Throwable ex) {
        logError(ex);
        final String error = "Erreur non gérée : " + ex.getClass().getCanonicalName();
        return createErrorMessage(error, ex.getMessage());
    }

    private static ErrorMessage createErrorMessage(String error, String message) {
        return new ErrorMessage(ZonedDateTime.now(), error, message);
    }

    public static class ErrorMessage {

        private ZonedDateTime timestamp;
        private String error;
        private String message;

        public ErrorMessage() {
        }

        public ErrorMessage(ZonedDateTime timestamp, String error, String message) {
            this.timestamp = timestamp;
            this.error = error;
            this.message = message;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
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

    }
}
