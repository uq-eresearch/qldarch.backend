package net.qldarch.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.inject.Singleton;

import net.qldarch.guice.Bind;

@Bind(eagerSingleton=true)
@Singleton
public class RandomString {

  private final SecureRandom prng;

  private final MessageDigest sha;

  public RandomString() throws NoSuchAlgorithmException {
    prng = SecureRandom.getInstance("SHA1PRNG");
    sha = MessageDigest.getInstance("SHA-1");
  }

  private String hexEncode(byte[] aInput) {
    StringBuilder result = new StringBuilder();
    char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'};
    for (int idx = 0; idx < aInput.length; ++idx) {
      byte b = aInput[idx];
      result.append(digits[ (b&0xf0) >> 4 ]);
      result.append(digits[ b&0x0f]);
    }
    return result.toString();
  }

  public String next() {
    String randomNum = new Integer(prng.nextInt()).toString();
    byte[] result =  sha.digest(randomNum.getBytes());
    return hexEncode(result);
  }

}
