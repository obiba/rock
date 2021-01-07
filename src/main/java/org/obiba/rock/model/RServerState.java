package org.obiba.rock.model;

/**
 * State of the server: running status and how to connect it.
 */
public interface RServerState {

    boolean isRunning();

    Integer getPort();

    String getEncoding();

    String getCluster();
}
