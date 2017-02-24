package net.qldarch.hibernate;

import org.hibernate.Session;

public interface HWork<T> {

  public T run(Session session);

}
