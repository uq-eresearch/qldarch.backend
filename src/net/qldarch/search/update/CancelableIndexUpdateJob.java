package net.qldarch.search.update;

public abstract class CancelableIndexUpdateJob implements IndexUpdateJob {

  private volatile boolean canceled = false;

  @Override
  public void cancel() {
    canceled = true;
  }

  boolean isCanceled() {
    return canceled;
  }

}
