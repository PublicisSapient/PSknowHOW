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

import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { environment } from "../../../../environments/environment";
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';

@Component({
	selector: 'app-access-mgmt',
	templateUrl: './access-mgmt.component.html',
	styleUrls: ['./access-mgmt.component.css', '../profile.component.css'],
})
export class AccessMgmtComponent implements OnInit {
	@ViewChild('projectfilter') projectFilter;
	users = [];
	allUsers = [];
	userData: any;
	rolesRequest = <any>'';
	rolesData: any;
	roleList: any;
	projectsData = {};
	dropdownSettingsProject = {};
	projects: any;
	displayDialog: boolean;
	displayDuplicateProject: boolean;
	selectedProjects = <any>[];
	addedProjectsOrNodes = <any>[];
	selectedProjectAccess = {};
	selectedProjectAccessIndex = 0;
	searchRole: any;
	searchProject: any;
	searchRoleList: any;
	dataLoading = <any>[];
	submitValidationMessage = <string>'';
	projectHierarchyData = <any>[];
	subscription = <any>{};
	toolTipHtml = <string>'';
	top = <string>'';
	left = <string>'';
	showToolTip = <boolean>false;
	allProjectsData = <any>[];
	enableAddBtn = false;
	accessConfirm: boolean;
	showAddUserForm: boolean = false;
	addData: object = {
		"authType": 'SSO',
		"username": '',
		"emailAddress": '',
		"projectsAccess": []
	};
	ssoLogin = environment.SSO_LOGIN;
	isSuperAdmin: boolean = false;

	constructor(private service: SharedService, private httpService: HttpService, private messageService: MessageService, private confirmationService: ConfirmationService, private authService: GetAuthorizationService) { }


	ngOnInit() {
		this.isSuperAdmin = this.authService.checkIfSuperUser();
		this.getRolesList();
		this.getUsers();
		this.subscription = this.service.passAllProjectsData.subscribe((allProjectsData) => {
			this.receiveProjectsData(allProjectsData);
		});
	}

	// fetches all users
	getUsers() {
		this.httpService.getAllUsers().subscribe((userData) => {
			if (userData[0] !== 'error' && !userData.error) {
				this.users = userData.data;
				this.allUsers = this.users;
			} else {
				// show error message
				this.messageService.add({ severity: 'error', summary: 'Error in fetching user data. Please try after some time.' });
			}
			this.dataLoading.push('allUsers');
		});
	}

	receiveProjectsData(data) {
		this.allProjectsData = data;
	}

	mouseEnter(event, item, node) {
		// console.log(event, item, accessLevel);
		const accessLevel = node.accessLevel;
		if (this.allProjectsData?.length) {
			// console.log(this.allProjectsData);
			if (accessLevel.toLowerCase() === 'project') {
				const tooltipProject = this.allProjectsData?.filter((proj) => proj.id === item.itemId);
				this.toolTipHtml = `<span>Project: ${tooltipProject[0].projectName}</span><br/>`;
				tooltipProject[0].hierarchy.forEach(hier => {
					this.toolTipHtml += `<span>${hier.hierarchyLevel.hierarchyLevelName}: ${hier.value}</span><br/>`;
				});
			} else {
				let selectedHierarchy = [];
				this.allProjectsData.every((proj) => {
					proj.hierarchy.forEach(hier => {
						if (hier.hierarchyLevel.hierarchyLevelId === accessLevel && hier.value === item.itemName) {
							selectedHierarchy = proj.hierarchy;
							return false;
						}
					});
					return true;
				});

				const hierarchyArray = selectedHierarchy.map((elem) => elem.hierarchyLevel.hierarchyLevelId);
				this.toolTipHtml = ``;
				selectedHierarchy.forEach(hier => {
					if (hierarchyArray.indexOf(hier.hierarchyLevel.hierarchyLevelId) <= hierarchyArray.indexOf(accessLevel)) {
						this.toolTipHtml += `<span>${hier.hierarchyLevel.hierarchyLevelName}: ${hier.value}</span><br/>`;
					}
				});
			}
		} else {
			this.toolTipHtml = `<span>Please wait while projects data loads.</span>`;
		}
		this.top = event.pageY + 'px';
		this.left = event.pageX + 'px';
		this.showToolTip = true;
	}

	mouseLeave() {
		this.showToolTip = false;
	}

	// removes project
	removeProject(itemName, projectArr) {
		this.removeByAttr(projectArr, 'itemName', itemName);
	}

	removeByAttr(arr, attr, value) {
		let i = arr.length;
		while (i--) {
			if (arr[i] && arr[i].hasOwnProperty(attr) && arguments.length > 2 && arr[i][attr] === value) {
				arr.splice(i, 1);
			}
		}
		return arr;
	}

