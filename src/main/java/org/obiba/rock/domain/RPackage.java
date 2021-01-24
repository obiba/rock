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

import com.google.common.collect.Maps;

import java.util.Map;

public class RPackage {
  private String name;
  private Map<String, String> fields;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public void putField(String key, String value) {
    if (fields == null) fields = Maps.newLinkedHashMap();
    fields.put(key, value);
  }
}
