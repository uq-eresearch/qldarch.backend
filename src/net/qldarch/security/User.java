package net.qldarch.security;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.qldarch.gson.JsonExclude;

@Entity
@Table(name="appuser")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="username")
@ToString(of="username")
@Data
public class User {

  @Id
  private Long id;

  @Column(name="name")
  private String username;

  private String email;

  private String displayName;

  @JsonExclude
  private String password;

  private String role;

  @JsonExclude
  @Column(name="signin")
  private boolean signInAllowed;

  @JsonExclude
  private Timestamp activated;

  private boolean contact;

  @JsonExclude
  private String activation;

  public boolean isActivated() {
    return getActivated() != null;
  }

  public boolean isAdmin() {
    return StringUtils.equals("admin", role);
  }

  public boolean isEditor() {
    return StringUtils.equals("editor", role);
  }

  public boolean isReader() {
    return StringUtils.equals("reader", role);
  }
}
