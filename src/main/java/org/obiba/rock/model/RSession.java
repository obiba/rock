package org.obiba.rock.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public interface RSession {

    String getId();

    String getSubject();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    Date getCreateDate();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    Date getLastAccessDate();

    boolean isBusy();

    void close();
}
