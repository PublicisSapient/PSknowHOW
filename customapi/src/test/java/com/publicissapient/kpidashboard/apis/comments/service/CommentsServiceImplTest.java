package com.publicissapient.kpidashboard.apis.comments.service;

import static com.publicissapient.kpidashboard.common.util.DateUtil.dateTimeFormatter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentHistoryRepositoryCustom;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentRepositoryCustom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class CommentsServiceImplTest {

	@Mock
	KpiCommentsRepository kpiCommentsRepository;
	@Mock
	private KpiCommentRepositoryCustom kpiCommentsCustomRepository;

	@Mock
	private KpiCommentHistoryRepositoryCustom kpiCommentsHistoryCustomRepository;

	@Mock
	KpiCommentsHistoryRepository kpiCommentsHistoryRepository;
	String node;
	String level;
	String sprintId;
	String kpiId;
	String commentBy;
	String comment;
	String TIME_FORMAT;
	String date;
	@InjectMocks
	private CommentsServiceImpl commentServiceImpl;
	@Mock
	private CustomApiConfig customApiConfig;

	@Before
	public void before() {
		node = "1";
		level = "level";
		sprintId = "10";
		kpiId = "kpi12";
		commentBy = "testUser";
		comment = "More data required";
		TIME_FORMAT = "dd-MMM-YYYY";
		date = dateTimeFormatter(new Date(), TIME_FORMAT);
	}

	@Test
	public void submitCommentTest_saveComments() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
		commentDTO.setNode(node);
		commentDTO.setLevel(level);
		commentDTO.setSprintId(sprintId);
		commentDTO.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		commentInfo.setCommentBy(commentBy);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentInfo.setCommentOn(date);
		commentInfo.setCommentId(UUID.randomUUID().toString());
		commentDTO.setCommentsInfo(commentsInfo);

		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertTrue(commentSubmitted);
	}

	@Test
	public void submitCommentTest_saveWhenCommentsAlreadyExists() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
		commentDTO.setNode(node);
		commentDTO.setLevel(level);
		commentDTO.setSprintId(sprintId);
		commentDTO.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		commentInfo.setCommentBy(commentBy);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentInfo.setCommentOn(date);
		commentInfo.setCommentId(UUID.randomUUID().toString());
		commentDTO.setCommentsInfo(commentsInfo);

		KPIComments kpiComments = new KPIComments();
		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId)).thenReturn(kpiComments);

		KpiCommentsHistory kpiCommentsHistory = new KpiCommentsHistory();
		kpiCommentsHistory.setCommentsInfo(commentsInfo);
		when(kpiCommentsHistoryRepository.findByNodeAndLevelAndSprintIdAndKpiId(node, level, sprintId, kpiId))
				.thenReturn(kpiCommentsHistory);

		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertTrue(commentSubmitted);
	}

	@Test
	public void submitCommentTest_exceptionWhileSavingComments() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
		commentDTO.setNode(node);
		commentDTO.setLevel(level);
		commentDTO.setSprintId(sprintId);
		commentDTO.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		commentInfo.setCommentBy(commentBy);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentInfo.setCommentOn(date);
		commentInfo.setCommentId(UUID.randomUUID().toString());
		commentDTO.setCommentsInfo(commentsInfo);

		when(kpiCommentsRepository.save(any())).thenThrow(NullPointerException.class);

		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertFalse(commentSubmitted);
	}

	@Test
	public void submitCommentTest_saveWhenCommentsAlreadyExists_commentsAreLessThanConfigMaxComments() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
		commentDTO.setNode(node);
		commentDTO.setLevel(level);
		commentDTO.setSprintId(sprintId);
		commentDTO.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		commentInfo.setCommentBy(commentBy);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentInfo.setCommentOn(date);
		commentInfo.setCommentId(UUID.randomUUID().toString());
		commentDTO.setCommentsInfo(commentsInfo);

		KPIComments kpiComments = new KPIComments();
		kpiComments.setCommentsInfo(commentsInfo);
		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId)).thenReturn(kpiComments);

		KpiCommentsHistory kpiCommentsHistory = new KpiCommentsHistory();
		kpiCommentsHistory.setCommentsInfo(commentsInfo);

		when(customApiConfig.getKpiCommentsMaxStoreCount()).thenReturn(2);
		when(kpiCommentsHistoryRepository.findByNodeAndLevelAndSprintIdAndKpiId(node, level, sprintId, kpiId))
				.thenReturn(kpiCommentsHistory);

		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertTrue(commentSubmitted);
	}

	@Test
	public void submitCommentTest_saveWhenCommentsAlreadyExists_commentsAreMoreThanConfigMaxComments() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
		commentDTO.setNode(node);
		commentDTO.setLevel(level);
		commentDTO.setSprintId(sprintId);
		commentDTO.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		commentInfo.setCommentBy(commentBy);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentInfo.setCommentOn(date);
		commentInfo.setCommentId(UUID.randomUUID().toString());
		commentDTO.setCommentsInfo(commentsInfo);

		KPIComments kpiComments = new KPIComments();
		kpiComments.setCommentsInfo(commentsInfo);
		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId)).thenReturn(kpiComments);

		KpiCommentsHistory kpiCommentsHistory = new KpiCommentsHistory();
		kpiCommentsHistory.setCommentsInfo(commentsInfo);
		when(kpiCommentsHistoryRepository.findByNodeAndLevelAndSprintIdAndKpiId(node, level, sprintId, kpiId))
				.thenReturn(kpiCommentsHistory);

		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertTrue(commentSubmitted);
	}

	@Test
	public void findCommentByKPIIdTest() {
		KPIComments kpiComment = new KPIComments();
		kpiComment.setNode(node);
		kpiComment.setLevel(level);
		kpiComment.setSprintId(sprintId);
		kpiComment.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		String date = dateTimeFormatter(new Date(), TIME_FORMAT);
		commentInfo.setCommentId(UUID.randomUUID().toString());
		commentInfo.setCommentBy(commentBy);
		commentInfo.setCommentOn(date);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		kpiComment.setCommentsInfo(commentsInfo);

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		mappedCollection.put("node", node);
		mappedCollection.put("level", level);
		mappedCollection.put("sprintId", sprintId);
		mappedCollection.put("kpiId", kpiId);
		mappedCollection.put("CommentsInfo", commentsInfo);
		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId)).thenReturn(kpiComment);
		Mockito.when(customApiConfig.getLimitCommentsShownOnKpiDashboardCount()).thenReturn(5);
		Map<String, Object> mappedCollectionActual = commentServiceImpl.findCommentByKPIId(node, level, sprintId,
				kpiId);
		Assert.assertEquals(mappedCollection, mappedCollectionActual);
	}

	@Test
	public void findCommentByBoard() {
		KPIComments kpiComment = new KPIComments();
		kpiComment.setNode(node);
		kpiComment.setLevel(level);
		kpiComment.setSprintId(sprintId);
		kpiComment.setKpiId(kpiId);

		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		String date = dateTimeFormatter(new Date(), TIME_FORMAT);
		commentInfo.setCommentId(UUID.randomUUID().toString());
		commentInfo.setCommentBy(commentBy);
		commentInfo.setCommentOn(date);
		commentInfo.setComment(comment);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		commentsInfo.add(commentInfo);
		kpiComment.setCommentsInfo(commentsInfo);
		List<KPIComments> kpiCommentsList= new ArrayList<>();
		kpiCommentsList.add(kpiComment);

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		mappedCollection.put("kpi12",4);
		when(kpiCommentsRepository.findCommentsByBoard(Arrays.asList(node), level, sprintId, Arrays.asList(kpiId))).thenReturn(kpiCommentsList);
		Map<String, Integer> mappedCollectionActual = commentServiceImpl.findCommentByBoard(Arrays.asList(node), level, sprintId,
				Arrays.asList(kpiId));
		Assert.assertEquals(mappedCollection, mappedCollectionActual);
	}

	@Test
	public void deleteComments() {
		Mockito.doNothing().when(kpiCommentsCustomRepository).deleteByCommentId(anyString());
		Mockito.doNothing().when(kpiCommentsHistoryCustomRepository).markCommentDelete(anyString());
		commentServiceImpl.deleteComments("");

	}


}
