package net.qldarch.search.update;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;
import net.qldarch.resteasy.Lifecycle;
import net.qldarch.search.Index;

@Slf4j
@Bind
@Singleton
public class IndexUpdater implements Lifecycle {

  @Inject
  private Index index;

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
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try(Directory directory = index.directory()) {
              try(IndexWriter writer = new IndexWriter(directory, config)) {
                job.run(writer);
              } catch(Exception e) {
                log.warn("update search index failed", e);
              }
            } catch(IOException e) {
              log.warn("failed to open search directory", e);
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
