package net.qldarch.resteasy;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;

@Bind
@Singleton
@Slf4j
public class ExceptionLogFilter implements Filter {

  @Override
  public void init(FilterConfig arg0) throws ServletException {}

  @Override
  public void destroy() {}

  private Throwable getRootCause(Throwable e) {
    if(e == null) {
      return null;
    }
    if(e.getCause() == null) {
      return e;
    } else {
      return getRootCause(e.getCause());
    }
  }

  private boolean logException(Exception e) {
    Throwable t = getRootCause(e);
    if(t!=null) {
      final String msg = t.getMessage();
      if("Broken pipe".equals(msg)) {
        return false;
      } else if("Connection reset by peer".equals(msg)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch(IOException|ServletException|RuntimeException e) {
      if(logException(e)) {
        final HttpServletRequest hreq = (HttpServletRequest)request;
        if(hreq.getQueryString() != null) {
          log.debug("exception while executing request '{}?{}'",
              hreq.getRequestURI(), hreq.getQueryString(), e);
        } else {
          log.debug("exception while executing request '{}'", hreq.getRequestURI(), e);
        }
      }
      throw e;
    }
  }

}
