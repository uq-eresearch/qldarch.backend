package net.qldarch.security;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.jaxrs.ContentType;

@Path("/signup")
@Slf4j
public class WsSignUp {

  @Inject
  private SignUp signup;

  @POST
  @Produces(ContentType.JSON)
  public SignUpResponse signup(@FormParam("email") String email,
      @FormParam("displayname") String displayname, @FormParam("password") String password) {
    log.info("sign-up email {}", email);
    return signup.signup(email, displayname, password);
  }
}
