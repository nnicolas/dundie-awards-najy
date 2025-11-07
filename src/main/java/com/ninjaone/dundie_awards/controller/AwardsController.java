package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.GiveAwardsResponse;
import com.ninjaone.dundie_awards.service.AwardsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping()
public class AwardsController {


    private final AwardsService awardsService;

    public AwardsController(AwardsService awardsService) {
        this.awardsService = awardsService;
    }

    @PostMapping("/give-dundie-awards/{organizationId}")
    @ResponseBody
    public ResponseEntity<GiveAwardsResponse> giveDundieAwards(@PathVariable long organizationId) {
        try {
            int numAwards = awardsService.giveAwards(organizationId);
            Logger.getGlobal().log(Level.INFO, String.format("Awards submitted for organizationId: %d", organizationId));
            return ResponseEntity.ok(new GiveAwardsResponse(organizationId, numAwards));
        } catch (Exception ex) {
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to submit Awards for organizationId: %d", organizationId));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
