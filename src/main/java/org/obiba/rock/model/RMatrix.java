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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import java.util.List;

public interface RMatrix<T> {

  @JsonIgnore
  int getColumnCount();

  String[] getColumnNames();

  String getColumnName(int idx);

  int getColumnIndex(String name);

  @JsonIgnore
  int getRowCount();

  String[] getRowNames();

  String getRowName(int idx);

  int getRowIndex(String name);

  Iterable<T[]> iterateRows();

  Iterable<T[]> iterateColumns();

  default List<T[]> getRows() {
    return Lists.newArrayList(iterateRows());
  }
}
