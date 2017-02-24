package net.qldarch.openstack;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.configuration.Cfg;
import net.qldarch.guice.Bind;

@Bind
@Singleton
@Slf4j
public class ObjectStore {

  @Inject @Cfg("os.container.url")
  private String objectContainerUrl;

  private String containerUrl() {
    return objectContainerUrl;
  }

  private String getHeader(String name, HttpResponse response) {
    Header[] header = response.getHeaders(name);
    if((header != null) && header.length >= 1) {
      return header[0].getValue();
    } else {
      return null;
    }
    
  }

  private Long getHeaderAsLong(String name, HttpResponse response) {
    try {
      return new Long(getHeader(name, response));
    } catch(Exception e) {
      return null;
    }
  }

  public FileInfo info(final String token, final String name) {
    final String url = containerUrl() + "/" + name;
    try {
      final CloseableHttpClient httpclient = HttpClients.createDefault();
      final HttpHead head = new HttpHead(url);
      head.addHeader("X-Auth-Token", token);
      try(CloseableHttpResponse response = httpclient.execute(head)) {
        try {
          final int sc = response.getStatusLine().getStatusCode();
          if(sc == 200) {
            return new FileInfo(null, name, getHeaderAsLong("Content-Length", response), 
                getHeader("Content-Type", response), getHeader("Etag", response)); 
          } else if(sc != 404) {
            log.debug("object store head request for '{}'returned with status line '{}'",
                url, response.getStatusLine());
          }
        } finally {
          EntityUtils.consume(response.getEntity());
        }
      }
      return null;
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to retrieve head request with url '%s'", url), e);
    }
  }

  public boolean upload(final String token, final FileInfo file, final InputStream content) {
    final String url = containerUrl() + "/" + file.name;
    try {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpPut put = new HttpPut(url);
      put.addHeader("X-Auth-Token", token);
      put.addHeader("Content-Type", file.mimetype);
      put.setEntity(new InputStreamEntity(content));
      try(CloseableHttpResponse response = httpclient.execute(put)) {
        try {
          final int sc = response.getStatusLine().getStatusCode();
          if(sc == 201) {
            return true;
          } else {
            log.debug("object store put request with url '{}' returned with status line '{}'",
                url, response.getStatusLine());
          }
        } finally {
          EntityUtils.consume(response.getEntity());
        }
      }
      return false;
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to upload file to object store with url '{}'", url), e);
    }
  }

  public String getLocation(String path) {
    return containerUrl() + "/" + path;
  }
}
