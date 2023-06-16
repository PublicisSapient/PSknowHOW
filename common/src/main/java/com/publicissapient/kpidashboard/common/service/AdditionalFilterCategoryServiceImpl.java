package com.publicissapient.kpidashboard.common.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;

@Service
public class AdditionalFilterCategoryServiceImpl implements AdditionalFilterCategoryService {
	@Autowired
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;

	@Override
	public List<AdditionalFilterCategory> getAdditionalFilterCategories() {
		return additionalFilterCategoryRepository.findAllByOrderByLevel();
	}
}
