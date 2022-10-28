//Implemented by Saahil Joshi

package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {

    @MockBean
    UCSBOrganizationsRepository ucsbOrganizationsRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/ucsborganizations/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations?id=7"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    // Authorization tests for /api/ucsborganizations/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    // // Tests with mocks for database actions

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

            // arrange
            UCSBOrganizations ucsbOrg = UCSBOrganizations.builder()
                            .orgCode("THT")
                            .orgTranslationShort("Th Ta")
                            .orgTranslation("Theta Tau")
                            .inactive(false)
                            .build();

            when(ucsbOrganizationsRepository.findById("THT")).thenReturn(Optional.of(ucsbOrg));

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations?id=7"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(ucsbOrganizationsRepository, times(1)).findById("THT");
            String expectedJson = mapper.writeValueAsString(ucsbOrg);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(ucsbOrganizationsRepository.findById("LBJ")).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations?id=LBJ"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(ucsbOrganizationsRepository, times(1)).findById("LBJ");
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("UCSBOrganization with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

            // arrange
            UCSBOrganizations ucsbOrg1 = UCSBOrganizations.builder()
                            .orgCode("LBJ")
                            .orgTranslationShort("Le Br Ja")
                            .orgTranslation("LeBron James")
                            .inactive(false)
                            .build();

            UCSBOrganizations ucsbOrg2 = UCSBOrganizations.builder()
                            .orgCode("STC")
                            .orgTranslationShort("Ste Cur")
                            .orgTranslation("Stephen Curry")
                            .inactive(false)
                            .build();

            ArrayList<UCSBOrganizations> expectedOrgs = new ArrayList<>();
            expectedOrgs.addAll(Arrays.asList(ucsbOrg1, ucsbOrg2));

            when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedOrgs);

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(ucsbOrganizationsRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedOrgs);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsborganization() throws Exception {
            // arrange
            UCSBOrganizations ucsbOrg1 = UCSBOrganizations.builder()
                            .orgCode("LBJ")
                            .orgTranslationShort("LeBrJa")
                            .orgTranslation("LeBronJames")
                            .inactive(false)
                            .build();

            when(ucsbOrganizationsRepository.save(eq(ucsbOrg1))).thenReturn(ucsbOrg1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/ucsborganizations/post?orgCode=LBJ&orgTranslationShort=LeBrJa&orgTranslation=LeBronJames")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbOrganizationsRepository, times(1)).save(ucsbOrg1);
            String expectedJson = mapper.writeValueAsString(ucsbOrg1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_date() throws Exception {
            // arrange
            UCSBOrganizations ucsbOrg1 = UCSBOrganizations.builder()
                            .orgCode("LBJ")
                            .orgTranslationShort("LeBrJa")
                            .orgTranslation("LeBronJames")
                            .inactive(false)
                            .build();

            when(ucsbOrganizationsRepository.findById("LBJ")).thenReturn(Optional.of(ucsbOrg1));

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/ucsborganizations?id=LBJ")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbOrganizationsRepository, times(1)).findById("LBJ");
            verify(ucsbOrganizationsRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBOrganizations with id LBJ deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_ucsborganization_and_gets_right_error_message()
                    throws Exception {
            // arrange

            when(ucsbOrganizationsRepository.findById("lbj")).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/ucsborganizations?id=lbj")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(ucsbOrganizationsRepository, times(1)).findById("lbj");
            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBOrganization with id lbj not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_ucsborganization() throws Exception {
            // arrange
            UCSBOrganizations ucsbOrgOrig = UCSBOrganizations.builder()
                            .orgCode("LBJ")
                            .orgTranslationShort("LeBrJa")
                            .orgTranslation("LeBronJames")
                            .inactive(false)
                            .build();

            UCSBOrganizations ucsbOrgEdited = UCSBOrganizations.builder()
                            .orgCode("STC")
                            .orgTranslationShort("SteCur")
                            .orgTranslation("StephCurry")
                            .inactive(false)
                            .build();

            String requestBody = mapper.writeValueAsString(ucsbOrgEdited);

            when(ucsbOrganizationsRepository.findById("LBJ")).thenReturn(Optional.of(ucsbOrgOrig));

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/ucsborganizations?id=LBJ")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbOrganizationsRepository, times(1)).findById("LBJ");
            verify(ucsbOrganizationsRepository, times(1)).save(ucsbOrgEdited); // should be saved with correct user
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
            // arrange
            UCSBOrganizations ucsbEditedDate = UCSBOrganizations.builder()
                            .orgCode("LBJ")
                            .orgTranslationShort("LeBrJa")
                            .orgTranslation("LeBronJames")
                            .inactive(false)
                            .build();

            String requestBody = mapper.writeValueAsString(ucsbEditedDate);

            when(ucsbOrganizationsRepository.findById("LBJ")).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/ucsborganizations?id=LBJ")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(ucsbOrganizationsRepository, times(1)).findById("LBJ");
            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBDate with id 67 not found", json.get("message"));

    }
}
