package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.service.AwardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AwardsController.class)
class AwardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AwardsService awardsService;

    @Test
    void giveDundieAwards_success_returnsOkAndBody() throws Exception {

        long organizationId = 1;
        int numAwards = 5;
        when(awardsService.giveAwards(organizationId)).thenReturn(numAwards);

        mockMvc.perform(post("/give-dundie-awards/{organizationId}", organizationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.organizationId").value((int) organizationId))
                .andExpect(jsonPath("$.awardsGiven", is(numAwards)));
    }

    @Test
    void giveDundieAwards_failure_returnsInternalServerError() throws Exception {
        long organizationId = 1L;
        when(awardsService.giveAwards(organizationId)).thenThrow(new RuntimeException("Exception"));

        mockMvc.perform(post("/give-dundie-awards/{organizationId}", organizationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
