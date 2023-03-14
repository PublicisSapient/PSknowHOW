package com.publicissapient.kpidashboard.apis.comments.service;

import static com.publicissapient.kpidashboard.common.util.DateUtil.dateTimeFormatter;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.CommentsKpiWise;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.commentshistory.KpiCommentsHistoryRepository;

@RunWith(MockitoJUnitRunner.class)
public class CommentsServiceImplTest {

	@InjectMocks
	private CommentsServiceImpl commentServiceImpl;

	@Mock
	KpiCommentsRepository kpiCommentsRepository;

	@Mock
	KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

	String node = "1";
	String level = "level";
	String sprintId = "10";
	String kpiId = "kpi12";
	String commentBy = "testUser";
	String comment = "More data required";
	String sortCommentsByDESC = "DESC";
	String sortCommentsByASC = "ASC";

	@Test
	public void submitCommentTest() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
		commentDTO.setNode(node);
		commentDTO.setLevel(level);
		commentDTO.setSprintId(sprintId);

		List<CommentsKpiWise> commentsKpiWise = new ArrayList<>();
		CommentsKpiWise commentKpiWise = new CommentsKpiWise();
		commentKpiWise.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		commentInfo.setCommentBy(commentBy);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentKpiWise.setCommentsInfo(commentsInfo);

		commentsKpiWise.add(commentKpiWise);

		commentsKpiWise.add(commentKpiWise);
		commentDTO.setCommentsKpiWise(commentsKpiWise);

		List<KPIComments> kpiCommentsSorted = new ArrayList<>();

		KPIComments kpiComment1 = Mockito.mock(KPIComments.class);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);
		kpiCommentsSorted.add(kpiComment1);

		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId, sortCommentsByASC))
				.thenReturn(kpiCommentsSorted);
		when(kpiCommentsRepository.deleteKpiCommentsData(Mockito.any(KPIComments.class))).thenReturn(true);

		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertTrue(commentSubmitted);
	}

	@Test
	public void testSubmitComment_withException() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertFalse(commentSubmitted);
	}

	@Test
	public void findCommentByKPIIdTest() {

		List<KPIComments> kpiComments = new ArrayList<>();
		KPIComments kpiComment = new KPIComments();
		kpiComment.setNode(node);
		kpiComment.setLevel(level);
		kpiComment.setSprintId(sprintId);

		List<CommentsKpiWise> commentsKpiWise = new ArrayList<>();
		CommentsKpiWise commentKpiWise = new CommentsKpiWise();
		commentKpiWise.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		String uuid = UUID.randomUUID().toString();
		String date = dateTimeFormatter(new Date(), Constant.KPI_COMMENT_ON_DATE_FORMAT);
		commentInfo.setCommentId(uuid);
		commentInfo.setCommentBy(commentBy);
		commentInfo.setCommentOn(date);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentKpiWise.setCommentsInfo(commentsInfo);

		commentsKpiWise.add(commentKpiWise);
		kpiComment.setCommentsKpiWise(commentsKpiWise);
		kpiComments.add(kpiComment);

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		mappedCollection.put("node", node);
		mappedCollection.put("level", level);
		mappedCollection.put("sprintId", sprintId);
		mappedCollection.put("kpiId", kpiId);
		mappedCollection.put("CommentsInfo", commentsInfo);
		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId, sortCommentsByDESC))
				.thenReturn(kpiComments);

		Map<String, Object> mappedCollectionActual = commentServiceImpl.findCommentByKPIId(node, level, sprintId,
				kpiId);
		Assert.assertEquals(mappedCollection, mappedCollectionActual);

	}
}