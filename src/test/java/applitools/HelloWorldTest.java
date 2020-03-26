package applitools;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class HelloWorldTest extends BaseTest
{
  @Test
  public void testHello() {
    open("https://applitools.com/helloworld");

    // Visual validation point #1.
    eyesCheck("Hello!");

    // Click the "Click me!" button.
    $(By.tagName("button")).click();

    eyesCheck("Hello Thumbs up!");
  }
}
