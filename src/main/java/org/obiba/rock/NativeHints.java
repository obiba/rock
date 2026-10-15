/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import org.obiba.rock.domain.RServeCommand;

/**
 * Native image hints for what is only reached at runtime: classes instantiated reflectively from the
 * external conf/logback.xml, classpath R scripts and polymorphic JSON types.
 * Ignored when running on the JVM.
 */
class NativeHints implements RuntimeHintsRegistrar {

  private static final String[] LOGBACK_CLASSES = {
      "ch.qos.logback.classic.jul.LevelChangePropagator",
      "ch.qos.logback.classic.filter.ThresholdFilter",
      "ch.qos.logback.classic.encoder.PatternLayoutEncoder",
      "ch.qos.logback.core.ConsoleAppender",
      "ch.qos.logback.core.rolling.RollingFileAppender",
      "ch.qos.logback.core.rolling.FixedWindowRollingPolicy",
      "ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy",
      "ch.qos.logback.core.util.FileSize"
  };

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    for (String className : LOGBACK_CLASSES) {
      hints.reflection().registerType(TypeReference.of(className),
          MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
    }
    // R scripts loaded from the classpath
    hints.resources().registerPattern("*.R");
    // serialized as its runtime type, controllers declare RCommand
    new BindingReflectionHintsRegistrar().registerReflectionHints(hints.reflection(), RServeCommand.class);
  }

}
