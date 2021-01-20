package applitools;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.applitools.ICheckSettings;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.StitchMode;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

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
    eyes.setConfiguration(getConfiguation());
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

  public EyesWrapper getEyesWrapper() {
    return myEyesWrapper.get();
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

  public void eyesCheck(ICheckSettings settings) {
    eyesCheck(null, settings);
  }

  /**
   * Convenience method for performing the Applitools validation.
   *
   * @param tag or step name of the validation
   */
  public void eyesCheck(String tag, ICheckSettings settings) {
    EyesWrapper eyesWrapper = getEyesWrapper();
    Eyes eyes = eyesWrapper.getEyes();

    if (!eyesWrapper.isOpenRequested()) {
      WebDriver remoteDriver = WebDriverRunner.getAndCheckWebDriver();
      if (remoteDriver instanceof WrapsDriver) {
        remoteDriver = ((WrapsDriver) remoteDriver).getWrappedDriver();
      }
      eyes.open(remoteDriver, APPLICATION_NAME, testName.get(), new RectangleSize(800, 600));
      eyesWrapper.setOpenRequested(true);
    }
    eyes.check(tag, settings);
  }

  public void eyesCheck() {
    eyesCheck(Target.window());
  }

  private static com.applitools.eyes.selenium.Configuration getConfiguation() {
    com.applitools.eyes.selenium.Configuration sconf = new com.applitools.eyes.selenium.Configuration();


    // Set a batch name so all the different browser and mobile combinations are
    // part of the same batch
    sconf.setBatch(batch);
    sconf.setApiKey(APPLITOOLS_KEY);
    sconf.setStitchMode(StitchMode.CSS);

    // Add Chrome browsers with different Viewports
    sconf.addBrowser(800, 600, BrowserType.CHROME);
    sconf.addBrowser(700, 500, BrowserType.CHROME);
    sconf.addBrowser(1200, 800, BrowserType.CHROME);
    sconf.addBrowser(1600, 1200, BrowserType.CHROME);
    sconf.addBrowser(700, 800, BrowserType.CHROME);
    sconf.addBrowser(800, 700, BrowserType.CHROME);
    sconf.addBrowser(1200, 900, BrowserType.CHROME);
    sconf.addBrowser(1600, 1000, BrowserType.CHROME);

    // Add Firefox browser with different Viewports
    sconf.addBrowser(800, 600, BrowserType.FIREFOX);
    sconf.addBrowser(700, 500, BrowserType.FIREFOX);
    sconf.addBrowser(1200, 800, BrowserType.FIREFOX);
    sconf.addBrowser(1600, 1200, BrowserType.FIREFOX);
    sconf.addBrowser(700, 800, BrowserType.FIREFOX);
    sconf.addBrowser(800, 700, BrowserType.FIREFOX);
    sconf.addBrowser(1200, 900, BrowserType.FIREFOX);
    sconf.addBrowser(1600, 1000, BrowserType.FIREFOX);

    sconf.addBrowser(1600, 1200, BrowserType.IE_11);
    sconf.addBrowser(800, 600, BrowserType.IE_10);

    sconf.addBrowser(800, 600, BrowserType.EDGE_CHROMIUM);

    sconf.addBrowser(800, 600, BrowserType.SAFARI);
    sconf.addBrowser(800, 600, BrowserType.SAFARI_TWO_VERSIONS_BACK);

    // Add iPhone 4 device emulation
    sconf.addDeviceEmulation(DeviceName.iPad_Mini);

    return sconf;
  }
}
