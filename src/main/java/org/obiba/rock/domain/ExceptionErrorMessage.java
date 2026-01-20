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

import com.google.common.collect.Lists;
import org.obiba.rock.model.ErrorMessage;
import org.springframework.http.HttpStatus;

public class ExceptionErrorMessage extends ErrorMessage {

  public ExceptionErrorMessage(String status, Exception exception, String... args) {
    setStatus(status);
    setKey(exception.getClass().getSimpleName());
    // Use generic message to prevent information disclosure
    setMessage("An error occurred");
    setArgs(Lists.newArrayList(args));
  }

  public ExceptionErrorMessage(HttpStatus status, Exception exception, String... args) {
    setStatus(status.value() + "");
    setKey(exception.getClass().getSimpleName());
    // Use generic message to prevent information disclosure
    setMessage("An error occurred");
    setArgs(Lists.newArrayList(args));
  }
}
