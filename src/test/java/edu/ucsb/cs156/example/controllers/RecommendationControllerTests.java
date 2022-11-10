package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationController.class)
@Import(TestConfig.class)
public class RecommendationControllerTests extends ControllerTestCase {
    
    @MockBean
    RecommendationRepository recommendRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/ucsbdates/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/Recommendation/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/Recommendation/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/Recommendation?id=7"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    // Authorization tests for /api/ucsbdates/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/Recommendation/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/Recommendation/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    // // Tests with mocks for database actions

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

            // arrange
            LocalDateTime request_date = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime needed_date = LocalDateTime.parse("2022-05-01T00:00:00");

            Recommendation recommendation = Recommendation.builder()
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("BS/MS program")
                            .dateRequested(request_date)
                            .dateNeeded(needed_date)
                            .done(false)
                            .build();

            when(recommendRepository.findById(eq(7L))).thenReturn(Optional.of(recommendation));

            // act
            MvcResult response = mockMvc.perform(get("/api/Recommendation?id=7"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(recommendRepository, times(1)).findById(eq(7L));
            String expectedJson = mapper.writeValueAsString(recommendation);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(recommendRepository.findById(eq(7L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/Recommendation?id=7"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(recommendRepository, times(1)).findById(eq(7L));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("Recommendation with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_recommendation() throws Exception {

            // arrange
            LocalDateTime request_date = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime needed_date = LocalDateTime.parse("2022-05-01T00:00:00");

            Recommendation recommend1 = Recommendation.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(request_date)
                                .dateNeeded(needed_date)
                                .done(false)
                                .build();

            LocalDateTime request_date2 = LocalDateTime.parse("2022-05-20T00:00:00");
            LocalDateTime needed_date2 = LocalDateTime.parse("2022-11-15T00:00:00");

            Recommendation recommend2 = Recommendation.builder()
                                .requesterEmail("ldelplaya@ucsb.edu")
                                .professorEmail("richert@ucsb.edu")
                                .explanation("PhD CS Stanford")
                                .dateRequested(request_date2)
                                .dateNeeded(needed_date2)
                                .done(false)
                                .build();

            ArrayList<Recommendation> expectedDates = new ArrayList<>();
            expectedDates.addAll(Arrays.asList(recommend1, recommend2));

            when(recommendRepository.findAll()).thenReturn(expectedDates);

            // act
            MvcResult response = mockMvc.perform(get("/api/Recommendation/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(recommendRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedDates);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendation() throws Exception {
            // arrange

            LocalDateTime request_date = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime needed_date = LocalDateTime.parse("2022-05-01T00:00:00");

            Recommendation recommend1 = Recommendation.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(request_date)
                                .dateNeeded(needed_date)
                                .done(true)
                                .build();

            when(recommendRepository.save(eq(recommend1))).thenReturn(recommend1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/Recommendation/post?requesterEmail=cgaucho@ucsb.edu&professorEmail=phtcon@ucsb.edu&explanation=BS/MS program&dateRequested=2022-04-20T00:00:00&dateNeeded=2022-05-01T00:00:00&done=true")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(recommendRepository, times(1)).save(recommend1);
            String expectedJson = mapper.writeValueAsString(recommend1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_recommendation() throws Exception {
            // arrange

            LocalDateTime request_date = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime needed_date = LocalDateTime.parse("2022-05-01T00:00:00");

            Recommendation recommend1 = Recommendation.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(request_date)
                                .dateNeeded(needed_date)
                                .done(true)
                                .build();

            when(recommendRepository.findById(eq(15L))).thenReturn(Optional.of(recommend1));

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/Recommendation?id=15")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(recommendRepository, times(1)).findById(15L);
            verify(recommendRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("Recommendation with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                    throws Exception {
            // arrange

            when(recommendRepository.findById(eq(15L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/Recommendation?id=15")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(recommendRepository, times(1)).findById(15L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("Recommendation with id 15 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_ucsbdate() throws Exception {
            // arrange

            LocalDateTime request_date = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime needed_date = LocalDateTime.parse("2022-05-01T00:00:00");

            Recommendation recommendOrigin = Recommendation.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(request_date)
                                .dateNeeded(needed_date)
                                .done(false)
                                .build();

            LocalDateTime request_date2 = LocalDateTime.parse("2022-05-20T00:00:00");
            LocalDateTime needed_date2 = LocalDateTime.parse("2022-11-15T00:00:00");

            Recommendation recommendEdited = Recommendation.builder()
                                .requesterEmail("ldelplaya@ucsb.edu")
                                .professorEmail("richert@ucsb.edu")
                                .explanation("PhD CS Stanford")
                                .dateRequested(request_date2)
                                .dateNeeded(needed_date2)
                                .done(true)
                                .build();

            String requestBody = mapper.writeValueAsString(recommendEdited);

            when(recommendRepository.findById(eq(67L))).thenReturn(Optional.of(recommendOrigin));

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/Recommendation?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(recommendRepository, times(1)).findById(67L);
            verify(recommendRepository, times(1)).save(recommendEdited); // should be saved with correct user
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
            // arrange

            LocalDateTime request_date = LocalDateTime.parse("2022-04-20T00:00:00");
            LocalDateTime needed_date = LocalDateTime.parse("2022-05-01T00:00:00");

            Recommendation recommendEdited = Recommendation.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("BS/MS program")
                                .dateRequested(request_date)
                                .dateNeeded(needed_date)
                                .done(false)
                                .build();

            String requestBody = mapper.writeValueAsString(recommendEdited);

            when(recommendRepository.findById(eq(67L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/Recommendation?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(recommendRepository, times(1)).findById(67L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("Recommendation with id 67 not found", json.get("message"));

    }
}
