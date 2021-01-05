package org.obiba.rock.model;

import java.util.Date;

public interface RCommand {

    String getId();

    String getStatus();

    boolean isFinished();

    Date getCreateDate();

    Date getStartDate();

    Date getEndDate();

    boolean isWithError();

    String getError();

    boolean isWithResult();
}