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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import rtApi.model.Rame;
import rtApi.services.RameAccessService;
import rtApi.services.messages.RameEntranceAnswer;
import rtApi.services.messages.RameEntranceRequest;

/**
 *
 * @author Rémi Venant
 */
@Controller
@MessageMapping("rameaccess")
public class RameAccessWSController {

    private static final Log LOG = LogFactory.getLog(RameAccessWSController.class);
    private final RameAccessService rameAccessSvc;

    @Autowired
    public RameAccessWSController(RameAccessService rameAccessSvc) {
        this.rameAccessSvc = rameAccessSvc;
    }

    @MessageMapping
    public RameEntranceRequest handleRequestMessage(@Payload RameEntranceRequest message) {
        if (message == null) {
            LOG.warn("Received null message on RameAccessWSController:request");
            throw new IllegalArgumentException("Missing message");
        }
        LOG.info("Receive request for rame " + message.getNumSerie());
        this.rameAccessSvc.askRameEntrance(message);
        return message;
        //this.realtimeNotifSvc.notifyRameEntranceRequest(message);
    }

    @MessageMapping("{numSerie:[a-zA-Z0-9]{1,12}}")
    public RameEntranceAnswer handleAnswerMessage(@DestinationVariable String numSerie, @Payload RameEntranceAnswer message) {
        if (message == null) {
            LOG.warn("Received null message on RameAccessWSController:answer");
            throw new IllegalArgumentException("Missing message");
        }
        if (!numSerie.equals(message.getNumSerie())) {
            LOG.warn("Mismatch numSerie path component  with message.numSerie");
            throw new IllegalArgumentException("Mismatch numSerie path component  with message.numSerie");
        }
        LOG.info("Receive answer for rame " + numSerie);
        Rame rame = this.rameAccessSvc.answerRameEntranceRequest(message);
        return message;
        //this.realtimeNotifSvc.notifyRameEntranceAnswer(rame.getConducteurEntrant(), message);
    }
}
