package com.publicissapient.kpidashboard.githubaction.util;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.githubaction.config.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class GitHubActionURIBuilder {

    private final ProcessorToolConnection gitHubActionToolConnection;

    public GitHubActionURIBuilder(ProcessorToolConnection toolConnection) {
        this.gitHubActionToolConnection = toolConnection;
    }

    public String deployGithub() throws URISyntaxException {
        URI uri = getURI();
        String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
        final URIBuilder builder = new URIBuilder(scheme + "://" + uri.getHost() + getDeployPath());
        if (uri.getPort() > 0) {
            builder.setPort(uri.getPort());
        }

        getParams().forEach(builder::addParameter);

        return builder.build().toString();
    }

    public String build() throws URISyntaxException {
        URI uri = getURI();
        String scheme = "ssh".equalsIgnoreCase(uri.getScheme()) ? "https" : uri.getScheme();
        final URIBuilder builder = new URIBuilder(scheme + "://" + uri.getHost() + getBuildPath());
        if (uri.getPort() > 0) {
            builder.setPort(uri.getPort());
        }

        getParams().forEach(builder::addParameter);

        return builder.build().toString();
    }

    private Map<String, String> getParams() {
        Map<String, String> map = new HashMap<>();
        map.put("per_page", Constants.PER_PAGE_SIZE);
        return map;
    }
    private URI getURI() {
        String url = gitHubActionToolConnection.getUrl();
        url = StringUtils.removeEnd(url, ".git");
        return URI.create(url.replace(" ", "%20"));
    }

    private String getBuildPath() {
        StringBuilder sb = new StringBuilder();
        sb.append("/repos/" + gitHubActionToolConnection.getUsername() + "/" + gitHubActionToolConnection.getRepositoryName()
                + "/actions/workflows/" + gitHubActionToolConnection.getWorkflowID() + "/runs");
        return sb.toString();
    }

    private String getDeployPath() {
        StringBuilder sb = new StringBuilder();
        sb.append("/repos/" + gitHubActionToolConnection.getUsername() + "/" + gitHubActionToolConnection.getRepositoryName()
                + "/deployment" );
        return sb.toString();
    }

}
