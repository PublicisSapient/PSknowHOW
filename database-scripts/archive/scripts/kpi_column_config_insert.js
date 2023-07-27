//script to save the default configuration of Kpi's modalWindow Column
db.getCollection('kpi_column_configs').remove({ "basicProjectConfigId": null });
db.kpi_column_configs.insertMany([{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi8',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Start Time',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'End Time',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Duration',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Url',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi40',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi11',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Repository Url',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Branch',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'No. Of Commit',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'No. of Merge',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi84',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Repository Url',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Branch',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Mean Time To Merge (In Hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi3',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Intake to DOR(In Days)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'DOR to DOD (In Days)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'DOD TO Live (In Days)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Lead Time (In Days)',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi53',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Open to Triage(In Days)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Triage to Complete (In Days)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Complete TO Live (In Days)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Lead Time (In Days)',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi39',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi5',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi46',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Time Estimate (in hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Time Spent (in hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi72',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Initial Commitment',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}
                                 		]
                                 	},
                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi14',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Linked Defects',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi82',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'First Time Pass',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi111',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Linked Defects to Story',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi35',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Escaped Defect',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi34',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Defect Removed',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi37',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Defect Rejected',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi28',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi36',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi132',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Defect ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi136',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Defect ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi126',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Created Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Resolved',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi42',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Automated',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi16',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked Story ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Automated',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi17',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Unit Coverage',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi38',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Sonar Violations',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi27',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Tech Debt (in days)',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi116',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Build Count',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Build Failure Count',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Failure Percentage',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi70',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Test',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Executed Test',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Execution %',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed Test',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed %',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi113',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Cost of Delay',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic Name',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic End Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Month',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi125',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Planned Completion Date (Due Date)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Actual Completion Date',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Potential Delay(in days)',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 10,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi73',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Release Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Release Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Release End Date',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Month',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi118',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Date',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Environment',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi80',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Defects Without Story Link',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi79',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked to Story',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi129',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi127',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi62',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Unit Coverage',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi64',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Sonar Violations',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi67',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Tech Debt (in days)',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi71',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Execution Date',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Test',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Executed Test',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Execution %',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed Test',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed %',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi63',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Automated',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi997',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi48',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi51',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi50',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi55',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi54',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi49',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size (In Story Points)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi66',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Start Time',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'End Time',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Duration',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Url',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi65',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Repository Url',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Branch',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'No. Of Commit',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi58',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Start Date',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'End Date',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Estimated Capacity (in hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi123',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi120',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi124',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi130',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Hours',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Delay',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi75',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Logged Work',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi119',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Dev Due Date',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Dev Completion Date',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 10,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Potential Delay(in days)',
                                 			order: 11,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 12,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi131',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Blocked Time',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Wait Time',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Wastage',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi133',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Linked Stories',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked Stories Size',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi134',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi122',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi135',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'First Time Pass',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked Defect',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect Priority',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi128',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Actual Start Date',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Dev Completion Date',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Actual Completion Date',
                                 			order: 10,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Delay(in days)',
                                 			order: 11,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 12,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Potential Delay(in days)',
                                 			order: 13,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 14,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi139',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Change Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},
                                 	{
                                        basicProjectConfigId: null,
                                        kpiId: 'kpi140',
                                        kpiColumnDetails: [{
                                            columnName: 'Defect ID',
                                            order: 0,
                                            isShown: true,
                                            isDefault: false
                                        }, {
                                            columnName: 'Issue Description',
                                            order: 1,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Issue Status',
                                            order: 2,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Issue Type',
                                            order: 3,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Size(story point/hours)',
                                            order: 4,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Root Cause',
                                            order: 5,
                                            isShown: true,
                                            isDefault: false
                                        }, {
                                            columnName: 'Priority',
                                            order: 6,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Assignee',
                                            order: 7,
                                            isShown: true,
                                            isDefault: false
                                        }, {
                                            columnName: 'Created during Iteration',
                                            order: 8,
                                            isShown: true,
                                            isDefault: false
                                        }]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi141',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi142',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi143',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi144',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                 	{
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi147',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                  {
                                     basicProjectConfigId: null,
                                     kpiId: 'kpi145',
                                     kpiColumnDetails: [{
                                       columnName: 'Issue Id',
                                       order: 0,
                                       isShown: true,
                                       isDefault: true
                                    },{
                                       columnName: 'Issue Description',
                                       order: 1,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Issue Status',
                                       order: 2,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Issue Type',
                                       order: 3,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Size(story point/hours)',
                                       order: 4,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Remaining Estimate',
                                       order: 5,
                                       isShown: true,
                                       isDefault: false
                                    }, {
                                       columnName: 'Dev Due Date',
                                       order: 6,
                                       isShown: true,
                                       isDefault: false
                                    }, {
                                       columnName: 'Dev Completion Date',
                                       order: 7,
                                       isShown: true,
                                       isDefault: false
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi138',
                                    kpiColumnDetails: [{
                                        columnName: 'Issue Id',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                    }, {
                                        columnName: 'Issue Type',
                                        order: 1,
                                        isShown: true,
                                        isDefault: true
                                    },{
                                        columnName: 'Issue Description',
                                        order: 2,
                                        isShown: true,
                                        isDefault: true
                                    },{
                                        columnName: 'Priority',
                                        order: 3,
                                        isShown: true,
                                        isDefault: false
                                    }, {
                                        columnName: 'Size(story point/hours)',
                                        order: 4,
                                        isShown: true,
                                        isDefault: false
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'Kpi146',
                                    kpiColumnDetails: [{
                                        columnName: 'Date',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                    }]
                                   },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'Kpi148',
                                    kpiColumnDetails: [{
                                        columnName: 'Date',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                        }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi149',
                                    kpiColumnDetails: [{
                                        columnName: 'Sprint Name',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                    }, {
                                        columnName: 'User Name',
                                        order: 1,
                                        isShown: true,
                                        isDefault: true
                                    },{
                                        columnName: 'Sprint Rating',
                                        order: 2,
                                        isShown: true,
                                        isDefault: true
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi150',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Story Size(In story point)',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Assignee',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Status',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi151',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Created Date',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Updated Date',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi152',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Created Date',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Updated Date',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  }
                                 ]);