package com.publicissapient.kpidashboard.common.repository.comments;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.comments.KPIComments;

/**
 * @author mahesh Repository for {@link KPIComments}.
 */
@Repository
public interface KpiCommentsRepository extends MongoRepository<KPIComments, String> {

	@Query("{ 'node' : ?0, 'level' : ?1, 'nodeChildId' : ?2, 'kpiId' : ?3}")
	KPIComments findCommentsByFilter(String node, String level, String nodeChildId, String kpiId);

	@Query("{ 'node' : { $in : ?0 }, 'level' : ?1, 'nodeChildId' : ?2, 'kpiId' : { $in : ?3 }}")
	List<KPIComments> findCommentsByBoard(List<String> nodes, String level, String nodeChildId, List<String> kpiIds);
}
