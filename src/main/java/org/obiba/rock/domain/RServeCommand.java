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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.obiba.rock.model.RCommand;
import org.obiba.rock.r.ROperation;
import org.obiba.rock.r.ROperationWithResult;

import java.util.Date;

/**
 * A R command is for deferred execution of an ROperation.
 */
public class RServeCommand extends RCommand {
  public enum Status {
    PENDING, IN_PROGRESS, COMPLETED, FAILED
  }

  private final ROperation rOperation;

  public RServeCommand(String sessionId, String id, ROperation rOperation) {
    setId(id);
    setSessionId(sessionId);
    this.rOperation = rOperation;
    setStatus(Status.PENDING.name());
    setCreatedDate(new Date());
  }

  @JsonIgnore
  public ROperation getROperation() {
    return rOperation;
  }

  public String getScript() {
    return rOperation.toString();
  }

  @JsonProperty("withResult")
  @Override
  public Boolean getWithResult() {
    return rOperation instanceof ROperationWithResult && asROperationWithResult().hasResult();
  }

  public ROperationWithResult asROperationWithResult() {
    return (ROperationWithResult) rOperation;
  }

  public void inProgress() {
    setStatus(Status.IN_PROGRESS.name());
    setStartDate(new Date());
  }

  public void completed() {
    setStatus(Status.COMPLETED.name());
    setEndDate(new Date());
    setFinished(true);
  }

  public void failed(String message) {
    setStatus(Status.FAILED.name());
    setEndDate(new Date());
    setError(message);
    setWithError(true);
    setFinished(true);
  }
}
