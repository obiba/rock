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
 * Source the system.info.R file and retrieves result.
 */
public class SystemInfoROperation extends AbstractROperationWithResult {

  private final String systemInfoPath;

  public SystemInfoROperation(String systemInfoPath) {
    this.systemInfoPath = systemInfoPath;
  }

  @Override
  public void doWithConnection() {
    setResult(null);
    setResult(eval(String.format("source('%s')", systemInfoPath)));
    setResult(eval("system.info()", false));
  }

}
