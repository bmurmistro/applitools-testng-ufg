package applitools;

import com.applitools.eyes.selenium.fluent.Target;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.open;

public class SplunkTest extends BaseTest
{
  @Factory(dataProvider="environments")
  public SplunkTest(String type, int width, int height) {
    super(type, width, height);
  }

  @Test
  public void testSplunkHome() throws InterruptedException {
    open("https://www.splunk.com/");
    eyesCheck(Target.window().fully());
  }

  @Test
  public void testSplunkSecurity() throws InterruptedException {
    open("https://www.splunk.com/en_us/cyber-security.html");
    eyesCheck(Target.window().fully());
  }
}
