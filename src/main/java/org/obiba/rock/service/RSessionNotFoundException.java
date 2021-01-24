/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.service;

public class RSessionNotFoundException extends RuntimeException {

  private final String id;

  public RSessionNotFoundException(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String getMessage() {
    return "No such R session: " + id;
  }
}
