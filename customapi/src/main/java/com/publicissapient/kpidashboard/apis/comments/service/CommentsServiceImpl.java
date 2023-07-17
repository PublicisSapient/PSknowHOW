package com.publicissapient.kpidashboard.apis.comments.service;

import static com.publicissapient.kpidashboard.common.util.DateUtil.dateTimeFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentViewResponseDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.comments.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentHistoryRepositoryCustom;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentRepositoryCustom;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Mahesh
 *
 */
@Service
@Slf4j
public class CommentsServiceImpl implements CommentsService {

	public static final String TIME_FORMAT = "dd-MMM-YYYY HH:mm";

	@Autowired
	private KpiCommentsRepository kpiCommentsRepository;

	@Autowired
	private KpiCommentRepositoryCustom kpiCommentsCustomRepository;

	@Autowired
	private KpiCommentHistoryRepositoryCustom kpiCommentsHistoryCustomRepository;

	@Autowired
	private KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * This method will find the comment details for selected KpiId by filtering the
	 * comments based on the organization level and maximum comments count.
	 * 
	 * @param node
	 * @param level
	 * @param nodeChildId
	 * @param kpiId
	 * @return
	 */
	@Override
	public Map<String, Object> findCommentByKPIId(String node, String level, String nodeChildId, String kpiId) {
		// fetching comments from history which are not deleted
		KpiCommentsHistory kpiComments = kpiCommentsHistoryRepository.findByNodeAndLevelAndNodeChildIdAndKpiId(node,
				level, nodeChildId, kpiId);

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		if (null != kpiComments) {
			log.info("Received all matching comment from DB, comments size: {}", kpiComments);
			List<CommentsInfo> finalCommentsInfo = kpiComments.getCommentsInfo().stream()
					.filter(info -> !info.isDeleted()).collect(Collectors.toList());
			mappedCollection.put("node", node);
			mappedCollection.put("level", level);
			mappedCollection.put("nodeChildId", nodeChildId);
			mappedCollection.put("kpiId", kpiId);
			mappedCollection.put("CommentsInfo", finalCommentsInfo);
		}
		log.info("Final filter comments of matching kpiId {}", mappedCollection);
		return mappedCollection;
	}

	@Override
	public Map<String, Integer> findCommentByBoard(List<String> node, String level, String nodeChildId,
			List<String> kpiIds) {
		List<KpiCommentsHistory> kpiCommentsList = kpiCommentsHistoryRepository.findCommentsByBoard(node, level,
				nodeChildId, kpiIds);
		Map<String, Integer> hierarchyWiseComments = new HashMap<>();
		if (CollectionUtils.isNotEmpty(kpiCommentsList)) {
			kpiCommentsList.stream().collect(Collectors.groupingBy(KpiCommentsHistory::getKpiId))
					.forEach((kpiId, commentsList) -> hierarchyWiseComments.merge(kpiId, commentsList.stream()
							.flatMap(comment -> comment.getCommentsInfo().stream().filter(info -> !info.isDeleted()))
							.collect(Collectors.toList()).size(), Integer::sum));
		}
		return hierarchyWiseComments;
	}

	@Override
	public void deleteComments(String commentId) {
		kpiCommentsCustomRepository.deleteByCommentId(commentId);
		kpiCommentsHistoryCustomRepository.markCommentDelete(commentId);
	}

