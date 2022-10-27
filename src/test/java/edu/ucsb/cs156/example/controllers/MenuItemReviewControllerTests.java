package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

@WebMvcTest(value = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {
    
    @MockBean
    MenuItemReviewRepository menuItemReviewRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/ucsbdates/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/MenuItemReview/all"))
                        .andExpect(status().is(403)).andReturn(); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/MenuItemReview/all"))
                        .andExpect(status().is(200)).andReturn(); // logged in users can get all
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/MenuItemReview/1"))
                        .andExpect(status().is(403)).andReturn(); // logged out users can't get by id
    }

    //Authoriation tests for /api/ucsbdates/post
    // (Perhaps should have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/MenuItemReview/post"))
                        .andExpect(status().is(403)).andReturn(); // logged out users can't post

    // Tests with mocks for database actions
    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
        //arrange
        LocalDateTime ldt = LocalDateTime.parse("2022-04-20");

        MenuItemReview menuItemReview = MenuItemReview.builder()
                .id(1)
                .itemId(27)
                .reviewerEmail("cgaucho@ucsb.edu")
                .stars(3)
                .dateReviewed(ldt)
                .comments("bland af")
                .build();

        when(menuItemReviewRepository.findById(1L)).thenReturn(Optional.of(reviews));

        //act
        MvcResult response = mockMvc.perform(get("/api/MenuItemReview/1"))
                    .andExpect(status().isOk()).andReturn();

        //assert

        verify(menuItemReviewRepository, times(1)).findById(1L);
        String expectedJson = mapper.writeValueAsString(menuItemReview);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_menuitemreviews() throws Exception {
        //arrange
        LocalDateTime ldt = LocalDateTime.parse("2022-04-20");

        MenuItemReview menuItemReview1 = MenuItemReview.builder()
                .id(1)
                .itemId(27)
                .reviewerEmail("cgaucho@ucsb.edu")
                .stars(3)
                .dateReviewed(ldt)
                .comments("bland af")
                .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-04-20");

        MenuItemReview menuItemReview2 = MenuItemReview.builder()
                .id(2)
                .itemId(29)
                .reviewerEmail("cgaucho@ucsb.edu")
                .stars(5)
                .dateReviewed(ldt2)
                .comments("best apple pie ever")
                .build();

        LocalDateTime ldt3 = LocalDateTime.parse("2022-04-21");

        MenuItemReview menuItemReview3 = MenuItemReview.builder()
                .id(3)
                .itemId(29)
                .reviewerEmail("	ldelplaya@ucsb.edu")
                .stars(0)
                .dateReviewed(ldt3)
                .comments("not tryna get food poisoning, but if I were this would do it")
                .build();

        ArrayList<MenuItemReview> expectedReviews = new ArrayList<MenuItemReview>();
        expectedReviews.addAll(Arrays.asList(menuItemReview1, menuItemReview3));
            
        when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

        // act
        MvcResult response = mockMvc.perform(get(urlTemplate: "/api/MenuItemReview/all"))
                .andExpect(status().isOk()).andReturn();

        //assert

}
