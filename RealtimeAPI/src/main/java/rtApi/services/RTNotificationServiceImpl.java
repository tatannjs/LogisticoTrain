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
package rtApi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import rtApi.services.messages.RameRemoveRequest;

/**
 *
 * @author Rémi Venant
 */
@Service
@Validated
public class RTNotificationServiceImpl implements RTNotificationService {

    private final SimpMessagingTemplate msgTemplate;

    @Autowired
    public RTNotificationServiceImpl(SimpMessagingTemplate msgTemplate) {
        this.msgTemplate = msgTemplate;
    }

    @Override
    public void notifyRameRemoved(RameRemoveRequest request) {
        this.msgTemplate.convertAndSend("/topic/rameaccess", request);
    }

}
