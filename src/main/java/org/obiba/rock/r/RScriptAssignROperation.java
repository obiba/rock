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

/**
 * Does the evaluation of a R script and stores the result.
 */
public class RScriptAssignROperation extends RScriptROperation {

    private final String scriptStr;

    public RScriptAssignROperation(String script) {
        super(String.format("is.null(%s)", script));
        this.scriptStr = script;
        setIgnoreResult(true);
    }

    @Override
    public String toString() {
        return scriptStr;
    }
}
