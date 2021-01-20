package applitools;

import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class HelloWorldTest extends BaseTest
{
  @Test
  public void testHelloWorld() {
    open("https://applitools.com/helloworld");

    // Visual validation point #1.
    eyesCheck("Hello!", Target.window().fully());

    // Click the "Click me!" button.
    $(By.tagName("button")).click();

    eyesCheck("Hello Thumbs up!", Target.window().fully());
  }

  public void testHelloWorldDiff1() {
    open("https://applitools.com/helloworld/?diff1");

    eyesCheck(Target.window().fully());
  }

  public void testHelloWorldDiff2() {
    open("https://applitools.com/helloworld/?diff1");

    eyesCheck(Target.window().fully());
  }

  @Test
  public void testNoOp() throws InterruptedException {
  }
}
