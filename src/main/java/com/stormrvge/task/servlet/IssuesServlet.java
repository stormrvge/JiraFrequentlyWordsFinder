package com.stormrvge.task.servlet;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.stormrvge.task.util.Dictionary;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IssuesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String PROJECT_NAME = req.getParameter("project_name");

        resp.setContentType("text/html");

        if (PROJECT_NAME != null) {
            List<Issue> issues = getIssues(PROJECT_NAME);

            if (issues != null && issues.isEmpty()) {
                resp.getWriter().write("<h3>No such project or project hasn't bugs issues<h3>");
            } else {
                Map<String, Integer> wordsMap = Dictionary.countWords(issues);
                int numOfIssues = issues.size();

                AtomicInteger counter = new AtomicInteger(1);
                wordsMap.forEach((key, value) -> {
                    try {
                        resp.getWriter().write(counter.getAndIncrement() + ". " + key
                                + " - frequency = " + ((double)value / numOfIssues) + "<br>");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            resp.getWriter().write("<h3>You need to do GET request like " +
                    "\"url/jira/plugins/issuesservlet?project_name=YOUR_PROJECT_NAME\"</h3>");
        }
    }

    // This function send JQL Query to JIRA for take issues with type "bug"
    // and returns list of "bug" issues
    private List<Issue> getIssues(String projectName) {
        final String BUGS = "10104";    // This is ID of "Bug" Issue Type

        JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        SearchService searchService = ComponentAccessor.getComponent(SearchService.class);

        ApplicationUser user = authenticationContext.getLoggedInUser();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        Query query = jqlClauseBuilder.project(projectName).and().issueType(BUGS).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();

        SearchResults searchResults = null;
        try {
            searchResults = searchService.search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }

        return searchResults != null ? searchResults.getIssues() : null;
    }
}