package net.qldarch.security;

import java.util.List;

public interface UserStore {

  public User get(String username);

  public User getByUsernameOrEmail(String usernameOrEmail);

  public User get(Long id);

  public List<User> all();
}
