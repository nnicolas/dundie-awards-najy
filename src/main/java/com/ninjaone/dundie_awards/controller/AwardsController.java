package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.GiveAwardsResponse;
import com.ninjaone.dundie_awards.service.AwardsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping()
public class AwardsController {


    private final AwardsService awardsService;

    public AwardsController(AwardsService awardsService) {
        this.awardsService = awardsService;
    }

    @PostMapping(value = "/give-dundie-awards/{organizationId}" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GiveAwardsResponse> giveDundieAwards(@PathVariable long organizationId) {
        try {
            int numAwards = awardsService.giveAwards(organizationId);
            Logger.getGlobal().log(Level.INFO, String.format("Awards submitted for organizationId: %d", organizationId));
            return ResponseEntity.ok(new GiveAwardsResponse(organizationId, numAwards));
        } catch (Exception ex) {
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to submit awards for organizationId: %d", organizationId), ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
