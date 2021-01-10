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
import org.obiba.rock.model.RCommand;
import org.obiba.rock.r.ROperation;
import org.obiba.rock.r.ROperationWithResult;

import java.util.Date;

/**
 * A R command is for deferred execution of an ROperation.
 */
public class RServeCommand implements RCommand {
    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }

    private final String id;

    private final ROperation rOperation;

    private Status status;

    private final Date createDate;

    private Date startDate;

    private Date endDate;

    private String error;

    public RServeCommand(String id, ROperation rOperation) {
        this.id = id;
        this.rOperation = rOperation;
        status = Status.PENDING;
        createDate = new Date();
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public ROperation getROperation() {
        return rOperation;
    }

    public String getScript() {
        return rOperation.toString();
    }

    public String getStatus() {
        return status.toString();
    }

    public boolean isFinished() {
        return status == Status.COMPLETED || status == Status.FAILED;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isWithError() {
        return !Strings.isNullOrEmpty(error);
    }

    public String getError() {
        return error;
    }

    public boolean isWithResult() {
        return rOperation instanceof ROperationWithResult && asROperationWithResult().hasResult();
    }

    public ROperationWithResult asROperationWithResult() {
        return (ROperationWithResult) rOperation;
    }

    public void inProgress() {
        status = Status.IN_PROGRESS;
        startDate = new Date();
    }

    public void completed() {
        status = Status.COMPLETED;
        endDate = new Date();
    }

    public void failed(String message) {
        status = Status.FAILED;
        endDate = new Date();
        error = message;
    }

    @Override
    public String toString() {
        return rOperation.toString();
    }
}
