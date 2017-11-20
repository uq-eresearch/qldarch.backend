package net.qldarch.search.update;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;
import net.qldarch.resteasy.Lifecycle;

@Slf4j
@Bind
@Singleton
public class IndexUpdater implements Lifecycle {

  @Inject
  private SearchIndexWriter searchindexwriter;

  private volatile boolean running = false;

  private Thread idxWorkerThread;

  private BlockingQueue<IndexUpdateJob> tasks = new LinkedBlockingQueue<>();

  private class SearchIndexUpdater implements Runnable {

    @Override
    public void run() {
      log.info("started index update worker");
      while(running) {
        try {
          IndexUpdateJob job = tasks.take();
          if(job != null) {
            try {
              job.run(searchindexwriter.getWriter());
            } catch(Exception e) {
              log.warn("update search index failed", e);
            }
          }
        } catch(InterruptedException e) {

        } catch(Throwable t) {
          log.warn("caught exception in index worker loop", t);
        }
      }
      log.info("index update worker exiting");
    }
  }

  @Override
  public synchronized void start() {
    if(!running) {
      running = true;
      final String threadName = "Search-Index-Update";
      idxWorkerThread = new Thread(new SearchIndexUpdater(), threadName);
      idxWorkerThread.start();
    }
  }

  @Override
  public synchronized void stop() {
    running = false;
    if(idxWorkerThread!=null) {
      idxWorkerThread.interrupt();
      try {
        idxWorkerThread.join();
      } catch(Exception e) {}
      idxWorkerThread = null;
    }
  }

  public synchronized boolean addTasks(IndexUpdateJob job) {
    return tasks.offer(job);
  }

}
