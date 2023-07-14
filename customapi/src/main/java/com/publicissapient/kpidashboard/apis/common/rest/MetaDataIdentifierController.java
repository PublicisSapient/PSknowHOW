package com.publicissapient.kpidashboard.apis.common.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.MetaDataIdentifierService;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifierDTO;

@RestController
@RequestMapping("/templates")
public class MetaDataIdentifierController {

	@Autowired
	private MetaDataIdentifierService metaDataIdentifierService;

	@GetMapping(value = { "/{basicConfigId}" })
	public ResponseEntity<List<MetadataIdentifierDTO>> getTemplateNames(@PathVariable String basicConfigId) {
		return new ResponseEntity<>(metaDataIdentifierService.getTemplateDetails(), HttpStatus.OK);
	}
}