	/**
	 *
	 * @param nodes
	 * @param level
	 * @param nodeChildId
	 * @param kpiIds
	 * @return
	 */
	@Override
	public List<CommentViewResponseDTO> findLatestCommentSummary(List<String> nodes, String level, String nodeChildId,
			List<String> kpiIds) {
		List<KpiCommentsHistory> kpiCommentsList = kpiCommentsHistoryRepository.findCommentsByBoard(nodes, level,
				nodeChildId, kpiIds);

		if (CollectionUtils.isNotEmpty(kpiCommentsList)) {
			return kpiCommentsList.stream().flatMap(kpiComment -> kpiComment.getCommentsInfo().stream()
					.filter(commentsInfo -> !commentsInfo.isDeleted()).map(commentsInfo -> {
						CommentViewResponseDTO commentViewResponseDTO = new CommentViewResponseDTO();
						commentViewResponseDTO.setKpiId(kpiComment.getKpiId());
						commentViewResponseDTO.setNode(kpiComment.getNode());
						commentViewResponseDTO.setLevel(kpiComment.getLevel());
						commentViewResponseDTO.setNodeChildId(kpiComment.getNodeChildId());
						commentViewResponseDTO.setComment(commentsInfo.getComment());
						commentViewResponseDTO.setCommentId(commentsInfo.getCommentId());
						commentViewResponseDTO.setCommentOn(commentsInfo.getCommentOn());
						commentViewResponseDTO.setCommentBy(commentsInfo.getCommentBy());
						return commentViewResponseDTO;
					})).sorted(Comparator.comparing(CommentViewResponseDTO::getCommentOn).reversed())
					.limit(customApiConfig.getLatestKpiCommentsSummary()).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * This method will save the comments for selected KpiId in both kpi_comments
	 * and kpi_comments_history collections.
	 * 
	 * @param comment
	 * @return
	 */
	@Override
	public boolean submitComment(CommentSubmitDTO comment) {

		log.debug("CommentSubmitDTO info {}", comment);
		List<CommentsInfo> commentsInfo = comment.getCommentsInfo();
		if (CollectionUtils.isNotEmpty(commentsInfo)) {
			for (CommentsInfo commentInfo : commentsInfo) {
				commentInfo.setCommentOn(dateTimeFormatter(new Date(), TIME_FORMAT));
				commentInfo.setCommentId(UUID.randomUUID().toString());
			}

			final ModelMapper modelMapper = new ModelMapper();
			KPIComments kpiComments = modelMapper.map(comment, KPIComments.class);
			KpiCommentsHistory kpiCommentsHistory = modelMapper.map(comment, KpiCommentsHistory.class);

			return filterCommentsInfo(kpiComments, kpiCommentsHistory);
		} else {
			log.info("No information about commentsInfo");
			return false;

		}

	}

	/**
	 * This method will save the comments in the collections and re-map the comments
	 * list on the basis of KPI max comments count to be stored in DB.
	 * 
	 * @param kpiComments
	 * @param kpiCommentsHistory
	 * @return
	 */
	private boolean filterCommentsInfo(KPIComments kpiComments, KpiCommentsHistory kpiCommentsHistory) {

		String node = kpiComments.getNode();
		String level = kpiComments.getLevel();
		String kpiId = kpiComments.getKpiId();
		String nodeChildId = kpiComments.getNodeChildId();
		List<CommentsInfo> newCommentsInfo = kpiComments.getCommentsInfo();
		List<CommentsInfo> newCommentsInfoHistory = kpiCommentsHistory.getCommentsInfo();

		try {
			KPIComments matchedKpiComments = kpiCommentsRepository.findCommentsByFilter(node, level, nodeChildId,
					kpiId);
			KpiCommentsHistory matchedKpiCommentsHistory = kpiCommentsHistoryRepository
					.findByNodeAndLevelAndNodeChildIdAndKpiId(node, level, nodeChildId, kpiId);

			if (Objects.isNull(matchedKpiComments)) {
				kpiCommentsRepository.save(kpiComments);
				kpiCommentsHistoryRepository.save(kpiCommentsHistory);
			} else {
				reMappingOfKpiComments(matchedKpiComments, newCommentsInfo);
				reMappingOfKpiCommentsHistory(matchedKpiCommentsHistory, newCommentsInfoHistory);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Issue occurred while performing operation on comment.");
		}
		return false;
	}

	/**
	 * This method will re-map the comments on the basis of KPI max comments count
	 * to be stored in DB for the matched KPI comment. If commentsInfoSize is
	 * greater or equal to the PER_KPI_MAX_COMMENTS_COUNT then oldest comment will
	 * be removed from DB from kpi_comments collection. Note, kpi_comments_history
	 * collection will store all the comments for a selected KPI in DB irrespective
	 * of the KPI max comments count.
	 * 
	 * @param matchedKpiComment
	 * @param newCommentsInfo
	 */
	private void reMappingOfKpiComments(KPIComments matchedKpiComment, List<CommentsInfo> newCommentsInfo) {
		List<CommentsInfo> commentsInfo = matchedKpiComment.getCommentsInfo();
		int commentsInfoSize = commentsInfo.size();
		int perKpiMaxCommentsStoreCount = customApiConfig.getKpiCommentsMaxStoreCount();
		if (commentsInfoSize < perKpiMaxCommentsStoreCount) {
			newCommentsInfo.addAll(commentsInfo);
			matchedKpiComment.setCommentsInfo(newCommentsInfo);
			kpiCommentsRepository.save(matchedKpiComment);
			log.debug("Saved new comment & re-arranged existing comments into kpi_comments collection {}",
					matchedKpiComment);

		} else {
			commentsInfo.remove(commentsInfoSize - 1);
			newCommentsInfo.addAll(commentsInfo);
			matchedKpiComment.setCommentsInfo(newCommentsInfo);
			kpiCommentsRepository.save(matchedKpiComment);
			log.debug("Old comments removed, saved new comment & re-arranged comments into kpi_comments collection {}",
					matchedKpiComment);
		}
	}

	/**
	 * This method will re-map the KPI comments for the kpi_comments_history
	 * collection.
	 * 
	 * @param kpiCommentsHistory
	 * @param newCommentsInfoHistory
	 */
	private void reMappingOfKpiCommentsHistory(KpiCommentsHistory kpiCommentsHistory,
			List<CommentsInfo> newCommentsInfoHistory) {

		List<CommentsInfo> commentsInfoHistory = kpiCommentsHistory.getCommentsInfo();
		newCommentsInfoHistory.addAll(commentsInfoHistory);
		kpiCommentsHistory.setCommentsInfo(newCommentsInfoHistory);
		kpiCommentsHistoryRepository.save(kpiCommentsHistory);
		log.debug("Saved new comment and re-arranged existing comments info into kpi_comments_history collection {}",
				kpiCommentsHistory);
	}

}