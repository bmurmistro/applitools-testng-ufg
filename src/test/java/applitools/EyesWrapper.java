package applitools;

import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.visualgrid.services.RunnerOptions;

public class EyesWrapper
{
  private Eyes eyes;

  private final EyesRunner runner;

  private boolean openRequested;

  public EyesWrapper(Eyes eyes, EyesRunner runner) {
    this.eyes =eyes;
    this.runner = runner;
  }

  public Eyes getEyes() {
    return eyes;
  }

  public void setEyes(Eyes eyes) {
    this.eyes = eyes;
  }

  public EyesRunner getRunner() {
    return runner;
  }

  public boolean isOpenRequested() {
    return openRequested;
  }

  public void setOpenRequested(final boolean openRequested) {
    this.openRequested = openRequested;
  }
}
