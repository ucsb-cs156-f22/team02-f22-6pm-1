//Implemented by Saahil Joshi
package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;
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

@Api(description = "UCSBOrganizations")
@RequestMapping("/api/UCSBOrganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {
    
    @Autowired
    UCSBOrganizationsRepository ucsbOrganizationsRepository; 

    @ApiOperation(value = "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Iterable<UCSBOrganizations> allOrganizations() {
        Iterable<UCSBOrganizations> org = ucsbOrganizationsRepository.findAll();
        return org;
    }

    @ApiOperation(value = "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganizations getById(
            @ApiParam("code") @RequestParam String code) {
        UCSBOrganizations org = ucsbOrganizationsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, code));

        return org;
    }
      

    @ApiOperation(value = "Create a new org")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganizations postOrganizations(
        @ApiParam("orgCode") @RequestParam String orgCode,
        @ApiParam("orgTranslationShort") @RequestParam String orgTranslationShort,
        @ApiParam("orgTranslation") @RequestParam String orgTranslation,
        @ApiParam("inactive") @RequestParam boolean inactive
        )
        {

        UCSBOrganizations org = new UCSBOrganizations();
        org.setOrgCode(orgCode);
        org.setOrgTranslationShort(orgTranslationShort);
        org.setOrgTranslation(orgTranslation);
        org.setInactive(inactive);
        

        UCSBOrganizations savedOrg = ucsbOrganizationsRepository.save(org);

        return savedOrg;
    }

    @ApiOperation(value = "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBOrganization(
            @ApiParam("orgCode") @RequestParam Long orgCode) {
        UCSBOrganizations ucsbOrganizations = ucsbOrganizationsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, id));

        ucsbOrganizationsRepository.delete(ucsbOrganizations);
        return genericMessage("UCSBOrganizations with id %s deleted".formatted(orgCode));
    }

    @ApiOperation(value = "Update a single organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganizations updateOrganizations(
            @ApiParam("orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganizations incoming) {

        UCSBOrganizations org = ucsbOrganizationsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));


        org.setOrgCode(incoming.getOrgCode());
        org.setOrgTranslationShort(incoming.getOrgTranslationShort());
        org.setOrgTranslation(incoming.getOrgTranslation());
        org.setInactive(incoming.getInactive());

        ucsbOrganizationsRepository.save(org);

        return org;
    }


}
