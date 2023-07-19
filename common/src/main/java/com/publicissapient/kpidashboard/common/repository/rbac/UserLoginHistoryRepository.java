package com.publicissapient.kpidashboard.common.repository.rbac;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.rbac.UsersLoginHistory;

public interface UserLoginHistoryRepository extends CrudRepository<UsersLoginHistory, ObjectId> {
}