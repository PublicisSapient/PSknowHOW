package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.publicissapient.kpidashboard.common.repository.comment.KpiCommentRepository;
import  java.util.List;
import java.util.stream.Collectors;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    @Autowired
    private KpiCommentRepository kpiCommentRepository;

  public KPIComments findKPIId(String kpiId){
       final KPIComments kpiComment = kpiCommentRepository.findById(kpiId).get();
   return kpiComment;
    }

    @Override
    public List<KPIComments> findCommentByKPIId(String projectBasicConfig,String kpi) {
        final ModelMapper modelMapper = new ModelMapper();

      final List<KPIComments> kpiComments =  kpiCommentRepository.findByCommentKpiWiseKpiId(projectBasicConfig,kpi);

        return kpiComments;

    }



    @Override
    public boolean submitComment(CommentSubmitDTO comment) {

        final ModelMapper modelMapper = new ModelMapper();
        final KPIComments kpicomments = modelMapper.map(comment, KPIComments.class);
        kpiCommentRepository.save(kpicomments);
        return true;
    }
}
