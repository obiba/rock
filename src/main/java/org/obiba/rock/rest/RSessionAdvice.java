/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.rest;

import org.obiba.rock.domain.ExceptionErrorMessage;
import org.obiba.rock.model.ErrorMessage;
import org.obiba.rock.r.NoSuchRCommandException;
import org.obiba.rock.r.REvaluationRuntimeException;
import org.obiba.rock.r.RRuntimeException;
import org.obiba.rock.service.RSessionNotFoundException;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class RSessionAdvice {

    @ResponseBody
    @ExceptionHandler(RSessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorMessage rSessionNotFoundHandler(RSessionNotFoundException ex) {
        return new ExceptionErrorMessage(HttpStatus.NOT_FOUND, ex, ex.getId());
    }

    @ResponseBody
    @ExceptionHandler(NoSuchRCommandException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorMessage rCommandNotFoundHandler(NoSuchRCommandException ex) {
        return new ExceptionErrorMessage(HttpStatus.NOT_FOUND, ex, ex.getId());
    }

    @ResponseBody
    @ExceptionHandler(RRuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorMessage rRuntimeHandler(RRuntimeException ex) {
        return new ExceptionErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorMessage illegalArgumentHandler(IllegalArgumentException ex) {
        return new ExceptionErrorMessage(HttpStatus.BAD_REQUEST, ex);
    }

    @ResponseBody
    @ExceptionHandler(REvaluationRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorMessage illegalArgumentHandler(REvaluationRuntimeException ex) {
        return new ExceptionErrorMessage(HttpStatus.BAD_REQUEST, ex);
    }

    @ResponseBody
    @ExceptionHandler(RserveException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorMessage rServeExceptionHandler(RserveException ex) {
        return new ExceptionErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ResponseBody
    @ExceptionHandler(REXPMismatchException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorMessage rexpMismatchHandler(REXPMismatchException ex) {
        return new ExceptionErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorMessage illegalArgumentHandler(AccessDeniedException ex) {
        return new ExceptionErrorMessage(HttpStatus.FORBIDDEN, ex);
    }

}
