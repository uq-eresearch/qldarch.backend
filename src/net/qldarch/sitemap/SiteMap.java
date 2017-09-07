package net.qldarch.sitemap;

import java.io.File;

import net.qldarch.guice.Bind;

import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;

@Bind
public class SiteMap {

  private WebSitemapGenerator wsg;

  public void init(String baseUrl, String location) throws Exception {
    wsg = WebSitemapGenerator.builder(baseUrl, new File(location)).autoValidate(true).build();
  }

  public void addUrl(String url) throws Exception {
    wsg.addUrl(url);
  }

  public void addUrlPriority(String url, Double priority) throws Exception {
    WebSitemapUrl urlWithPriority = new WebSitemapUrl.Options(url).priority(priority).build();
    wsg.addUrl(urlWithPriority);
  }

  public void write() throws Exception {
    wsg.write();
  }

}
