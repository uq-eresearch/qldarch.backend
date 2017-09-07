package net.qldarch.sitemap;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.architect.WsArchitects;
import net.qldarch.configuration.Cfg;
import net.qldarch.firm.WsFirms;
import net.qldarch.interview.WsInterviewsBrief;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.User;
import net.qldarch.structure.WsStructures;
import net.qldarch.text.WsArticles;
import net.qldarch.util.M;

@Slf4j
@Path("sitemap")
public class WsSiteMap {

  @Inject
  @Nullable
  private User user;

  @Inject
  private WsArchitects wsarchitects;

  @Inject
  private WsFirms wsfirms;

  @Inject
  private WsStructures wsstructures;

  @Inject
  private WsArticles wsarticles;

  @Inject
  private WsInterviewsBrief wsinterviewsbrief;

  @Inject
  @Cfg("baseUrl")
  private String baseUrl;

  @Inject
  @Cfg("frontend")
  private String frontend;

  @Inject
  private SiteMap sitemap;

  @POST
  @Produces(ContentType.JSON)
  public Response generate() {
    try {
      if(user != null && user.isAdmin()) {
        List<Map<String, Object>> architects = wsarchitects.get();
        List<Map<String, Object>> firms = wsfirms.get();
        List<Map<String, Object>> structures = wsstructures.get();
        List<Map<String, Object>> articles = wsarticles.articles();
        List<Map<String, Object>> interviews = wsinterviewsbrief.interviews();

        sitemap.init(baseUrl, frontend);
        sitemap.addUrlPriority(baseUrl, 1.0);

        // Frontend's Urls
        sitemap.addUrlPriority(baseUrl + "architects", 0.7);
        sitemap.addUrl(baseUrl + "architects/other");
        sitemap.addUrlPriority(baseUrl + "firms", 0.7);
        sitemap.addUrl(baseUrl + "firms/other");
        sitemap.addUrlPriority(baseUrl + "firms/timeline", 0.9);
        sitemap.addUrlPriority(baseUrl + "projects", 0.7);
        sitemap.addUrl(baseUrl + "projects/other");
        sitemap.addUrlPriority(baseUrl + "articles", 0.9);
        sitemap.addUrlPriority(baseUrl + "interviews", 0.9);
        sitemap.addUrlPriority(baseUrl + "projects?index=%23", 0.8);
        sitemap.addUrlPriority(baseUrl + "projects/other?index=%23", 0.8);
        for(char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
          sitemap.addUrlPriority(baseUrl + "architects?index=" + alphabet, 0.8);
          sitemap.addUrlPriority(baseUrl + "architects/other?index=" + alphabet, 0.6);
          sitemap.addUrlPriority(baseUrl + "firms?index=" + alphabet, 0.8);
          sitemap.addUrlPriority(baseUrl + "firms/other?index=" + alphabet, 0.6);
          sitemap.addUrlPriority(baseUrl + "firms/timeline?index=" + alphabet, 0.7);
          sitemap.addUrlPriority(baseUrl + "projects?index=" + alphabet, 0.8);
          sitemap.addUrlPriority(baseUrl + "projects/other?index=" + alphabet, 0.6);
        }
        for(Map<String, Object> architect : architects) {
          sitemap.addUrlPriority(baseUrl + "architect/summary?architectId=" + architect.get("id"), 1.0);
          sitemap.addUrl(baseUrl + "architect/photographs?architectId=" + architect.get("id"));
          sitemap.addUrl(baseUrl + "architect/employers?architectId=" + architect.get("id"));
          sitemap.addUrl(baseUrl + "architect/structures?architectId=" + architect.get("id"));
          sitemap.addUrl(baseUrl + "architect/articles?architectId=" + architect.get("id"));
          sitemap.addUrl(baseUrl + "architect/relationships?architectId=" + architect.get("id"));
          sitemap.addUrl(baseUrl + "architect/timeline?architectId=" + architect.get("id"));
        }
        for(Map<String, Object> firm : firms) {
          sitemap.addUrlPriority(baseUrl + "firm/summary?firmId=" + firm.get("id"), 1.0);
          sitemap.addUrl(baseUrl + "firm/photographs?firmId=" + firm.get("id"));
          sitemap.addUrl(baseUrl + "firm/interviews?firmId=" + firm.get("id"));
          sitemap.addUrl(baseUrl + "firm/employees?firmId=" + firm.get("id"));
          sitemap.addUrl(baseUrl + "firm/projects?firmId=" + firm.get("id"));
          sitemap.addUrl(baseUrl + "firm/articles?firmId=" + firm.get("id"));
          sitemap.addUrl(baseUrl + "firm/relationships?firmId=" + firm.get("id"));
          sitemap.addUrl(baseUrl + "firm/timeline?firmId=" + firm.get("id"));
        }
        for(Map<String, Object> structure : structures) {
          sitemap.addUrlPriority(baseUrl + "project/summary?structureId=" + structure.get("id"), 1.0);
          sitemap.addUrl(baseUrl + "project/photographs?structureId=" + structure.get("id"));
          sitemap.addUrl(baseUrl + "project/line-drawings?structureId=" + structure.get("id"));
          sitemap.addUrl(baseUrl + "project/interviews?structureId=" + structure.get("id"));
          sitemap.addUrl(baseUrl + "project/articles?structureId=" + structure.get("id"));
          sitemap.addUrl(baseUrl + "project/relationships?structureId=" + structure.get("id"));
          sitemap.addUrl(baseUrl + "project/timeline?structureId=" + structure.get("id"));
        }
        for(Map<String, Object> article : articles) {
          sitemap.addUrlPriority(baseUrl + "article?articleId=" + article.get("id"), 1.0);
        }
        for(Map<String, Object> interview : interviews) {
          sitemap.addUrlPriority(baseUrl + "interview/" + interview.get("interview"), 1.0);
        }

        sitemap.write();
        return Response.ok().entity(M.of("baseUrl", baseUrl, "location", frontend)).build();
      } else {
        log.debug("user {} is not admin", user);
      }
    } catch(Exception e) {
      log.debug("generating sitemap failed for user {} ", user, e);
    }
    return null;
  }
}