	// fetches the roles list
	getRolesList() {
		this.rolesRequest = this.httpService.getRolesList().subscribe((roles) => {
			this.rolesData = roles;
			if (this.rolesData['success']) {
				this.roleList = roles.data.map((role) => ({
						label: role.roleName,
						value: role.roleName,
					}));

				this.searchRoleList = [
					{
						label: 'Select Role',
						value: '',
					},
					...this.roleList,
				];
			} else {
				// show error message
				this.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
			}
			this.dataLoading.push('allRoles');
		});
	}

	filterByProject() {
		if (this.searchProject.length >= 3) {
			this.users = this.users?.filter((user) => user?.projectsAccess?.some((obj) => obj?.accessNodes?.some((level) => level?.accessItems?.some((proj) => proj?.itemName?.toLowerCase().indexOf(this.searchProject.toLowerCase()) > -1))));
		} else if (this.searchProject.length === 0) {
			this.users = this.allUsers;
		}
	}

	filterByRole() {
		this.users = this.allUsers;
		this.searchProject = '';
		if (this.searchRole.length) {
			this.users = this.users.filter((user) => user.projectsAccess.some((obj) => obj.role === this.searchRole));
		} else {
			this.users = this.allUsers;
		}
	}

	showDialogToAdd(projectsArr, projectAccess, index) {
		this.projectFilter.resetDropdowns = true;
		this.projectFilter.clearFilters();

		this.selectedProjectAccess = projectAccess;
		this.selectedProjectAccessIndex = index;
		this.selectedProjects = projectsArr;
		this.addedProjectsOrNodes = [];
		this.displayDialog = true;
		this.enableAddBtn = false;
	}

	cancelDialog() {
		this.hide();
		this.displayDialog = false;
	}

	hide() {
		this.projectFilter.resetDropdowns = true;
	}

	saveDialog() {
		// remove duplicates
		this.addedProjectsOrNodes = this.addedProjectsOrNodes.filter((items) => (items.accessItems = items.accessItems.filter((item, index, self) => index === self.findIndex((t) => t.itemId === item.itemId))));
		if (this.selectedProjects && this.selectedProjects.length) {
			this.selectedProjects = this.selectedProjects.concat(this.addedProjectsOrNodes);
		} else {
			this.selectedProjects = this.addedProjectsOrNodes;
		}

		// remove duplicates
		this.selectedProjects.forEach(element => {
			element.accessItems = Array.from(
				new Set(element.accessItems.map((object) => JSON.stringify(object)))
			).map((str: any) => JSON.parse(str));
		});

		this.selectedProjectAccess[this.selectedProjectAccessIndex].accessNodes = [...this.selectedProjects];
		this.displayDialog = false;
	}

	addRow(projectsAccess) {
		if (projectsAccess) {
			projectsAccess.push({
				role: 'ROLE_PROJECT_VIEWER',
				accessNodes: [],
			});
		}
	}

	removeRow(projectsAccess, index) {
		projectsAccess.splice(index, 1);
	}

	saveAccessChange(userData) {
		// clean userdata, remove empty access-nodes
		if (userData['projectsAccess']?.length) {
			userData['projectsAccess'].forEach((element) => {
				if (element.role !== 'ROLE_SUPERADMIN') {
					element['accessNodes'].forEach((node, index) => {
						if (node['accessItems']?.length === 0) {
							element['accessNodes'].splice(index, 1);
						}
					});
				}
			});
		}

		const uniqueProjectArr = [];
		const uniqueRoleArr = [];
		if (userData?.projectsAccess?.length) {
			userData.projectsAccess.forEach((obj) => {
				if (!uniqueRoleArr.includes(obj.role)) {
					uniqueRoleArr.push(obj.role);
					if (obj.accessNodes.length) {
						obj.accessNodes.forEach((node) => {
							node.accessItems.forEach((item) => {
								if (!uniqueProjectArr.includes(item.itemId)) {
									uniqueProjectArr.push(item.itemId);
								} else {
									this.submitValidationMessage = `Duplicate Access (${item.itemName.toUpperCase()})`;
									this.displayDuplicateProject = true;
								}
							});
						});
					} else if (obj.role !== 'ROLE_SUPERADMIN') {
						this.submitValidationMessage = 'You are submitting a role with empty project list. Please add projects.';
						this.displayDuplicateProject = true;
					}
				} else {
					this.submitValidationMessage = `A row for ${obj.role} already exists, please add accesses there.`;
					this.displayDuplicateProject = true;
				}
			});
		}

		if (!this.displayDuplicateProject) {
			this.httpService.updateAccess(userData, userData.username).subscribe((response) => {
				if (response['success']) {
					if(this.showAddUserForm){
						this.showAddUserForm = false;
						this.messageService.add({ severity: 'success', summary: 'User added.', detail: '' });
						this.resetAddDataForm();
					}else{
						this.messageService.add({ severity: 'success', summary: 'Access updated.', detail: '' });
					}
				} else {
					this.messageService.add({ severity: 'error', summary: 'Error in updating project access. Please try after some time.' });
				}
			});
		}
	}

