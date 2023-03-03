package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.comment.CommentInfo;
import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;
import com.publicissapient.kpidashboard.common.model.kpicommentshistory.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.repository.commentshistory.KpiCommentsHistoryRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.publicissapient.kpidashboard.common.repository.comment.KpiCommentRepository;


import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.publicissapient.kpidashboard.common.util.DateUtil.dateTimeFormatter;


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
        try {
            final ModelMapper modelMapper = new ModelMapper();
            setCommentOnDate(comment);
            KPIComments kpiComments = modelMapper.map(comment, KPIComments.class);
            final KpiCommentsHistory kpiCommentsHistory = modelMapper.map(comment, KpiCommentsHistory.class);
            kpiCommentRepository.saveIntoCollection(kpiComments);
            kpiCommentsHistoryRepository.save(kpiCommentsHistory);
           return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("Issue occur while saving the comment");
        }
        return false;
    }
    private List<CommentInfo> commentMappingOperation(String kpi, List<KPIComments> kpiComments) {

        List<CommentInfo> kpiIdMappedWithCommentInfo=new ArrayList<>();

        for (KPIComments var1 : kpiComments) {
            List<CommentKpiWise> kpiWiseList = var1.getCommentKpiWise();
            if(CollectionUtils.isNotEmpty(kpiWiseList)){
                for (CommentKpiWise var2 : kpiWiseList) {
                    if (kpi.equalsIgnoreCase(var2.getKpiId())) {
                        kpiIdMappedWithCommentInfo.addAll(var2.getCommentInfo());
                    }
                }
            }
           }
        return kpiIdMappedWithCommentInfo;
     }

    private void setCommentOnDate(CommentSubmitDTO comment)
    {
      String projectBasicConfig=  comment.getProjectBasicConfig();
        List<CommentKpiWise> commentKpiWise=   comment.getCommentKpiWise();
        for(CommentKpiWise var1 : commentKpiWise)
        {
            String kpi=var1.getKpiId();

            oldKpiCommentDelete(projectBasicConfig, kpi);

            List<CommentInfo> commentInfo= var1.getCommentInfo();
            if(CollectionUtils.isNotEmpty(commentInfo)) {
                for (CommentInfo var2 : commentInfo) {
                     if(var2 !=null) {
                         String date = dateTimeFormatter(new Date(), Constant.KPI_COMMENT_ON_DATE_FORMAT);
                         var2.setCommentOn(date);
                     }
                }
            }


        }
    }

    private void oldKpiCommentDelete(String projectBasicConfig, String kpi) {
         if (projectBasicConfig != null && kpi != null )
          {
            List<KPIComments> kpiCommentsSorted = kpiCommentRepository.findByKpiCommentSortByCommentOn(projectBasicConfig, kpi);
            LOGGER.info(" kpiCommentsSorted list basis on commentOn filed  {}", kpiCommentsSorted.size());

            if (CollectionUtils.isNotEmpty(kpiCommentsSorted) &&
                    kpiCommentsSorted.size() > Constant.PER_KPI_COMMENTS_DATA_STORE_COUNT)
                 {
                   KPIComments oldCommentId = kpiCommentsSorted.get(0);
                   Boolean result = kpiCommentRepository.deleteKpiCommentsData(oldCommentId);
                     if (result) {
                      LOGGER.info(" Old Comment got deleted from kpi_comments collection & document id is :{}", oldCommentId);
                      }

            }
        }
    }

}
