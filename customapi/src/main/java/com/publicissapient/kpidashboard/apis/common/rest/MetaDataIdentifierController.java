package com.publicissapient.kpidashboard.apis.common.rest;


import com.publicissapient.kpidashboard.apis.common.service.MetaDataIdentifierService;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifierDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/templates")
public class MetaDataIdentifierController {

    @Autowired
     private MetaDataIdentifierService metaDataIdentifierService;

    @GetMapping
    public ResponseEntity<List<MetadataIdentifierDTO>> getTemplateNames() {
        return new ResponseEntity<>(metaDataIdentifierService.getTemplateNamesAndID(), HttpStatus.OK);
    }
}
