package com.publicissapient.kpidashboard.apis.comments.service;


import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsHistoryRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



/**
 * @author Mahesh
 *
 */
@Service
@Slf4j
public class CommentsServiceImpl implements CommentsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommentsServiceImpl.class);
	@Autowired
	private KpiCommentsRepository kpiCommentsRepository;

	@Autowired
	private KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

	/**
	 * This method will find the comment details for selected KpiId or for selected
	 * Sprint for the project and returns mapped data.
	 */
	@Override
	public Map<String, Object> findCommentByKPIId(String node, String level, String sprintId, String kpiId) {

		KPIComments kpiComments = kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId);

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		if (null!=kpiComments) {
			LOGGER.info("Received all matching comment from DB, comments size: {}", kpiComments);
			List<CommentsInfo> finalCommentsInfo = commentMappingOperation(kpiComments);
			mappedCollection.put("node", node);
			mappedCollection.put("level", level);
			mappedCollection.put("sprintId", sprintId);
			mappedCollection.put("kpiId", kpiId);
			mappedCollection.put("CommentsInfo", finalCommentsInfo);
		}
		LOGGER.info("Final filter comments of matching kpiId {}", mappedCollection);
		return mappedCollection;
	}

	/**
	 * This method will map the comments with selected KpiId for the project and
	 * returns the list of commentsInfo.
	 */
	private List<CommentsInfo> commentMappingOperation(KPIComments kpiComments) {

		List<CommentsInfo> kpiIdMappedWithCommentsInfo = new ArrayList<>();
		List<CommentsInfo> commentsInfo = kpiComments.getCommentsInfo();

		for (CommentsInfo commentInfo : commentsInfo) {
			kpiIdMappedWithCommentsInfo.add(commentInfo);
			if (kpiIdMappedWithCommentsInfo.size() == Constant.HOW_MANY_COMMENTS_SHOW_ON_KPI_DASHBOARD_COUNT) {
				break;
			}
		}

		return kpiIdMappedWithCommentsInfo;
	}
	/**
	 * This method will save the comments for selected Kpi in both kpi_comments and
	 * kpi_comments_history collections.
	 */
	@Override
	public boolean submitComment(CommentSubmitDTO comment) {

		LOGGER.debug("CommentSubmitDTO info {}", comment);
		List<CommentsInfo> commentsInfo = comment.getCommentsInfo();
		if (CollectionUtils.isNotEmpty(commentsInfo)) {
			for (CommentsInfo commentInfo : commentsInfo) {
				commentInfo.setCommentOn(DateUtil.dateTimeFormatter(new Date(), DateUtil.TIME_FORMAT));
				commentInfo.setCommentId(UUID.randomUUID().toString());
			}
		}
		final ModelMapper modelMapper = new ModelMapper();
		KPIComments kpiComments = modelMapper.map(comment, KPIComments.class);
		KpiCommentsHistory kpiCommentsHistory =modelMapper.map(comment, KpiCommentsHistory.class);

		boolean result = doRepositoryOperation(kpiComments,kpiCommentsHistory);

	return result;
	}

	private boolean doRepositoryOperation(KPIComments kpiComments,KpiCommentsHistory kpiCommentsHistory) {

		String node = kpiComments.getNode();
		String level = kpiComments.getLevel();
		String kpiId = kpiComments.getKpiId();
		String sprintId = kpiComments.getSprintId();
		List<CommentsInfo> newCommentsInfo = kpiComments.getCommentsInfo();
		List<CommentsInfo> newCommentsInfoHistory = kpiCommentsHistory.getCommentsInfo();

		try {
			KPIComments matchedKpiComments = kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId);
			KpiCommentsHistory matchedKpiCommentsHistory = kpiCommentsHistoryRepository.findByNodeAndLevelAndSprintIdAndKpiId(node, level, sprintId, kpiId);

			if (null == matchedKpiComments) {
				kpiCommentsRepository.save(kpiComments);
				kpiCommentsHistoryRepository.save(kpiCommentsHistory);
			} else {
					reArrangeKpiComments(matchedKpiComments, newCommentsInfo);
				    reArrangeKpiCommentsHistory(matchedKpiCommentsHistory,newCommentsInfoHistory);
			}
		return true;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Issue occurred while performing operation on comment.");
		}
	return false;
	}



	private void reArrangeKpiComments(KPIComments kpiComment,List<CommentsInfo> newCommentsInfo){
		List<CommentsInfo> commentsInfo = kpiComment.getCommentsInfo();
		int commentsInfoSize = commentsInfo.size();
		KPIComments backupKpiComment;

		if(commentsInfoSize < Constant.PER_KPI_MAX_COMMENTS_COUNT) {
			newCommentsInfo.addAll(commentsInfo);
			kpiComment.setCommentsInfo(newCommentsInfo);
			backupKpiComment = kpiComment;
			kpiCommentsRepository.delete(kpiComment);
			kpiCommentsRepository.save(backupKpiComment);

			LOGGER.debug("Saved comments info into kpi_comment Collection {}", backupKpiComment);

		} else {    //commentsInfoSize >= Constant.PER_KPI_MAX_COMMENTS_COUNT
			commentsInfo.remove(commentsInfoSize - 1);
			newCommentsInfo.addAll(commentsInfo);
			kpiComment.setCommentsInfo(newCommentsInfo);
			backupKpiComment = kpiComment;
			kpiCommentsRepository.delete(kpiComment);
			kpiCommentsRepository.save(backupKpiComment);

			LOGGER.debug("Saved comments into kpi_comment Collection {}", backupKpiComment);
		}
	}

	private void reArrangeKpiCommentsHistory(KpiCommentsHistory kpiCommentsHistory,List<CommentsInfo> newCommentsInfoHistory){

		List<CommentsInfo> commentsInfoHistory = kpiCommentsHistory.getCommentsInfo();
		newCommentsInfoHistory.addAll(commentsInfoHistory);
		kpiCommentsHistory.setCommentsInfo(newCommentsInfoHistory);
		KpiCommentsHistory kpiCommentsHistoryBackup = kpiCommentsHistory;

		kpiCommentsHistoryRepository.delete(kpiCommentsHistory);
		kpiCommentsHistoryRepository.save(kpiCommentsHistoryBackup);
		LOGGER.debug("Saved comments info {}", kpiCommentsHistoryBackup);

	}


}