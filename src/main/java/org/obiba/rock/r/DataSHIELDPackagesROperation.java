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

import com.google.common.base.Joiner;

public class DataSHIELDPackagesROperation extends AbstractROperationWithResult {

  public static final String AGGREGATE_METHODS = "AggregateMethods";

  public static final String ASSIGN_METHODS = "AssignMethods";

  public static final String OPTIONS = "Options";

  private final boolean serialize;

  public DataSHIELDPackagesROperation(boolean serialize) {
    this.serialize = serialize;
  }

  @Override
  protected void doWithConnection() {
    setResult(null);
    // DS fields
    eval(String.format("base::assign('dsFields', c('%s'))", Joiner.on("','").join(AGGREGATE_METHODS, ASSIGN_METHODS, OPTIONS)));
    // extract DS fields from DESCRIPTION files
    eval("assign('pkgs', Map(function(p) { x <- as.list(p) ; x[names(x) %in% dsFields] }, " +
        "         Filter(function(p) any(names(p) %in% dsFields), " +
        "                lapply(installed.packages()[,1], function(p) as.data.frame(read.dcf(system.file('DESCRIPTION', package=p)), stringsAsFactors = FALSE)))))");
    // extract DS fields from DATASHIELD files
    eval("assign('x', lapply(installed.packages()[,1], function(p) system.file('DATASHIELD', package=p)))");
    eval("assign('y', lapply(x[lapply(x, nchar)>0], function(f) as.list(as.data.frame(read.dcf(f), stringsAsFactors = FALSE))))");
    // merge and prepare DS field values as arrays of strings
    eval("assign('pkgs', lapply(append(pkgs, y), function(p) lapply(p, function(pp)  gsub('^\\\\s+|\\\\s+$', '', gsub('\\n', '', unlist(strsplit(pp, ',')))))))");
    if (serialize)
      setResult(eval("pkgs"));
    else
      setResult(eval("jsonlite::toJSON(pkgs, auto_unbox = F)", false));
  }
}
