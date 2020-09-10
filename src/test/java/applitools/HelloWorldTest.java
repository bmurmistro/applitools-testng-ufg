package applitools;

import com.applitools.eyes.selenium.fluent.Target;
import org.apache.tools.ant.taskdefs.Tar;
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
    eyesCheck("Hello!", Target.window().fully());

    // Click the "Click me!" button.
    //$(By.tagName("button")).click();

    eyesCheck("Hello Thumbs up!", Target.window().fully());
  }

  @Test
  public void testHello1() {
  }

  @Test
  public void testHello2() {
  }

  @Test
  public void testHello3() {
  }

  @Test
  public void testHello4() {
  }

  @Test
  public void testHello5() {
  }

  @Test
  public void testHello6() {
  }

  @Test
  public void testHello7() {
  }
  
}
