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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rtApi.services.RTNotificationService;
import rtApi.services.RameAccessService;
import rtApi.services.messages.RameRemoveRequest;

/**
 *
 * @author Rémi Venant
 */
@RestController
@RequestMapping("/api/rest/rames")
public class RameAccessRestController {

    private final RameAccessService rameAccessService;

    private final RTNotificationService rtNotifSvc;

    @Autowired
    public RameAccessRestController(RameAccessService rameAccessService, RTNotificationService rtNotificationService) {
        this.rameAccessService = rameAccessService;
        this.rtNotifSvc = rtNotificationService;
    }

    @PutMapping("remove-order")
    public ResponseEntity<Void> removeRame(@RequestBody RameRemoveRequest removeRequest) {
        if (removeRequest == null) {
            throw new IllegalArgumentException("Missing remove request");
        }
        this.rameAccessService.removeRame(removeRequest);
        this.rtNotifSvc.notifyRameRemoved(removeRequest);

        return ResponseEntity.noContent().build();
    }
}
