
/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.common.model.azureboards.wiql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "queryType", "queryResultType", "asOf", "columns", "sortColumns", "workItems" })
public class AzureWiqlModel {

	@JsonProperty("queryType")
	private String queryType;
	@JsonProperty("queryResultType")
	private String queryResultType;
	@JsonProperty("asOf")
	private String asOf;
	@JsonProperty("columns")
	private List<Column> columns = null;
	@JsonProperty("sortColumns")
	private List<SortColumn> sortColumns = null;
	@JsonProperty("workItems")
	private List<WorkItem> workItems = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("queryType")
	public String getQueryType() {
		return queryType;
	}

	@JsonProperty("queryType")
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	@JsonProperty("queryResultType")
	public String getQueryResultType() {
		return queryResultType;
	}

	@JsonProperty("queryResultType")
	public void setQueryResultType(String queryResultType) {
		this.queryResultType = queryResultType;
	}

	@JsonProperty("asOf")
	public String getAsOf() {
		return asOf;
	}

	@JsonProperty("asOf")
	public void setAsOf(String asOf) {
		this.asOf = asOf;
	}

	@JsonProperty("columns")
	public List<Column> getColumns() {
		return columns;
	}

	@JsonProperty("columns")
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	@JsonProperty("sortColumns")
	public List<SortColumn> getSortColumns() {
		return sortColumns;
	}

	@JsonProperty("sortColumns")
	public void setSortColumns(List<SortColumn> sortColumns) {
		this.sortColumns = sortColumns;
	}

	@JsonProperty("workItems")
	public List<WorkItem> getWorkItems() {
		return workItems;
	}

	@JsonProperty("workItems")
	public void setWorkItems(List<WorkItem> workItems) {
		this.workItems = workItems;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("queryType", queryType).append("queryResultType", queryResultType)
				.append("asOf", asOf).append("columns", columns).append("sortColumns", sortColumns)
				.append("workItems", workItems).append("additionalProperties", additionalProperties).toString();
	}

}
