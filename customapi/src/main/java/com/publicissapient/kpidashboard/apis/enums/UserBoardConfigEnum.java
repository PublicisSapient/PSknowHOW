package com.publicissapient.kpidashboard.apis.enums;

import java.util.Arrays;
import java.util.List;

public enum UserBoardConfigEnum {
	SCRUM_KANBAN_BOARD(Arrays.asList("Iteration")),
	OTHER_BOARD(Arrays.asList( "Release", "Dora", "Backlog", "Kpi Maturity"));

	private final List<String> boardName;

	UserBoardConfigEnum(List<String> boardName) {
		this.boardName = boardName;
	}

	public List<String> getBoardName() {
		return boardName;
	}

}
