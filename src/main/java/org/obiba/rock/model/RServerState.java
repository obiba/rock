/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.model;

import java.util.List;

/**
 * State of the server: running status and how to connect it.
 */
public interface RServerState {

    String getId();

    boolean isRunning();

    String getEncoding();

    List<String> getTags();

    String getVersion();
}
