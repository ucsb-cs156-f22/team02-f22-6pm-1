package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController{
    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    @ApiOperation(value = "List all MenuItemReview")
    @PreAuthorize("hasRole('Role_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allMenuItemReview() {
        Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
        return reviews;
    }

    @ApiOperation(value = "Get a single review")
    @PreAuthorize("hasRole('Role_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @ApiParam("id") @RequestParam Long id) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return menuItemReview;
    }

    @ApiOperation(value = "Create a new review")
    @PreAuthorize("hasRole('Role_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postMenuItemReview(
            @ApiParam("id") @RequestParam Long id,
            @ApiParam("review") @Valid @RequestBody MenuItemReview review) {

        MenuItemReview menuItemReview = new MenuItemReview();
        menuItemReview.setReviewerEmail(review.getReviewerEmail());
        menuItemReview.setStars(review.getStars());
        menuItemReview.setDateReviewed(review.getDateReviewed());
        menuItemReview.setComments(review.getComments());

        MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);
        
        return savedMenuItemReview;
    }
    
    @ApiOperation(value = "Delete a review")
    @PreAuthorize("hasRole('Role_ADMIN')")
    @DeleteMapping("")
    public Object deleteMenuItemReview(
            @ApiParam("id") @RequestParam Long id) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));
    
         menuItemReviewRepository.delete(menuItemReview);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single date")
    @PreAuthorize("hasRole('Role_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateMenuItemReview(
            @ApiParam("id") @RequestParam Long id,
            @ApiParam("review") @Valid @RequestBody MenuItemReview review) {

        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));
        
        menuItemReview.setReviewerEmail(review.getReviewerEmail());
        menuItemReview.setStars(review.getStars());
        menuItemReview.setDateReviewed(review.getDateReviewed());
        menuItemReview.setComments(review.getComments());

        menuItemReviewRepository.save(menuItemReview);
        
        return menuItemReview;
    }
}
