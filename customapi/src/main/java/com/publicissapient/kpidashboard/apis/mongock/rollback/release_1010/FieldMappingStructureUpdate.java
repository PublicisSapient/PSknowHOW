package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1010;


import com.publicissapient.kpidashboard.apis.mongock.FieldMappingStructureForMongock;
import com.publicissapient.kpidashboard.apis.mongock.data.FieldMappingStructureDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

/**
 * rollback field-mapping structure data
 *
 * @author aksshriv1
 */
@Slf4j
@ChangeUnit(id = "r_updated_mapping_structure", order = "010109", author = "aksshriv1", systemVersion = "10.1.0")
public class FieldMappingStructureUpdate {

    private final MongoTemplate mongoTemplate;
    private List<FieldMappingStructureForMongock> fieldMappingStructureBackupList;
    private static final String FIELD_MAPPING_STRUCTURE_COLLECTION = "field_mapping_structure";
    private static final String FILE_PATH_FIELD_MAPPING_LIST = "/json/mongock/default/field_mapping_structure_backup_10.0.0.json";
    public FieldMappingStructureUpdate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        FieldMappingStructureDataFactory fieldMappingStructureBackupDataFactory = FieldMappingStructureDataFactory.newInstance(FILE_PATH_FIELD_MAPPING_LIST);
        fieldMappingStructureBackupList= fieldMappingStructureBackupDataFactory.getFieldMappingStructureList();
    }

    @Execution
    public void changeSet() {
        // Delete all existing documents in the collection
        mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE_COLLECTION).deleteMany(new Document());
        MongockUtil.saveListToDB(fieldMappingStructureBackupList, FIELD_MAPPING_STRUCTURE_COLLECTION, mongoTemplate);
    }

    @RollbackExecution
    public void rollback() {
        // no implementation required
    }
}

