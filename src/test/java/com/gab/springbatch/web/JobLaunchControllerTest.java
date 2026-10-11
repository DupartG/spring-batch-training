package com.gab.springbatch.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@SpringBatchTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobLaunchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JobRepositoryTestUtils jobRepositoryTestUtils;

    @AfterEach
    void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void launchesKnownJob() throws Exception {
        mockMvc.perform(post("/jobs/firstJob").param("source", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobName").value("firstJob"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.exitCode").value("COMPLETED"))
                .andExpect(jsonPath("$.executionId").isNumber());
    }

    @Test
    void returns404ForUnknownJob() throws Exception {
        mockMvc.perform(post("/jobs/doesNotExist"))
                .andExpect(status().isNotFound());
    }
}
