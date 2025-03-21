/******************************************************************************
 * ORGANIZATION HIERARCHY MANAGEMENT SCRIPT
 * 
 * !!! READ THIS FIRST !!!
 * 
 * This script has three sections:
 * 1. USER CONFIGURATION SECTION - This is where you make your changes
 * 2. CORE LOGIC SECTION - DO NOT MODIFY this section
 * 3. SCRIPT EXECUTION section which initializes the script, you can modify it if you want to create full hierarchy or just one node

 PRE-REQUISITE
 hierarchy_levels collection should be populated and you must be aware of the hierarchy structure
 ******************************************************************************/


/**============================================================================
 * 
 *                         === USER CONFIGURATION SECTION ===
 *                         ONLY MAKE CHANGES IN THIS SECTION
 * 
 * Instructions:
 * 1. Define your hierarchy structure in createHierarchy()
 * 2. Only modify the following fields in each node:
 *    - nodeName:  UPDATE THE NODE NAMES TO MATCH YOUR ORGANIZATION.
 *    - nodeDisplayName: UPDATE THE nodeDisplayName AS PER YOUR PREFERENCE TO BE VISIBLE ON THE DASHBOARD.
 *    - hierarchyLevelId: Level ID from your MongoDB hierarchy_levels collection
 *    
 * 
 =============================================================================*/

/**
 *The function createHierarchy follows to create hierarchy of mentioned structure
 * 
 * Enterprise Level: Organization
 * Portfolio Level: Business Unit1, Business Unit2
 * Project Level: Portfolio1, Portfolio2, Portfolio3
 * 
 * The resulting hierarchy will look like this:
 * Organization (hierarchyLevelOne)
 * ├── Business Unit1 (hierarchyLevelTwo)
 * │   ├── Portfolio1 (hierarchyLevelThree)
 * │   └── Portfolio2 (hierarchyLevelThree)
 * └── Business Unit2 (hierarchyLevelTwo)
 *     └── Portfolio3 (hierarchyLevelThree)
 *
 */


function createHierarchy() {
    try {
        // Reset tracking array at the start
        createdNodesInSession = [];

        // Step 1: Create Level1 (Top Level)MODIFY HERE FOR CUSTOM TOP-LEVEL NODE)
        const organization = createHierarchyNode({
            nodeName: 'Organization', // CHANGE THIS
            nodeDisplayName: 'Organization',
            hierarchyLevelId: 'hierarchyLevelOne', // CHANGE THIS
            parentId: null // No parent for top level
        });
        console.log('Created Organization:', organization);

        // Step 2: Create Level2 (Second Level)
        const buOne = createHierarchyNode({
            nodeName: 'Business Unit1', // CHANGE THIS
            nodeDisplayName: 'Business Unit1',
            hierarchyLevelId: 'hierarchyLevelTwo', // CHANGE THIS
            parentId: organization.nodeId // Points to organization
        });

        const buTwo = createHierarchyNode({
            nodeName: 'Business Unit2', // CHANGE THIS
            nodeDisplayName: 'Business Unit2',
            hierarchyLevelId: 'hierarchyLevelTwo', // CHANGE THIS
            parentId: organization.nodeId
        });
        console.log('Created Business Units:', buOne, buTwo);

        // Step 3: Create Level3 (Third Level)
        const port1 = createHierarchyNode({
            nodeName: 'Portfolio1', // CHANGE THIS
            nodeDisplayName: 'Portfolio1',
            hierarchyLevelId: 'hierarchyLevelThree', // CHANGE THIS
            parentId: buOne.nodeId // Points to buOne portfolio
        });

        const port2 = createHierarchyNode({
            nodeName: 'Portfolio2', // CHANGE THIS
            nodeDisplayName: 'Portfolio2',
            hierarchyLevelId: 'hierarchyLevelThree', // CHANGE THIS
            parentId: buOne.nodeId
        });

        const port3 = createHierarchyNode({
            nodeName: 'Portfolio3', // CHANGE THIS
            nodeDisplayName: 'Portfolio3',
            hierarchyLevelId: 'hierarchyLevelThree', // CHANGE THIS
            parentId: buTwo.nodeId // Points to buTwo portfolio
        });

        console.log('Hierarchy successfully created!');
        return true;
    } catch (error) {
        console.error('Error creating hierarchy:', error);
        rollbackCreatedNodes();
        return false;
    }
}



