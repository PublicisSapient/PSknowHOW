package com.publicissapient.kpidashboard.apis.comments.service;

import static com.publicissapient.kpidashboard.common.util.DateUtil.dateTimeFormatter;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentsKpiWise;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.commentshistory.KpiCommentsHistoryRepository;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class CommentsServiceImpl implements CommentsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentsServiceImpl.class);

    @Autowired
    private KpiCommentsRepository kpiCommentsRepository;

    @Autowired
    private KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

	/**
	 * This method will find the comment details for selected KpiId and returns mapped data.
	 * @param projectBasicConfig
	 * @param kpiId
	 * @return
	 */
	@Override
	public Map<String, Object> findCommentByKPIId(String projectBasicConfig, String kpiId) {

		List<KPIComments> kpiComments = kpiCommentsRepository.findByCommentKpiWiseKpiId(projectBasicConfig, kpiId);
		LOGGER.info("Received all matching comment from DB, comments size: {}", kpiComments.size());

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(kpiComments)) {
			List<CommentsInfo> finalCommentsInfo = commentMappingOperation(kpiId, kpiComments);
			mappedCollection.put("ProjectBasicConfig", projectBasicConfig);
			mappedCollection.put("KpiId", kpiId);
			mappedCollection.put("CommentsInfo", finalCommentsInfo);
		}
		LOGGER.info("Final filter comments of matching kpiId {}", mappedCollection);
		return mappedCollection;
	}

	/**
	 * This method will submit the comments for selected Kpi in both kpi_comments and kpi_comments_history collections.
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
	 * This method will map the comments with selected KpiId and returns the list of commentsInfo.
	 * @param kpiId
	 * @param kpiComments
	 * @return
	 */
	private List<CommentsInfo> commentMappingOperation(String kpiId, List<KPIComments> kpiComments) {
		List<CommentsInfo> kpiIdMappedWithCommentsInfo = new ArrayList<>();
		for (final KPIComments var1 : kpiComments) {
			List<CommentsKpiWise> kpiWiseList = var1.getCommentsKpiWise();
			if (CollectionUtils.isNotEmpty(kpiWiseList)) {
				for (final CommentsKpiWise var2 : kpiWiseList) {
					if (kpiId.equalsIgnoreCase(var2.getKpiId())) {
						kpiIdMappedWithCommentsInfo.addAll(var2.getCommentsInfo());
					}
				}
			}
		}
		return kpiIdMappedWithCommentsInfo;
	}

	/**
	 * This method will save new comment and delete old commentId if existing commentsList exceeds 10 comments.
	 * @param comment
	 */
	private void setCommentOnDate(final CommentSubmitDTO comment) {
		String projectBasicConfig = comment.getProjectBasicConfig();
		List<CommentsKpiWise> commentsKpiWiseList = comment.getCommentsKpiWise();
		for (final CommentsKpiWise var1 : commentsKpiWiseList) {
			String kpi = var1.getKpiId();
			oldKpiCommentDelete(projectBasicConfig, kpi);
			List<CommentsInfo> commentsInfo = var1.getCommentsInfo();
			if (CollectionUtils.isNotEmpty(commentsInfo)) {
				for (final CommentsInfo var2 : commentsInfo) {
					if (var2 != null) {
						String date = dateTimeFormatter(new Date(), Constant.KPI_COMMENT_ON_DATE_FORMAT);
						var2.setCommentOn(date);
					}
				}
			}
		}
	}

    /**
     * This method will remove the old comment data from the existing commentsList when size exceeds 10 comments.
     * @param projectBasicConfig
     * @param kpiId
     */
	private void oldKpiCommentDelete(String projectBasicConfig, String kpiId) {
		if (projectBasicConfig != null && kpiId != null) {
			List<KPIComments> kpiCommentsSorted = kpiCommentsRepository
					.findByKpiCommentSortByCommentOn(projectBasicConfig, kpiId);
			LOGGER.info("kpiCommentsSorted list basis on commentOn filed  {}", kpiCommentsSorted.size());

			if (CollectionUtils.isNotEmpty(kpiCommentsSorted)
					&& kpiCommentsSorted.size() >= Constant.PER_KPI_COMMENTS_DATA_STORE_COUNT) {
				KPIComments oldCommentId = kpiCommentsSorted.get(0);
				boolean result = kpiCommentsRepository.deleteKpiCommentsData(oldCommentId);
				if (result) {
					LOGGER.info("Old CommentId {} is deleted from kpi_comments collection.", oldCommentId);
				}
			}
		}
	}
}