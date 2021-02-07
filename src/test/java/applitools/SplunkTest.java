package applitools;

import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.open;

public class SplunkTest extends BaseTest
{
  @Test
  public void testSplunkHome() throws InterruptedException {
    open("https://www.splunk.com/");
    ((JavascriptExecutor)driver).executeScript("var currScrollPosition = 0; var interval = setInterval(function() {let scrollPosition = document.documentElement.scrollTop;currScrollPosition += 300;window.scrollTo(0, currScrollPosition);if (scrollPosition === document.documentElement.scrollTop) {clearInterval(interval);window.scrollTo(0,0);}},100);");
    Thread.sleep(5000);
    eyesCheck(Target.window().fully());
  }

  @Test
  public void testSplunkSecurity() throws InterruptedException {
    open("https://www.splunk.com/en_us/cyber-security.html");
    ((JavascriptExecutor)driver).executeScript("var currScrollPosition = 0; var interval = setInterval(function() {let scrollPosition = document.documentElement.scrollTop;currScrollPosition += 300;window.scrollTo(0, currScrollPosition);if (scrollPosition === document.documentElement.scrollTop) {clearInterval(interval);window.scrollTo(0,0);}},100);");
    Thread.sleep(5000);
    eyesCheck(Target.window().fully());
  }
}
