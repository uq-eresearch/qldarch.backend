package net.qldarch.security;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.message.MessageContactResponse;
import net.qldarch.util.M;

@Path("/signup")
@Slf4j
public class WsSignUp {

  @Inject
  private SignUp signup;

  @Inject
  private Recaptcha recaptcha;

  @POST
  @Produces(ContentType.JSON)
  public SignUpResponse signup(@FormParam("email") String email, @FormParam("displayname") String displayname,
      @FormParam("password") String password, @FormParam("g-recaptcha-response") String gRecaptchaResponse) {
    log.info("sign-up email {}", email);
    try {
      Response verRecaptcha = recaptcha.verifyRecaptcha(gRecaptchaResponse);
      int status = verRecaptcha.getStatus();
      if(status != 200) {
        log.error("failed : http error code : {}", status);
        verRecaptcha.close();
        return SignUpResponse.failed("verifiying recaptcha failed : http error code : " + String.valueOf(status));
      } else if(verRecaptcha.hasEntity()) {
        try {
          String strRecaptcha = verRecaptcha.readEntity(String.class);
          log.info("json response: {}", strRecaptcha);
          JSONObject jsonRecaptcha = new JSONObject(strRecaptcha);
          if(jsonRecaptcha.getBoolean("success")) {
            verRecaptcha.close();
            return signup.signup(email, displayname, password);
          } else {
            verRecaptcha.close();
            return SignUpResponse.failed("Verifiying recaptcha unsuccessful");
          }
        } catch(JSONException e) {
          verRecaptcha.close();
          return SignUpResponse.failed("Caught JSONException: " + e.toString());
        }
      } else {
        verRecaptcha.close();
        return SignUpResponse.failed("Verifiying recaptcha failed");
      }
    } catch(Exception e) {
      return SignUpResponse.failed(e.toString());
    }
  }
}
