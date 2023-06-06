package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiDTO;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class HappinessKpiServiceImpl {

    @Autowired
    HappinessKpiDataRepository happinessKpiDataRepository;

    public ServiceResponse saveHappinessKpiData(HappinessKpiDTO happinessKpiDTO) {

        HappinessKpiData happinessKpiData = null;
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatterLocalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String result = formatterLocalDate.format(localDate);
        if(null!=happinessKpiDTO)
        {
            ModelMapper mapper = new ModelMapper();
            happinessKpiDTO.setDateOfSubmission(result);
            happinessKpiData = mapper.map(happinessKpiDTO, HappinessKpiData.class);
            happinessKpiData.setBasicProjectConfigId(new ObjectId(happinessKpiDTO.getBasicProjectConfigId()));
        }
        if (null == happinessKpiData) {
            log.info("happinessKpiData object is empty");
            return new ServiceResponse(false, "happinessKpiData cannot be empty", null);
        }

        if (!valid(happinessKpiData)) {
            log.info("happinessKpiData is not valid");
            return new ServiceResponse(false, "BasicProjectConfigId, Sprint Id or userRatingList cannot be empty or null", null);
        }

        HappinessKpiData existingForSameDay = happinessKpiDataRepository.findExistingByBasicProjectConfigIdAndSprintIDAndDateOfSubmission(happinessKpiData.getBasicProjectConfigId(),happinessKpiData.getSprintID(),happinessKpiData.getDateOfSubmission());
        if(existingForSameDay!=null)
        {
            happinessKpiData.setId(existingForSameDay.getId());
            happinessKpiDataRepository.save(happinessKpiData);
            log.info("Successfully updated happinessKpiData into db");
            return new ServiceResponse(true, "updated existing happinessKpiData", happinessKpiData);
        }
        else {
            happinessKpiDataRepository.save(happinessKpiData);
            log.info("Successfully created and saved happinessKpiData into db");
            return new ServiceResponse(true, "created and saved new happinessKpiData", happinessKpiData);
        }
    }

    public boolean valid(HappinessKpiData happinessKpiData) {
        if (happinessKpiData.getBasicProjectConfigId() == null) {
            log.info("projectBasicConfigId is null");
            return false;
        }

        if (happinessKpiData.getUserRatingList() == null || happinessKpiData.getUserRatingList().isEmpty()) {
            log.info("userRatingData list is null or empty");
            return false;
        }

        if (happinessKpiData.getSprintID()==null || happinessKpiData.getSprintID().isEmpty()) {
            log.info("sprintID is null or empty");
            return false;
        }
        if (happinessKpiData.getDateOfSubmission()==null || happinessKpiData.getDateOfSubmission().isEmpty()) {
            log.info("dateOfSubmission is null or empty");
            return false;
        }

        return true;
    }
}