/**============================================================================
 * 
 *                         === CORE LOGIC SECTION ===
 *                         DO NOT MODIFY ANYTHING BELOW
 * 
 * This section contains the core functionality of the script.
 * Modifying this code may break the hierarchy creation process.
 * 
 =============================================================================*/

const levels = db.hierarchy_levels.find().sort({
    level: 1
}).toArray();


let createdNodesInSession = [];

function generateUniqueId() {
    return `${Date.now().toString(36)}-${Math.random().toString(36).substr(2, 5)}`;
}

function validateNodeData(nodeData) {
    const required = ['nodeName', 'nodeDisplayName', 'hierarchyLevelId'];
    const missing = required.filter(field => !nodeData[field]);

    if (missing.length > 0) {
        throw new Error(`Missing required fields: ${missing.join(', ')}`);
    }

    const existingNode = db.organization_hierarchy.findOne({
        nodeName: nodeData.nodeName,
        hierarchyLevelId: nodeData.hierarchyLevelId,
        parentId: nodeData.parentId || null
    }, {
        projection: {
            nodeId: 1,
            nodeName: 1
        }
    });

    if (existingNode) {
        throw new Error(`Node with name ${nodeData.nodeName} already exists at this level`);
    }

    const level = levels.find(l => l.hierarchyLevelId === nodeData.hierarchyLevelId);
    if (!level) {
        throw new Error(`Invalid hierarchy level: ${nodeData.hierarchyLevelId}.'`);
    }

    return true;
}

function rollbackCreatedNodes() {
    console.log('Rolling back created nodes...');

    for (const nodeId of createdNodesInSession.reverse()) {
        try {
            db.organization_hierarchy.deleteOne({
                nodeId: nodeId
            });
            console.log(`Rolled back node: ${nodeId}`);
        } catch (rollbackError) {
            console.error(`Failed to rollback node ${nodeId}:`, rollbackError);
        }
    }

    createdNodesInSession = [];
    console.log('Rollback completed');
}

function insertIntoDB(nodeData) {
    const normalizedNode = {
        nodeId: nodeData.nodeId,
        nodeName: nodeData.nodeName,
        nodeDisplayName: nodeData.nodeDisplayName,
        hierarchyLevelId: nodeData.hierarchyLevelId,
        parentId: nodeData.parentId || null,
        createdAt: nodeData.createdAt,
        updatedAt: nodeData.updatedAt
    };

    db.organization_hierarchy.insertOne(normalizedNode);
    createdNodesInSession.push(normalizedNode.nodeId);
    return normalizedNode;
}

function createHierarchyNode(nodeData) {
    try {
        const normalizedNode = {
            nodeId: generateUniqueId(),
            ...nodeData,
            nodeName: nodeData.nodeName,
            createdAt: new Date(),
            updatedAt: new Date()
        };

        validateNodeData(normalizedNode);

        if (normalizedNode.parentId) {
            const parent = db.organization_hierarchy.findOne({
                nodeId: normalizedNode.parentId
            });

            if (!parent) {
                throw new Error(`Parent node not found: ${normalizedNode.parentId}`);
            }


            const parentLevel = levels.find(l => l.hierarchyLevelId === parent.hierarchyLevelId);
            const currentLevel = levels.find(l => l.hierarchyLevelId === normalizedNode.hierarchyLevelId);

            if (parentLevel.level !== currentLevel.level - 1) {
                throw new Error('Parent node must be at a higher level in hierarchy');
            }
        }

        return insertIntoDB(normalizedNode);
    } catch (error) {
        console.error('Error creating hierarchy node:', error);
        throw error;
    }
}


/**============================================================================
 * 
 *                         === SCRIPT EXECUTION ===
 * 
 * Uncomment one of the following approaches to create your hierarchy:
 * 
 =============================================================================*/




// APPROACH 1: Run the hierarchy (recommended for first-time users)
const success = createHierarchy();
if (!success) {
    console.log('Hierarchy creation failed and was rolled back');
}

/* APPROACH 2: Create individual nodes (for advanced users)

// To create your own hierarchy, you can either:
// 1. Modify the createHierarchy() function above
// 2. Or create nodes individually using createHierarchyNode():
/*
const myEnterprise = createHierarchyNode({
    nodeName: 'my_company',
    nodeDisplayName: 'My Company',
    hierarchyLevelId: 'hierarchyLevelOne',
    parentId: null
});
*/