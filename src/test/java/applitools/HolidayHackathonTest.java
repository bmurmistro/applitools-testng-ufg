package applitools;

import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class HolidayHackathonTest extends BaseTest
{
  @Test
  public void testMainPage() throws InterruptedException{
    open("https://demo.applitools.com/tlcHackathonMasterV1.html");

    eyesCheck(Target.window().fully());
  }

  @Test
  public void testFilter() throws InterruptedException {
    open("https://demo.applitools.com/tlcHackathonMasterV1.html");

    $(By.id("colors__Black")).click();
    $(By.id("filterBtn")).click();

    eyesCheck(Target.window().fully());
  }

  @Test
  public void testNoOp() throws InterruptedException {
  }
}
