package net.qldarch.security;

interface SessionStore {

  Session newSession(User user);

  void expire(Session session);

  User get(Session session);

}
