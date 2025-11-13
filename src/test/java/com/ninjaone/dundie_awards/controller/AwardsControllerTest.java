package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.GiveAwardsResponse;
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
        when(awardsService.giveAwards(7L)).thenReturn(3);

        mockMvc.perform(post("/give-dundie-awards/{organizationId}", 7L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.organizationId", is(7)))
                .andExpect(jsonPath("$.awardsGiven", is(3)));
    }

    @Test
    void giveDundieAwards_failure_returnsInternalServerError() throws Exception {
        when(awardsService.giveAwards(7L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/give-dundie-awards/{organizationId}", 7L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
