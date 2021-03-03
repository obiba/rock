/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.rock.r;

import org.rosuda.REngine.REXP;

/**
 * Does the evaluation of a R script and stores the result.
 */
public class RScriptToJSONROperation extends AbstractROperationWithResult {

  private final String script;

  public RScriptToJSONROperation(String script) {
    if (script == null) throw new IllegalArgumentException("R script cannot be null");
    this.script = script;
  }

  /**
   * Evaluates the provided R script.
   */
  @Override
  public void doWithConnection() {
    setResult(null);
    eval("base::assign('.rock.toJSON', function(x) { tryCatch(jsonlite::toJSON(x, auto_unbox = T), error = function(e) { jsonlite::serializeJSON(x) }) })", false);
    REXP rval = eval(String.format(".rock.toJSON(%s)", script), false);
    setResult(rval);
  }

  @Override
  public String toString() {
    return script;
  }
}
