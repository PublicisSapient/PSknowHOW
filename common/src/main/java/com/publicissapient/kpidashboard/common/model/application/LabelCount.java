package com.publicissapient.kpidashboard.common.model.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class to hold data about label & its respective count.
 *
 * @author shunaray
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LabelCount {
    String labelValue;
    Integer countValue;
}
