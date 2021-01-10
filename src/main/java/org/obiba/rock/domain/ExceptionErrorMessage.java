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

import org.obiba.rock.model.ErrorMessage;
import org.springframework.http.HttpStatus;

public class ExceptionErrorMessage implements ErrorMessage {

    private final String status;

    private final Exception exception;

    private final String[] args;

    public ExceptionErrorMessage(String status, Exception exception, String... args) {
        this.status = status;
        this.exception = exception;
        this.args = args;
    }

    public ExceptionErrorMessage(HttpStatus status, Exception exception, String... args) {
        this.status = status.value() + "";
        this.exception = exception;
        this.args = args;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getKey() {
        return exception.getClass().getSimpleName();
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public String getMessage() {
        return exception.getMessage();
    }
}
