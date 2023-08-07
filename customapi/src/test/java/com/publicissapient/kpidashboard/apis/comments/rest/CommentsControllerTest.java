package com.publicissapient.kpidashboard.apis.comments.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.comments.service.CommentsService;
import com.publicissapient.kpidashboard.common.model.comments.CommentRequestDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentSubmitDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentViewRequestDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentViewResponseDTO;
import com.publicissapient.kpidashboard.common.model.comments.CommentsInfo;

@RunWith(MockitoJUnitRunner.class)
public class CommentsControllerTest {

	ObjectMapper mapper = new ObjectMapper();
	String node;
	String level;
	String sprintId;
	String kpiId;
	private MockMvc mockMvc;
	@Mock
	private CommentsService commentsService;
	@InjectMocks
	private CommentsController commentsController;

	@Before
	public void before() {
		node = "1";
		level = "2";
		sprintId = "10";
		kpiId = "kpi12";
		mockMvc = MockMvcBuilders.standaloneSetup(commentsController).build();
	}

	@Test
	public void submitCommentsTest() throws Exception {

		CommentSubmitDTO comment = new CommentSubmitDTO();
		comment.setNode(node);
		comment.setLevel(level);
		comment.setNodeChildId(sprintId);
		comment.setKpiId(kpiId);
		List<CommentsInfo> commentsInfo = new ArrayList<>();
		CommentsInfo commentInfo = new CommentsInfo();
		commentInfo.setCommentBy("Mahesh");
		commentInfo.setComment("More Data Required");
		commentsInfo.add(commentInfo);
		comment.setCommentsInfo(commentsInfo);

		when(commentsService.submitComment(comment)).thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.post("/comments/submitComments")
				.content(mapper.writeValueAsString(comment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
		verify(commentsService).submitComment(comment);

	}

	@Test
	public void submitCommentsIssueTest() throws Exception {
		CommentSubmitDTO comment = Mockito.mock(CommentSubmitDTO.class);
		mockMvc.perform(MockMvcRequestBuilders.post("/comments/submitComments")
				.content(mapper.writeValueAsString(comment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());

	}

	@Test
	public void getCommentsTest() throws Exception {

		CommentRequestDTO commentRequestDTO = new CommentRequestDTO();
		commentRequestDTO.setNode(node);
		commentRequestDTO.setLevel(level);
		commentRequestDTO.setNodeChildId(sprintId);
		commentRequestDTO.setKpiId(kpiId);

		Map<String, Object> mappedCollection = new LinkedHashMap<>();
		mappedCollection.put("node", node);
		when(commentsService.findCommentByKPIId(node, level, sprintId, kpiId)).thenReturn(mappedCollection);
		mockMvc.perform(post("/comments/getCommentsByKpiId").content(mapper.writeValueAsString(commentRequestDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

	@Test
	public void testComment_NotFound() throws Exception {
		String expectedResponse = "{'message':'Comment not found','success':false}";

		CommentRequestDTO commentRequestDTO = new CommentRequestDTO();
		commentRequestDTO.setNode(node);
		commentRequestDTO.setLevel(level);
		commentRequestDTO.setNodeChildId(sprintId);
		commentRequestDTO.setKpiId(kpiId);

		mockMvc.perform(post("/comments/getCommentsByKpiId").content(mapper.writeValueAsString(commentRequestDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(expectedResponse));

	}

	@Test
	public void getCommentsViewSummaryTest() throws Exception {

		CommentViewRequestDTO commentViewRequestDTO = new CommentViewRequestDTO();
		List<String> nodes = new ArrayList<>();
		nodes.add("xyz_project_node_id");
		commentViewRequestDTO.setNodes(nodes);
		commentViewRequestDTO.setLevel(level);
		commentViewRequestDTO.setNodeChildId(sprintId);
		List<String> kpiIds = new ArrayList<>();
		kpiIds.add("kpi3");
		kpiIds.add("kpi5");
		commentViewRequestDTO.setKpiIds(kpiIds);

		List<CommentViewResponseDTO> commentViewResponseDTOList = new ArrayList<>();
		CommentViewResponseDTO commentViewResponseDTO = new CommentViewResponseDTO();
		commentViewResponseDTO.setKpiId(kpiId);
		commentViewResponseDTO.setNode(node);
		commentViewResponseDTO.setLevel(level);
		commentViewResponseDTO.setNodeChildId(sprintId);
		commentViewResponseDTO.setComment("test data");
		commentViewResponseDTO.setCommentId("UUID");
		commentViewResponseDTO.setCommentOn("16-May-2023 15:33");
		commentViewResponseDTO.setCommentBy("SUPERADMIN");

		commentViewResponseDTOList.add(commentViewResponseDTO);
		when(commentsService.findLatestCommentSummary(nodes, level, sprintId, kpiIds)).thenReturn(commentViewResponseDTOList);
		mockMvc.perform(post("/comments/commentsSummary").content(mapper.writeValueAsString(commentViewRequestDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

	@After
	public void cleanUp() {
		mockMvc = null;
	}
}
