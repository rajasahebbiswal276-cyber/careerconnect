package com.careerconnect;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Starts the whole application with the in-memory H2 database and calls the real endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;

    private String registerBody(String email, String role) {
        return "{\"fullName\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"secret123\",\"role\":\"" + role + "\"}";
    }

    @Test
    void publicJobListIsAvailableWithoutLogin() throws Exception {
        mvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void protectedEndpointNeedsToken() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndReadProfile() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("flow@test.com", "CANDIDATE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"flow@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.token");

        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow@test.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));
    }

    @Test
    void candidateCannotOpenAdminEndpoints() throws Exception {
        String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("nonadmin@test.com", "CANDIDATE")))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(res, "$.token");

        mvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsAreReturnedAsJson() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"not-an-email\",\"password\":\"1\",\"role\":\"CANDIDATE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void nobodyCanSelfRegisterAsAdmin() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("sneaky@test.com", "ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void seededJobsAreSearchable() throws Exception {
        mvc.perform(get("/api/jobs").param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThan(0)));
    }
}
