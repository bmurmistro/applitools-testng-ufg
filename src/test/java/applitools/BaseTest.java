package applitools;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public class BaseTest
{
  public static Eyes eyes = new Eyes();

  private String testName;

  private static BatchInfo batchInfo;

  private static final String APPLITOOLS_KEY =
      System.getProperty("APPLITOOLS_API_KEY", System.getenv("APPLITOOLS_API_KEY"));

  private static final String APPLICATION_NAME = System.getProperty("applicationName", "Applitools Test App");

  private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  protected static WebDriver driver;

  static {
    String localBranchName = System.getProperty("branchName", System.getenv("GIT_BRANCH_NAME"));
    if (localBranchName == null) {
      localBranchName = "default";
    }
    eyes.setIsDisabled(APPLITOOLS_KEY == null);

    if (!eyes.getIsDisabled()) {
      String buildNumber = System.getenv("BUILD_NUMBER");
      batchInfo = new BatchInfo(
          (buildNumber != null ? "#" + buildNumber : dateFormat.format(new Date())) + " " + localBranchName);
      //BatchInfo batchInfo = new BatchInfo(System.getenv("APPLITOOLS_BATCH_ID"));
      // If the test runs via TeamCity, set the batch ID accordingly.
      String batchId = System.getenv("APPLITOOLS_BATCH_ID");
      if (batchId != null) {
        batchInfo.setId(batchId);
      }
      eyes.setBatch(batchInfo);

      eyes.setApiKey(APPLITOOLS_KEY);

      // For local testing or ci runs with master set the branchName and parentBranchNam
      if ((batchId != null && "master".equalsIgnoreCase(localBranchName)) || batchId == null) {
        eyes.setBranchName(
            localBranchName.equalsIgnoreCase("master") ? "bmurmistro/applitools-testng/master" : localBranchName);
        eyes.setParentBranchName(System.getProperty("parentBranchName", "bmurmistro/applitools-testng/master"));
      }
      eyes.setIgnoreCaret(true);
    }
    eyes.setLogHandler(new StdoutLogHandler(true));
  }

  @BeforeClass
  public static void setUp() {
    //ChromeOptions options = new ChromeOptions();
    //options.addArguments("--headless");
    Configuration.browser = "chrome";
    //Configuration.headless = true;
    driver = WebDriverRunner.getAndCheckWebDriver();
  }

  @AfterClass
  public static void tearDown() {
    driver.quit();
  }

  public void eyesCheck() {
    eyesCheck(null);
  }

  @BeforeMethod
  public void onTestStart(Method m, ITestContext ctx) {
    if (!eyes.getIsDisabled() && eyes.getBatch() == null) {
      throw new IllegalArgumentException(
          "The branchName parameter or the Bamboo environment variables are required if visual testing is enabled " +
              "(the applitoolsKey property is provided).");
    }
    testName = getClass().getSimpleName() + "." + m.getName();
  }

  @AfterMethod
  public void onTestFinish() {
    try {
      // End visual testing. Validate visual correctness.
      if (eyes.getIsOpen()) {
        eyes.close(true);
      }
    }
    finally {
      testName = null;
      // Abort test in case of an unexpected error.
      eyes.abortIfNotClosed();
    }
  }

  /**
   * Convenience method for performing the Applitools validation.
   *
   * @param tag or step name of the validation
   */
  public void eyesCheck(String tag) {
    if (!eyes.getIsOpen()) {
      WebDriver remoteDriver = WebDriverRunner.getAndCheckWebDriver();

      if (remoteDriver instanceof WrapsDriver) {
        remoteDriver = ((WrapsDriver) remoteDriver).getWrappedDriver();
      }

      eyes.open(remoteDriver, APPLICATION_NAME, testName, new RectangleSize(800, 600));
    }
    eyes.check(tag, Target.window());
  }
}
