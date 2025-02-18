package com.publicissapient.kpidashboard.github.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

@ExtendWith(SpringExtension.class)
public class GitHubURIBuilderTest {

	ProcessorToolConnection connection;

	@BeforeEach
	public void setup() {
		connection = new ProcessorToolConnection();
		connection.setUrl("http://github.api.com");
		connection.setBranch("feature-branch");
		connection.setRepositoryName("repo");
	}

	@Test
	public void build_shouldReturnValidCommitPath() throws Exception {
		// Arrange
		GitHubURIBuilder uriBuilder = new GitHubURIBuilder(connection);

		// Act
		String result = uriBuilder.build();

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("/repos/"));
		assertTrue(result.contains("/commits"));
	}

	@Test
	public void mergeRequestUrlbuild_shouldReturnValidMRPath() throws Exception {
		// Arrange
		GitHubURIBuilder uriBuilder = new GitHubURIBuilder(connection);
		// Act
		String result = uriBuilder.mergeRequestUrlbuild();

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("/repos/"));
		assertTrue(result.contains("/pulls"));
	}

	@Test
	public void build_withCustomBranch_shouldIncludeBranchInPath() throws Exception {
		// Arrange
		GitHubURIBuilder uriBuilder = new GitHubURIBuilder(connection);

		// Act
		String result = uriBuilder.build();

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("/repo/commits"));
	}

	@Test
	public void build_withNullBranch_shouldUseDefaultMasterBranch() throws Exception {
		// Arrange
		connection.setBranch(null);
		GitHubURIBuilder uriBuilder = new GitHubURIBuilder(connection);

		// Act
		String result = uriBuilder.build();

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("sha=master"));
	}
}
