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


import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsHistoryRepository;

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
	String commentBy = "Mahesh";
	String comment = "More data required";

    static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	String date = dateTimeFormatter(new Date(), TIME_FORMAT);



	@Test
	public void submitCommentTest() {
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

		KPIComments kpiComment = new KPIComments();

		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId))
				.thenReturn(null);

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
		kpiComment.setCommentsInfo(commentsInfo);


		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		mappedCollection.put("node", node);
		mappedCollection.put("level", level);
		mappedCollection.put("sprintId", sprintId);
		mappedCollection.put("kpiId", kpiId);
		mappedCollection.put("CommentsInfo", commentsInfo);
		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId))
				.thenReturn(kpiComment);

		Map<String, Object> mappedCollectionActual = commentServiceImpl.findCommentByKPIId(node, level, sprintId, kpiId);
		Assert.assertEquals(mappedCollection, mappedCollectionActual);

	}

	/*@Test
	public void submitCommentTest_ReMapping() {
		final CommentSubmitDTO commentDTO = new CommentSubmitDTO();

		//KPIComments matchedKpiComments = Mockito.mock(KPIComments.class);

		KPIComments matchedKpiComments = new KPIComments();

		//KPIComments matchedKpiComments = Mockito.mock(KPIComments.class);
		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo= new CommentsInfo();
		commentInfo.setComment("first comment");
		commentsInfo.add(commentInfo);
		matchedKpiComments.setCommentsInfo(commentsInfo);



		when(kpiCommentsRepository.findCommentsByFilter(node, level, sprintId, kpiId))
				.thenReturn(matchedKpiComments);
		when(kpiCommentsRepository.save(matchedKpiComments)).thenReturn(null);
		//	when(kpiCommentsRepository.deleteKpiCommentsData(Mockito.any(KPIComments.class))).thenReturn(true);

		final boolean commentSubmitted = commentServiceImpl.submitComment(commentDTO);
		Assert.assertTrue(commentSubmitted);
	}
*/

}

