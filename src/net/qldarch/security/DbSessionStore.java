package net.qldarch.security;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.guice.Bind;
import net.qldarch.util.M;
import net.qldarch.util.RandomString;

@Bind(to=SessionStore.class)
@Singleton
@Slf4j
public class DbSessionStore implements SessionStore {

  @Inject
  private Db db;

  @Inject
  private RandomString rstr;

  @Inject
  private UserStore users;

  private void removeExpired() {
    try {
      db.execute("delete from session where created < now() - Interval '12 hour'");
    } catch(Exception e) {
      log.debug("remove expired sessions failed", e);
    }
  }

  @Override
  public Session newSession(User user) {
    removeExpired();
    try {
      if(user != null) {
        final Session session = new Session(rstr.next());
        db.execute("insert into session(session, appuser) values(:session, :appuser)",
            M.of("session", session.getSessionId(), "appuser", user.getId()));
        return session;
      }
    } catch(Exception e) {
      log.debug("new session failed for user {}", user, e);
    }
    return null;
  }

  @Override
  public void expire(Session session) {
    removeExpired();
    try {
      if(session != null) {
        db.execute("delete from session where session = :session",
            M.of("session", session.getSessionId()));
      }
    } catch(Exception e) {
      log.debug("expire session {} failed", session, e);
    }
  }

  @Override
  public User get(Session session) {
    removeExpired();
    // TODO use session to user cache here
    try {
      if(session != null) {
        Map<String, Object> row = db.executeQuery("select appuser from session where session = :session",
            M.of("session", session.getSessionId()), Rsc::fetchFirst);
        if(row != null) {
          long userId = ((Number)row.get("appuser")).longValue();
          return users.get(userId);
        }
      }
    } catch(Exception e) {
      log.debug("get session {} failed", e);
    }
    return null;
  }

}
