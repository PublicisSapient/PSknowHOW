package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdditionalFilterCategoryServiceImpl implements AdditionalFilterCategoryService {
    @Autowired
    private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;
    @Override
    public List<AdditionalFilterCategory> getAdditionalFilterCategories() {
        return additionalFilterCategoryRepository.findAllByOrderByLevel();
    }
}
