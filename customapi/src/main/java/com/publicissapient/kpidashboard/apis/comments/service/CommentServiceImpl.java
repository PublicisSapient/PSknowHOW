package com.publicissapient.kpidashboard.apis.comments.service;

import com.publicissapient.kpidashboard.apis.comments.rest.CommentController;
import com.publicissapient.kpidashboard.common.model.comment.CommentInfo;
import com.publicissapient.kpidashboard.common.model.comment.CommentKpiWise;
import com.publicissapient.kpidashboard.common.model.comment.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comment.KPIComments;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.publicissapient.kpidashboard.common.repository.comment.KpiCommentRepository;

import java.util.ArrayList;
import  java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private KpiCommentRepository kpiCommentRepository;

 public List<KPIComments> findCommentByKPIIdList(String projectBasicConfig,String kpi){
     final ModelMapper modelMapper = new ModelMapper();

     final List<KPIComments> kpiComment1 = kpiCommentRepository.findByCommentKpiWiseKpiIdList(projectBasicConfig,kpi);
    List<KPIComments> commentKpiWise   = kpiComment1.stream().map(h1 -> modelMapper.map(h1 ,KPIComments.class)).collect(Collectors.toList());
          return kpiComment1;
    }

    @Override
    public List<CommentKpiWise> findCommentByKPIId(String projectBasicConfig,String kpi) {
        final ModelMapper modelMapper = new ModelMapper();
        List<CommentKpiWise> finalCommentKpiWise = new ArrayList<>();

        List<KPIComments>  kpiComments =  kpiCommentRepository.findByCommentKpiWiseKpiId(projectBasicConfig,kpi);

        if(kpiComments !=null && !kpiComments.isEmpty()) {
            //Converted Entity class into Model class
            List<KPIComments> commentKpiWise   = kpiComments.stream().map(h1 -> modelMapper.map(h1 ,KPIComments.class)).collect(Collectors.toList());

            for (KPIComments var1 : commentKpiWise) {
                List<CommentKpiWise> ckw = var1.getCommentKpiWise();
                 if(ckw !=null) {
                     for (CommentKpiWise var : ckw) {
                         //Checking condition with kpiId and adding into list
                               if (kpi.equalsIgnoreCase(var.getKpiId())) {
                                   finalCommentKpiWise.add(var);
                                  List<CommentInfo> commentInfo = var.getCommentInfo();
                                 LOGGER.info(var.getCommentInfo().toString());
                         }
                     }
                 }
            }
        }

        return finalCommentKpiWise;

    }



    @Override
    public boolean submitComment(CommentSubmitDTO comment) {

        final ModelMapper modelMapper = new ModelMapper();
        //Converted Model class into Entity class
        final KPIComments kpicomments = modelMapper.map(comment, KPIComments.class);
       kpiCommentRepository.save(kpicomments);
        return true;
    }
}
