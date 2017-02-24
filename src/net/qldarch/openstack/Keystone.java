package net.qldarch.openstack;

import javax.inject.Singleton;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;
import net.qldarch.util.M;

@Bind
@Singleton
@Slf4j
public class Keystone {

  public String signin(String tenant, String user, String password) {
    try {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpPost post = new HttpPost("https://keystone.rc.nectar.org.au:5000/v2.0/tokens");
      post.addHeader("Content-Type", "application/json");
      post.setEntity(new StringEntity(new Gson().toJson(M.of("auth", M.of("tenantName", tenant,
          "passwordCredentials", M.of("username", user, "password", password))))));
      try(CloseableHttpResponse response = httpclient.execute(post)) {
        log.debug("keystone signin request returned with status line '{}'", response.getStatusLine());
        HttpEntity entity = response.getEntity();
        try {
          return JsonPath.parse(entity.getContent()).read("$.access.token.id");
        } finally {
          EntityUtils.consume(entity);
        }
      }
    } catch(Exception e) {
      log.error("failed to signin to openstack with user '{}'", user, e);
      throw new RuntimeException("failed to signin to openstack, see logfile");
    }
  }

  public boolean valid(String token) {
    try {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet get = new HttpGet("https://keystone.rc.nectar.org.au:5000/v2.0/tenants");
      get.addHeader("X-Auth-Token", token);
      try(CloseableHttpResponse response = httpclient.execute(get)) {
        try {
          final int sc = response.getStatusLine().getStatusCode();
          if(sc == 200) {
            return true;
          } else if(sc != 401) {
            log.debug("token valiation request returned with status line '{}'", response.getStatusLine());
          }
        } finally {
          EntityUtils.consume(response.getEntity());
        }
      }
    } catch(Exception e) {
      log.error("failed to validate token", e);
    }
    return false;
  }
}
