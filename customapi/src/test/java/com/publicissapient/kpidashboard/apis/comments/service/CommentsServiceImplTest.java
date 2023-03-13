package com.publicissapient.kpidashboard.apis.comments.service;

import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;
import com.publicissapient.kpidashboard.common.model.comments.CommentsKpiWise;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.commentshistory.KpiCommentsHistoryRepository;

/**
 * @author shusingh7
 */
@RunWith(MockitoJUnitRunner.class)
public class CommentsServiceImplTest {

    @InjectMocks
    private CommentsServiceImpl commentsServiceImpl;

    @Mock
    KpiCommentsRepository kpiCommentsRepository;

    @Mock
    KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

    String projectBasicConfig="JIRA PROJECT LEAD_636b4e1b50fcfe4df80ca469";
    String kpiId="kpi14";

//    @Test
//    public void testFindCommentByKPIId_mappedComments() {
//
//        List<KPIComments> kpiComments = new ArrayList<>(1);
//        KPIComments kpiComment = Mockito.mock(KPIComments.class);
//        kpiComments.add(kpiComment);
//        when(kpiCommentsRepository.findByCommentKpiWiseKpiId(Mockito.anyString(), Mockito.anyString())).thenReturn(kpiComments);
//
//        Map<String, Object> mappedCollection = commentsServiceImpl.findCommentByKPIId(projectBasicConfig, kpiId);
//
//        Assert.assertEquals(mappedCollection.size(), 3);
//        Assert.assertSame(mappedCollection.get("ProjectBasicConfig"), projectBasicConfig);
//        Assert.assertSame(mappedCollection.get("KpiId"), kpiId);
//        Assert.assertNotNull(mappedCollection.get("CommentsInfo"));
//    }
//
//    @Test
//    public void testFindCommentByKPIId_mappedCollection() {
//
//        List<KPIComments> kpiComments = new ArrayList<>(1);
//        KPIComments kpiComment = Mockito.mock(KPIComments.class);
//        kpiComments.add(kpiComment);
//        when(kpiCommentsRepository.findByCommentKpiWiseKpiId(Mockito.anyString(), Mockito.anyString())).thenReturn(kpiComments);
//
//        List<CommentsKpiWise> commentsKpiWise = new ArrayList<>(1);
//        CommentsKpiWise commentKpiWise = new CommentsKpiWise();
//        commentKpiWise.setKpiId(kpiId);
//        List<CommentsInfo> commentsInfo = new ArrayList<>(1);
//        commentKpiWise.setCommentsInfo(commentsInfo);
//        commentsKpiWise.add(commentKpiWise);
//        when(kpiComment.getCommentsKpiWise()).thenReturn(commentsKpiWise);
//
//        Map<String, Object> mappedCollection = commentsServiceImpl.findCommentByKPIId(projectBasicConfig, kpiId);
//
//        Assert.assertEquals(mappedCollection.size(), 3);
//        Assert.assertSame(mappedCollection.get("ProjectBasicConfig"), projectBasicConfig);
//        Assert.assertSame(mappedCollection.get("KpiId"), kpiId);
//        Assert.assertNotNull(mappedCollection.get("CommentsInfo"));
//    }
//
//    @Test
//    public void testSubmitComment_forCommentsLimit() {
//
//        final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
//        commentDTO.setProjectBasicConfig(projectBasicConfig);
//
//        final List<CommentsKpiWise> commentsKpiWise = new ArrayList<>(1);
//        final CommentsKpiWise commentKpiWise = new CommentsKpiWise();
//        commentKpiWise.setKpiId(kpiId);
//        final List<CommentsInfo> commentsInfo = new ArrayList<>(1);
//        final CommentsInfo commentInfo = new CommentsInfo();
//        commentsInfo.add(commentInfo);
//        commentKpiWise.setCommentsInfo(commentsInfo);
//        commentsKpiWise.add(commentKpiWise);
//        commentDTO.setCommentsKpiWise(commentsKpiWise);
//
//        List<KPIComments> kpiComments = new ArrayList<>(1);
//        KPIComments kpiComment = Mockito.mock(KPIComments.class);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//        kpiComments.add(kpiComment);
//
//        when(kpiCommentsRepository.findByKpiCommentSortByCommentOn(Mockito.anyString(), Mockito.anyString())).thenReturn(kpiComments);
//        when(kpiCommentsRepository.deleteKpiCommentsData(Mockito.any(KPIComments.class))).thenReturn(true);
//
//        final boolean commentSubmitted = commentsServiceImpl.submitComment(commentDTO);
//
//        Assert.assertTrue(commentSubmitted);
//    }
//
//    @Test
//    public void testSubmitComment_withException() {
//        final CommentSubmitDTO commentDTO = new CommentSubmitDTO();
//        final boolean commentSubmitted = commentsServiceImpl.submitComment(commentDTO);
//        Assert.assertFalse(commentSubmitted);
//    }
}
