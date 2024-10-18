package com.publicissapient.kpidashboard.apis.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrganizationHierarchyDataFactory {
	
	private static final String FILE_PATH_ORGANIZATION_HIERARCHY = "/json/default/organization_hierarchy.json";
	
	private List<OrganizationHierarchy> organizationHierarchies;

	private ObjectMapper mapper;
	
	private OrganizationHierarchyDataFactory() {
	}
	
	public static OrganizationHierarchyDataFactory newInstance(String filePath) {
		OrganizationHierarchyDataFactory factory = new OrganizationHierarchyDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static OrganizationHierarchyDataFactory newInstance() {

		return newInstance(null);
	}
	
	private void init(String filePath) {
		try {
			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_ORGANIZATION_HIERARCHY : filePath;

			organizationHierarchies = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<OrganizationHierarchy>>() {
					});
		} catch (Exception e) {
			log.error("Error in reading organization hierarchy from file = " + filePath, e);
		}
	}

	private void createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
	}

	public List<OrganizationHierarchy> getOrganizationHierarchies() {
		return organizationHierarchies;
	}

	public OrganizationHierarchy findById(String id) {

		return organizationHierarchies.stream()
				.filter(organizationHierarchy -> organizationHierarchy.getId().toHexString().equals(id)).findFirst()
				.orElse(null);
	}
}
