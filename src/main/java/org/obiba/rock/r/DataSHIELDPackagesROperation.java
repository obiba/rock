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

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class DataSHIELDPackagesROperation extends AbstractROperationWithResult {

  private static final String DATASHIELD_FIND_SCRIPT = ".datashieldFind.R";

  private final boolean serialize;

  public DataSHIELDPackagesROperation(boolean serialize) {
    this.serialize = serialize;
  }

  @Override
  protected void doWithConnection() {
    setResult(null);
    try (InputStream is = new ClassPathResource(DATASHIELD_FIND_SCRIPT).getInputStream();) {
      writeFile(DATASHIELD_FIND_SCRIPT, is);
      eval(String.format("base::source('%s')", DATASHIELD_FIND_SCRIPT));
    } catch (IOException e) {
      throw new RRuntimeException(e);
    }
    if (serialize)
      setResult(eval(".datashieldFind()"));
    else
      setResult(eval("jsonlite::toJSON(.datashieldFind(), auto_unbox = F)", false));
  }
}
