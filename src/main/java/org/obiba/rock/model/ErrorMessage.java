package org.obiba.rock.model;

public interface ErrorMessage {

    String getStatus();

    String getKey();

    String[] getArgs();

    String getMessage();

}
