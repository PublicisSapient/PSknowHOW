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

package com.publicissapient.kpidashboard.apis.model;

import java.util.List;

/**
 * The Account filter request input
 */
public class AccountFilterRequest extends BaseModel {

	private int currentSelection;
	private String currentSelectionLabel;
	private List<AccountFilterResponse> filterDataList;
	private Boolean kanban;
	private List<String> sprintIncluded;
	private boolean activeSprintIncluded;

	/**
	 * Gets current selection.
	 *
	 * @return the current selection
	 */
	public int getCurrentSelection() {
		return currentSelection;
	}

	/**
	 * Sets current selection.
	 *
	 * @param currentSelection
	 *            the current selection
	 */
	public void setCurrentSelection(int currentSelection) {
		this.currentSelection = currentSelection;
	}

	/**
	 * Gets filter data list.
	 *
	 * @return the filter data list
	 */
	public List<AccountFilterResponse> getFilterDataList() {
		return filterDataList;
	}

	/**
	 * Sets filter data list.
	 *
	 * @param filterDataList
	 *            the filter data list
	 */
	public void setFilterDataList(List<AccountFilterResponse> filterDataList) {
		this.filterDataList = filterDataList;
	}

	/**
	 * Gets current selection label.
	 *
	 * @return the current selection label
	 */
	public String getCurrentSelectionLabel() {
		return currentSelectionLabel;
	}

	/**
	 * Sets current selection label.
	 *
	 * @param currentSelectionLabel
	 *            the current selection label
	 */
	public void setCurrentSelectionLabel(String currentSelectionLabel) {
		this.currentSelectionLabel = currentSelectionLabel;
	}

	/**
	 * Is kanban boolean.
	 *
	 * @return the boolean
	 */
	public boolean isKanban() {
		return this.kanban;
	}

	/**
	 * Sets kanban.
	 *
	 * @param kanban
	 *            the kanban
	 */
	public void setKanban(boolean kanban) {
		this.kanban = kanban;
	}

	public boolean isActiveSprintIncluded() {
		return activeSprintIncluded;
	}

	public void setActiveSprintIncluded(boolean activeSprintIncluded) {
		this.activeSprintIncluded = activeSprintIncluded;
	}

	/**
	 * @return sprintIncluded
	 */
	public List<String> getSprintIncluded() {
		return this.sprintIncluded;
	}

	/**
	 * @param sprintIncluded
	 *            sprintIncluded
	 */
	public void setSprintIncluded(List<String> sprintIncluded) {
		this.sprintIncluded = sprintIncluded;
	}
}
