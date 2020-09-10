package applitools;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.applitools.ICheckSettings;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
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


  private static String localBranchName;
  
  static {
    String buildNumber = System.getenv("BUILD_NUMBER");
    batch = new BatchInfo((buildNumber != null ? "#" + buildNumber : dateFormat.format(new Date())));
    //BatchInfo batchInfo = new BatchInfo(System.getenv("APPLITOOLS_BATCH_ID"));
    // If the test runs via TeamCity, set the batch ID accordingly.
    String batchId = System.getenv("APPLITOOLS_BATCH_ID");
    if (batchId != null) {
      batch.setId(batchId);
    }
  }
  
  private ThreadLocal<Eyes> myEyes = ThreadLocal.withInitial(() -> {
    Eyes eyes = new Eyes(runner);
    localBranchName = System.getProperty("branchName", System.getenv("GIT_BRANCH_NAME"));
    if (localBranchName == null) {
      localBranchName = "default";
    }
    eyes.setIsDisabled(APPLITOOLS_KEY == null);

    if (!eyes.getIsDisabled()) {
      
      eyes.setBatch(batch);
      eyes.setApiKey(APPLITOOLS_KEY);
  
      //eyes.setLogHandler(new StdoutLogHandler(true));
      eyes.setConfiguration(getConfiguation());
      eyes.setApiKey(APPLITOOLS_KEY);
      //eyes.setBatch(batch);

      //eyes.setBranchName(localBranchName);

      // For local testing or ci runs with master set the branchName and parentBranchName
      if ((batch.getId() != null && "master".equalsIgnoreCase(localBranchName)) || batch.getId() == null) {
        eyes.setBranchName(
            localBranchName.equalsIgnoreCase("master") ? "bmurmistro/applitools-junit/master" : localBranchName);
        eyes.setParentBranchName("default");
      }
      eyes.setIgnoreCaret(true);
    }
    //eyes.setLogHandler(new StdoutLogHandler(true));
    return eyes;
  });

  private static ThreadLocal<String> testName = new ThreadLocal<>();
  
  public Eyes getEyes() {
    return myEyes.get();
  }

  @BeforeMethod
  public void onTestStart(Method m, ITestContext ctx) {
    /*if (!eyes.getIsDisabled() && eyes.getBatch() == null) {
      throw new IllegalArgumentException(
          "The branchName parameter or the Bamboo environment variables are required if visual testing is enabled " +
              "(the applitoolsKey property is provided).");
    }*/
    long id = Thread.currentThread().getId();
    System.out.println("Before test-method. Thread id is: " + id);
    Configuration.browser = "chrome";
    //Configuration.headless = true;
    driver = WebDriverRunner.getAndCheckWebDriver();
    testName.set(getClass().getSimpleName() + "." + m.getName());
  }

  @AfterMethod
  public void onTestFinish() {
    Eyes eyes = getEyes();
    if (eyes != null) {
      try {
        // End visual testing. Validate visual correctness.
        if (eyes.getIsOpen()) {
          eyes.closeAsync();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        // Abort test in case of an unexpected error.
        eyes.abortAsync();
        driver.quit();
      }
    }
    
  }
  
  @AfterSuite
  public void afterSuite(ITestContext context) {
    TestResultsSummary allTestResults = runner.getAllTestResults();
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
    Eyes eyes = getEyes();
    if (!eyes.getIsOpen()) {
      WebDriver remoteDriver = WebDriverRunner.getAndCheckWebDriver();

      if (remoteDriver instanceof WrapsDriver) {
        remoteDriver = ((WrapsDriver) remoteDriver).getWrappedDriver();
      }

      eyes.open(remoteDriver, APPLICATION_NAME, testName.get(), new RectangleSize(800, 600));
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
