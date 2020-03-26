package applitools;

import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.open;

public class LoginTest
    extends BaseTest
{
  @Test
  public void testLogin() {
    open("http://demo.applitools.com/loginBefore.html");

    eyesCheck();
  }
}
