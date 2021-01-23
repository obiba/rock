/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import org.obiba.rock.model.RSession;
import org.obiba.rock.r.NoSuchRCommandException;
import org.obiba.rock.r.ROperation;
import org.obiba.rock.r.RScriptROperation;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rserve R session adapter.
 */
public class RServeSession implements RSession {

  private static final Logger log = LoggerFactory.getLogger(RServeSession.class);

  private final String id;

  private final String subject;

  private final Date createDate;

  private Date lastAccessDate;

  private String originalWorkDir;

  private String originalTempDir;

  private boolean busy;

  private final Lock lock = new ReentrantLock();

  private final RConnection rConnection;

  /**
   * R commands to be processed.
   */
  private final BlockingQueue<RServeCommand> rCommandQueue = new LinkedBlockingQueue<>();

  /**
   * All R commands.
   */
  private final List<RServeCommand> rCommandList = Collections.synchronizedList(new LinkedList<RServeCommand>());

  private RCommandsConsumer rCommandsConsumer;

  private Thread consumer;

  /**
   * R command identifier increment.
   */
  private int commandId = 1;

  public RServeSession(String subject, RConnection connection) {
    this.subject = subject;
    this.rConnection = connection;
    this.id = UUID.randomUUID().toString();
    this.createDate = new Date();
    this.lastAccessDate = createDate;

    try {
      originalWorkDir = getRWorkDir();
      originalTempDir = updateRTempDir();
    } catch (Exception e) {
      // ignore
    }
  }

  //
  // Model methods
  //

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public Date getCreateDate() {
    return createDate;
  }

  @Override
  public Date getLastAccessDate() {
    return lastAccessDate;
  }

  @Override
  public boolean isBusy() {
    return busy;
  }

  //
  // Management methods
  //

  @JsonIgnore
  public String getWorkDir() {
    return originalWorkDir;
  }

  @JsonIgnore
  public String getTempDir() {
    return originalTempDir;
  }

  /**
   * Check if the R session is not busy and has expired.
   *
   * @param timeout in minutes
   * @return
   */
  public boolean hasExpired(long timeout) {
    Date now = new Date();
    return !busy && now.getTime() - lastAccessDate.getTime() > timeout * 60 * 1000;
  }

  /**
   * Update last access date.
   */
  public void touch() {
    lastAccessDate = new Date();
  }

  @Override
  public String toString() {
    return id;
  }

  //
  // ROperationTemplate methods
  //

  /**
   * Executes the R operation on the current R session of the invoking Opal user.
   */
  public synchronized void execute(ROperation rop) {
    lock.lock();
    busy = true;
    touch();
    try {
      rop.doWithConnection(rConnection);
    } finally {
      busy = false;
      touch();
      lock.unlock();
    }
  }

  public synchronized String executeAsync(ROperation rop) {
    touch();
    ensureRCommandsConsumer();
    String rCommandId = id + "-" + commandId++;
    RServeCommand cmd = new RServeCommand(rCommandId, rop);
    rCommandList.add(cmd);
    rCommandQueue.offer(cmd);
    return rCommandId;
  }

  @JsonIgnore
  public Iterable<RServeCommand> getRCommands() {
    touch();
    return rCommandList;
  }

  public boolean hasRCommand(String cmdId) {
    touch();
    for (RServeCommand rCommand : rCommandList) {
      if (rCommand.getId().equals(cmdId)) return true;
    }
    return false;
  }

  public RServeCommand getRCommand(String cmdId) {
    touch();
    for (RServeCommand rCommand : rCommandList) {
      if (rCommand.getId().equals(cmdId)) return rCommand;
    }
    throw new NoSuchRCommandException(cmdId);
  }

  public RServeCommand removeRCommand(String cmdId) {
    touch();
    RServeCommand rCommand = getRCommand(cmdId);
    synchronized (rCommand) {
      rCommand.notifyAll();
    }
    rCommandList.remove(rCommand);
    return rCommand;
  }

  /**
   * Close the R session.
   */
  public void close() {
    if (isClosed()) return;

    try {
      cleanRWorkDir();
      cleanRTempDir();
    } catch (Exception e) {
      // ignore
    }

    try {
      rConnection.close();
    } catch (Exception e) {
      // ignore
    }

    if (consumer == null) return;
    try {
      consumer.interrupt();
    } catch (Exception e) {
      // ignore
    } finally {
      consumer = null;
      rCommandList.clear();
      rCommandQueue.clear();
    }
  }

  //
  // private methods
  //

  private boolean isClosed() {
    return !rConnection.isConnected();
  }

  private String getRWorkDir() throws REXPMismatchException {
    RScriptROperation rop = new RScriptROperation("base::getwd()", false);
    execute(rop);
    return rop.getResult().asString();
  }

  private void cleanRWorkDir() {
    if (Strings.isNullOrEmpty(originalWorkDir)) return;
    RScriptROperation rop = new RScriptROperation(String.format("base::unlink('%s', recursive=TRUE)", originalWorkDir), false);
    execute(rop);
  }

  private String updateRTempDir() throws REXPMismatchException {
    RScriptROperation rop = new RScriptROperation("if (!require(unixtools)) { install.packages('unixtools', repos = 'http://www.rforge.net/') }", false);
    execute(rop);
    rop = new RScriptROperation("unixtools::set.tempdir(base::file.path(base::tempdir(), base::basename(base::getwd())))", false);
    execute(rop);
    rop = new RScriptROperation("base::dir.create(base::tempdir(), recursive = TRUE)", false);
    execute(rop);
    rop = new RScriptROperation("base::tempdir()", false);
    execute(rop);
    return rop.getResult().asString();
  }

  private void cleanRTempDir() {
    if (Strings.isNullOrEmpty(originalTempDir)) return;
    RScriptROperation rop = new RScriptROperation(String.format("base::unlink('%s', recursive=TRUE)", originalTempDir), false);
    execute(rop);
  }

  private void ensureRCommandsConsumer() {
    if (rCommandsConsumer == null) {
      rCommandsConsumer = new RCommandsConsumer();
      startRCommandsConsumer();
    } else if (consumer == null || !consumer.isAlive()) {
      startRCommandsConsumer();
    }
  }

  private void startRCommandsConsumer() {
    consumer = new Thread() {
      @Override
      public void run() {
        try {
          rCommandsConsumer.run();
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.error("Error in thread execution", e);
          } else {
            log.error("Error in thread execution: {}", e.getMessage());
          }
        }
      }
    };
    consumer.setName("R Operations Consumer " + rCommandsConsumer);
    consumer.setPriority(Thread.NORM_PRIORITY);
    consumer.start();
  }

  private class RCommandsConsumer implements Runnable {

    @Override
    public void run() {
      log.debug("Starting R operations consumer");
      try {
        //noinspection InfiniteLoopStatement
        while (true) {
          consume(rCommandQueue.take());
        }
      } catch (InterruptedException ignored) {
        log.debug("Stopping R operations consumer");
      } catch (Exception e) {
        log.error("Error in R command consumer", e);
      }
    }

    private void consume(RServeCommand rCommand) {
      try {
        rCommand.inProgress();
        execute(rCommand.getROperation());
        rCommand.completed();
      } catch (Exception e) {
        log.error("Error when consuming R command: {}", e.getMessage(), e);
        rCommand.failed(e.getMessage());
      }
      synchronized (rCommand) {
        rCommand.notifyAll();
      }
    }
  }
}
