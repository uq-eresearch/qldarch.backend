package net.qldarch.security;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import net.qldarch.guice.Bind;
import net.qldarch.hibernate.HS;

@Bind(to=UserStore.class)
public class DbUserStore implements UserStore {

  @Inject
  private HS hs;

  @Override
  public User get(String username) {
    try {
      return hs.execute(session -> session.createQuery("from User where name = :username",
          User.class).setParameter("username", username).getSingleResult());
    } catch(NoResultException e) {
      return null;
    }
  }

  @Override
  public User getByUsernameOrEmail(String usernameOrEmail) {
    try {
      final User u0 = get(usernameOrEmail);
      if(u0 != null) {
        return u0;
      } else {
        try {
          return hs.execute(session -> session.createQuery("from User where email = :email order by id",
              User.class).setParameter("email", usernameOrEmail).setMaxResults(1).getSingleResult());
        } catch(NoResultException e) {
          return null;
        }
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public User get(Long id) {
    return hs.get(User.class, id);
  }

  @Override
  public List<User> all() {
    return hs.execute(session -> session.createQuery("from User", User.class).getResultList()); 
  }

}
