package com.publicissapient.kpidashboard.common.model.application;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("additional_filter_categories")
public class AdditionalFilterCategory {
    private int level;
    private String filterCategoryId;
    private String filterCategoryName;

}
