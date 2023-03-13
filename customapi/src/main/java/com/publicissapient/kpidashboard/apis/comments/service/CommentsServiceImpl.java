package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.CommentsKpiWise;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.commentshistory.KpiCommentsHistoryRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.publicissapient.kpidashboard.common.util.DateUtil.dateTimeFormatter;

@Service
@Slf4j
public class CommentsServiceImpl implements CommentsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommentsServiceImpl.class);
	String sortCommentsByDESC = "DESC";
	String sortCommentsByASC = "ASC";

	@Autowired
	private KpiCommentsRepository kpiCommentsRepository;

	@Autowired
	private KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

	/**
	 * This method will find the comment details for selected KpiId or for selected
	 * Sprint for the project and returns mapped data.
	 * 
	 * @param node
	 * @param level
	 * @param sprintId
	 * @param kpiId
	 * @return
	 */
	@Override
	public Map<String, Object> findCommentByKPIId(String node, String level, String sprintId, String kpiId) {

		List<KPIComments> kpiComments = kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId,
				sortCommentsByDESC);

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(kpiComments)) {
			LOGGER.info("Received all matching comment from DB, comments size: {}", kpiComments.size());
			List<CommentsInfo> finalCommentsInfo = commentMappingOperation(kpiId, kpiComments);
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
	 * This method will save the comments for selected Kpi in both kpi_comments and
	 * kpi_comments_history collections.
	 * 
	 * @param comment
	 * @return
	 */
	@Override
	public boolean submitComment(CommentSubmitDTO comment) {
		try {
			final ModelMapper modelMapper = new ModelMapper();
			setCommentOnDate(comment);
			KPIComments kpiComments = modelMapper.map(comment, KPIComments.class);
			final KpiCommentsHistory kpiCommentsHistory = modelMapper.map(comment, KpiCommentsHistory.class);
			kpiCommentsRepository.saveIntoCollection(kpiComments);
			kpiCommentsHistoryRepository.save(kpiCommentsHistory);
			LOGGER.info("Saved comments info {}", kpiComments);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Issue occurred while saving the comment.");
		}
		return false;
	}

	/**
	 * This method will map the comments with selected KpiId for the project and
	 * returns the list of commentsInfo.
	 * 
	 * @param kpiId
	 * @param kpiComments
	 * @return
	 */
	private List<CommentsInfo> commentMappingOperation(String kpiId, List<KPIComments> kpiComments) {
		List<CommentsInfo> kpiIdMappedWithCommentsInfo = new ArrayList<>();
		for (final KPIComments var1 : kpiComments) {
			List<CommentsKpiWise> kpiWiseList = var1.getCommentsKpiWise();
			if (CollectionUtils.isNotEmpty(kpiWiseList)) {
				for (CommentsKpiWise var2 : kpiWiseList) {
					if (kpiId.equalsIgnoreCase(var2.getKpiId())) {
						kpiIdMappedWithCommentsInfo.addAll(var2.getCommentsInfo());
					}
				}
			}
			if (kpiIdMappedWithCommentsInfo.size() == Constant.PER_KPI_MAX_COMMENTS_COUNT) {
				break;
			}
		}
		return kpiIdMappedWithCommentsInfo;
	}

	/**
	 * This method will save new comment and delete old commentId if existing
	 * commentsList exceeds configured max comments.
	 * 
	 * @param comment
	 */
	private void setCommentOnDate(CommentSubmitDTO comment) {
		List<CommentsKpiWise> commentsKpiWiseList = comment.getCommentsKpiWise();
		for (final CommentsKpiWise var1 : commentsKpiWiseList) {
			String kpi = var1.getKpiId();
			oldKpiCommentDelete(comment, kpi);
			List<CommentsInfo> commentsInfo = var1.getCommentsInfo();
			if (CollectionUtils.isNotEmpty(commentsInfo)) {
				for (CommentsInfo var2 : commentsInfo) {
					if (var2 != null) {
						String date = dateTimeFormatter(new Date(), Constant.KPI_COMMENT_ON_DATE_FORMAT);
						var2.setCommentOn(date);
						String uuid = UUID.randomUUID().toString();
						var2.setCommentId(uuid);
					}
				}
			}
		}
	}

	/**
	 * This method will remove the old comment data from the existing commentsList
	 * when size exceeds configured max comments.
	 * 
	 * @param comment
	 * @param kpiId
	 */
	private void oldKpiCommentDelete(CommentSubmitDTO comment, String kpiId) {
		String node = comment.getNode();
		String level = comment.getLevel();
		String sprintId = comment.getSprintId();
		List<KPIComments> kpiCommentsSorted = kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId,
				sortCommentsByASC);
		LOGGER.info("kpiCommentsSorted list with size:  {} sorted by commentOn field.", kpiCommentsSorted.size());

		if (CollectionUtils.isNotEmpty(kpiCommentsSorted)
				&& kpiCommentsSorted.size() >= Constant.PER_KPI_MAX_COMMENTS_COUNT) {
			KPIComments oldCommentId = kpiCommentsSorted.get(0);
			boolean result = kpiCommentsRepository.deleteKpiCommentsData(oldCommentId);
			if (result) {
				LOGGER.info("Old CommentId {} is deleted from kpi_comments collection.", oldCommentId);
			}
		}
	}
}