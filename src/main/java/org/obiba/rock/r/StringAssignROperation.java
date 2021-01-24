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
 * Assign textual values to symbols in R.
 */
public class StringAssignROperation extends AbstractROperation {

  private final String symbol;

  private final String value;

  public StringAssignROperation(String symbol, String value) {
    this.symbol = symbol;
    this.value = value;
  }

  @Override
  public void doWithConnection() {
    if (symbol == null) return;
    assign(symbol, value);
  }

  @Override
  public String toString() {
    return symbol + " <- " + value;
  }
}
