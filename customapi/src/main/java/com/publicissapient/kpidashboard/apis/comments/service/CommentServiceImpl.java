package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.comment.CommentInfo;
import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;
import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.repository.commentshistory.KpiCommentsHistoryRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.publicissapient.kpidashboard.common.repository.comment.KpiCommentRepository;


import java.util.*;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private KpiCommentRepository kpiCommentRepository;

    @Autowired
    private KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

    @Override
    public Map<String, Object>  findCommentByKPIId(String projectBasicConfig,String kpi) {

        List<KPIComments>  kpiComments =  kpiCommentRepository.findByCommentKpiWiseKpiId(projectBasicConfig,kpi);
        LOGGER.info("Received all matching comment from Db {}", kpiComments);


        Map<String, Object> mappedCollection= new LinkedHashMap<>();
        if(kpiComments !=null && !kpiComments.isEmpty()) {
            List<CommentInfo> finalCommentInfo =commentMappingOperation(kpi, kpiComments);
            mappedCollection.put("ProjectBasicConfig",projectBasicConfig);
            mappedCollection.put("KpiId",kpi);
            mappedCollection.put("CommentsInfo",finalCommentInfo);
         }
        LOGGER.info("final filter comments of matching kpiId {}", mappedCollection);

        return mappedCollection;
    }

    @Override
    public boolean submitComment(CommentSubmitDTO comment) {

        final ModelMapper modelMapper = new ModelMapper();
        setCommentOnDate(comment);
       final KPIComments kpiComments = modelMapper.map(comment, KPIComments.class);
        final KpiCommentsHistory kpiCommentsHistory = modelMapper.map(comment, KpiCommentsHistory.class);
        kpiCommentRepository.save(kpiComments);
        kpiCommentsHistoryRepository.save(kpiCommentsHistory);
        return true;
    }
    private List<CommentInfo> commentMappingOperation(String kpi, List<KPIComments> kpiComments) {

        List<CommentInfo> kpiIdMappedWithCommentInfo=new ArrayList<>();

        for (KPIComments var1 : kpiComments) {
            List<CommentKpiWise> kpiWiseList = var1.getCommentKpiWise();
            if(kpiWiseList !=null) {
                for (CommentKpiWise var2 : kpiWiseList) {
                    if (kpi.equalsIgnoreCase(var2.getKpiId())) {
                        kpiIdMappedWithCommentInfo.addAll(var2.getCommentInfo());
                       /*
                        List<KPIComments>  kpiComments1 =  kpiCommentRepository.findByCommentKpiWiseKpiId(projectBasicConfig,kpi);
                       if(kpiComments1.size()> Constant.PER_KPI_COMMENTS_DATA_STORE_COUNT)
                        {
                            kpiCommentRepository.delete();
                        }

                        */

                    }
                }
            }
        }
        return kpiIdMappedWithCommentInfo;
    }

    private void setCommentOnDate(CommentSubmitDTO comment)
    {

        List<CommentKpiWise> commentKpiWise1=   comment.getCommentKpiWise();
        for(CommentKpiWise var1 : commentKpiWise1)
        {
            List<CommentInfo> commentInfo= var1.getCommentInfo();
            if(commentInfo != null) {
                for (CommentInfo var2 : commentInfo) {
                    var2.setCommentOn(new Date());
                }
            }


        }
    }

}
