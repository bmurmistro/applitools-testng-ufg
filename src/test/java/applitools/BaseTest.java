package applitools;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.applitools.ICheckSettings;
import com.applitools.eyes.*;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.StitchMode;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.IosDeviceInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceName;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.testng.ITestContext;
import org.testng.annotations.*;

public class BaseTest
{
  private static VisualGridRunner runner = new VisualGridRunner(10);

  private static BatchInfo batch;

  private static final String APPLITOOLS_KEY =
      System.getProperty("APPLITOOLS_API_KEY", System.getenv("APPLITOOLS_API_KEY"));

  private static final String APPLICATION_NAME = System.getProperty("applicationName", "Applitools Test App");

  private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  protected WebDriver driver;

  //private static EyesWrapper eyesWrapper;
  private static ThreadLocal<EyesWrapper> myEyesWrapper = ThreadLocal.withInitial(() -> {
    Eyes eyes = new Eyes(runner);
    eyes.setLogHandler(new FileLogger("splunkLog11.txt", true, true));
    //eyes.setConfiguration(getConfiguation());
    EyesWrapper eyesWrapper = new EyesWrapper(eyes, runner);

    return eyesWrapper;
  });
  static {
    String buildNumber = System.getenv("BUILD_NUMBER");
    batch = new BatchInfo((buildNumber != null ? "#" + buildNumber : dateFormat.format(new Date())));
    // If the test runs via TeamCity, set the batch ID accordingly.
    String batchId = System.getenv("APPLITOOLS_BATCH_ID");
    if (batchId != null) {
      batch.setId(batchId);
    }
  }

  private static ThreadLocal<String> testName = new ThreadLocal<>();
  private String environmentType;
  private Integer width;
  private Integer height;


  public EyesWrapper getEyesWrapper() {
    return myEyesWrapper.get();
  }

  private static Object[][] environments = new Object[][] {
            new Object[] { "Desktop", 1480, 1200 },
            new Object[] { "Tablet", 768, 1024 },
            new Object[] { "Mobile", 375, 812 }
    };

  @DataProvider(name = "environments")
  public static Object[][] getEnvironments() {
    return environments;
  }

  @Factory (dataProvider="environments")
  public BaseTest(String type, int width, int height){
    this.environmentType = type;
    this.width = width;
    this.height = height;
    System.out.println(type);
    System.out.println(width);
    System.out.println(height);
  }

  @BeforeMethod
  public void onTestStart(Method m, ITestContext ctx) {
    long id = Thread.currentThread().getId();
    System.out.println("Before test-method. Thread id is: " + id);
    Configuration.browser = "chrome";
    //Configuration.headless = true;
    driver = WebDriverRunner.getAndCheckWebDriver();
    testName.set(getClass().getSimpleName() + "." + m.getName());
  }

  @AfterMethod
  public void onTestFinish() {
    EyesWrapper eyesWrapper = getEyesWrapper();
    Eyes eyes = eyesWrapper.getEyes();
    try {
      // End visual testing. Validate visual correctness.
      if (getEyesWrapper().isOpenRequested()) {
        eyes.closeAsync();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      eyesWrapper.setOpenRequested(false);
      // Abort test in case of an unexpected error.
      eyes.abortAsync();
      //driver.quit();
    }
  }
  
  @AfterSuite
  public void afterSuite(ITestContext context) {
    TestResultsSummary results = runner.getAllTestResults(false);
    System.out.println(results);
    driver.quit();
  }

  public void eyesCheck(ICheckSettings settings) throws InterruptedException {
    eyesCheck(null, settings);
  }

  /**
   * Convenience method for performing the Applitools validation.
   *
   * @param tag or step name of the validation
   */
  public void eyesCheck(String tag, ICheckSettings settings) throws InterruptedException {
    EyesWrapper eyesWrapper = getEyesWrapper();
    Eyes eyes = eyesWrapper.getEyes();
    eyes.setConfiguration(getConfiguation(environmentType));
    if (!eyesWrapper.isOpenRequested()) {
      WebDriver remoteDriver = WebDriverRunner.getAndCheckWebDriver();
      if (remoteDriver instanceof WrapsDriver) {
        remoteDriver = ((WrapsDriver) remoteDriver).getWrappedDriver();
      }
      eyes.open(remoteDriver, APPLICATION_NAME, testName.get(), new RectangleSize(width, height));
      eyesWrapper.setOpenRequested(true);
    }
    Thread.sleep(8000);
    ((JavascriptExecutor)driver).executeScript("var currScrollPosition = 0; var interval = setInterval(function() {let scrollPosition = document.documentElement.scrollTop;currScrollPosition += 300;window.scrollTo(0, currScrollPosition);if (scrollPosition === document.documentElement.scrollTop) {clearInterval(interval);window.scrollTo(0,0);}},100);var el = document.getElementsByClassName('drift-frame-controller'); if (el && el[0]) { el[0].style.display = 'none'} ; el = document.getElementById('onetrust-banner-sdk'); if (el) { el.style.display = 'none'; }");
    Thread.sleep(5000);
    eyes.check(tag, settings);
  }

  public void eyesCheck() throws InterruptedException {
    eyesCheck(Target.window());
  }

  private com.applitools.eyes.selenium.Configuration getConfiguation(String environmentType) {
    com.applitools.eyes.selenium.Configuration sconf = new com.applitools.eyes.selenium.Configuration();


    // Set a batch name so all the different browser and mobile combinations are
    // part of the same batch
    sconf.setBatch(batch);
    sconf.setApiKey(APPLITOOLS_KEY);
    sconf.setStitchMode(StitchMode.CSS);
    if (environmentType.equalsIgnoreCase("desktop")) {
      // Add Chrome browsers with different Viewports
      sconf.addBrowser(1480, 1200, BrowserType.CHROME);
      sconf.addBrowser(1480, 1200, BrowserType.FIREFOX);
      sconf.addBrowser(1480, 1200, BrowserType.EDGE_CHROMIUM);
      sconf.addBrowser(1480, 1200, BrowserType.SAFARI);
    }
    else if (environmentType.equalsIgnoreCase("tablet")) {
      sconf.addBrowser(new IosDeviceInfo(IosDeviceName.iPad_7));
    }
    else {
      sconf.addBrowser(new IosDeviceInfo(IosDeviceName.iPhone_X));
    }
    return sconf;
  }
}