	projectSelectedEvent(accessItem: any): void {
		if (accessItem && accessItem?.value?.length && !Object.keys(accessItem.valueRemoved)?.length && accessItem.accessType !== 'project') {
			const accessIndex = this.addedProjectsOrNodes.findIndex((x) => x.accessLevel === accessItem.accessType);
			if (accessIndex != -1) {
				this.addedProjectsOrNodes[accessIndex]['accessItems'] = [...this.addedProjectsOrNodes[accessIndex].accessItems, ...accessItem.value];
			} else {
				this.addedProjectsOrNodes = accessItem.value.map((item) => ({
						accessLevel: accessItem.accessType,
						accessItems: [...accessItem.value]
					}));
			}
		} else if (accessItem.accessType === 'project') {
			const accessIndex = this.addedProjectsOrNodes.findIndex((x) => x.accessLevel === accessItem.accessType);
			if (accessIndex != -1) {
				this.addedProjectsOrNodes[accessIndex]['accessItems'] = [...accessItem.value];
			} else {
				this.addedProjectsOrNodes = accessItem.value.map((item) => ({
						accessLevel: accessItem.accessType,
						accessItems: [...accessItem.value]
					}));
			}
		} else if (accessItem.valueRemoved.val.length === 1) {
			this.addedProjectsOrNodes = this.addedProjectsOrNodes.filter((items) => (items.accessItems = items.accessItems.filter((item, index, self) => index === self.findIndex((t) => t.itemId !== accessItem.valueRemoved.val[0].code))));
		} else {
			// clear filters clicked
			console.log('clear filters clicked');
			this.addedProjectsOrNodes = [];
		}

		// remove duplicate projects
		this.addedProjectsOrNodes = this.addedProjectsOrNodes.filter((items) => (items.accessItems = items.accessItems.filter((item, index, self) => index === self.findIndex((t) => t.itemId === item.itemId))));
		this.enableAddBtn = true;
	}

	onRoleChange(event, index, access) {
		const idx = access.findIndex((x) => x.role === event.value);
		if (idx != -1 && idx != index) {
			this.submitValidationMessage = `A row for ${event.value} already exists, please add accesses there.`;
			this.displayDuplicateProject = true;
		}
	}


	deleteUser(userName, userRole) {
		this.accessConfirm = true;
		let isSuperAdmin = false;
		if (userRole?.length > 0) {
			userRole.forEach(role => {
				if (role === 'ROLE_SUPERADMIN') {
					isSuperAdmin = true;
				}
			});
		}
		this.confirmationService.confirm({
			message: 'User and related access will be deleted forever, are you sure you want to delete it?',
			header: `Delete ${userName}?`,
			icon: 'pi pi-info-circle',
			accept: () => {
				this.deleteAccessReq(userName, isSuperAdmin);
			}
		});
	}


	deleteAccessReq(userName, isSuperAdmin) {
		this.httpService.deleteAccess(userName).subscribe(response => {
			this.accessDeletionStatus(response, isSuperAdmin);
		}, error => {
			this.accessDeletionStatus(error, isSuperAdmin);
		});
	}

	accessDeletionStatus(data, isSuperAdmin) {
		this.accessConfirm = false;
		let message = '';
		let icon = '';
		if (data.success) {
			message = data.message;
			icon = 'fa fa-check-circle alert-success';
			this.getUsers();
		} else if (!data.success && isSuperAdmin) {
			message = 'SUPERADMIN cannot be deleted';
			icon = 'fa fa-times-circle alert-danger';
		} else {
			message = 'Something went wrong. Please try again after sometime.';
			icon = 'fa fa-times-circle alert-danger';
		}

		this.confirmationService.confirm({
			message,
			header: 'Access Deletion Status',
			icon
		});
	}

	checkIfDisabled(){
		let res = true;
		if(this.addData['username'] && this.addData['emailAddress'] && this.addData['projectsAccess']?.length > 0){
			res = false;
		}
		return res;
	}

	resetAddDataForm(){
		this.addData = {
			"authType": 'SSO',
			"username": '',
			"emailAddress": '',
			"projectsAccess": []
		};
	}
}
