/*
 * SonarQube Java
 * Copyright (C) 2012-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks.maven;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.checks.maven.helpers.MavenDependencyCollector;
import org.sonar.java.checks.maven.helpers.MavenDependencyMatcher;
import org.sonar.maven.MavenFileScanner;
import org.sonar.maven.MavenFileScannerContext;
import org.sonar.maven.model.maven2.Dependency;
import org.sonar.squidbridge.annotations.NoSqale;
import org.sonar.squidbridge.annotations.RuleTemplate;

@Rule(
  key = DisallowedDependenciesCheck.KEY,
  name = "Disallowed dependencies should not be used",
  priority = Priority.MAJOR)
@RuleTemplate
@NoSqale
public class DisallowedDependenciesCheck implements MavenFileScanner {

  public static final String KEY = "S3417";

  @RuleProperty(
    key = "dependencyName",
    description = "Pattern describing forbidden dependencies group and artifact ids. "
      + "E.G. <code>*:.*log4j</code>, or <code>x.y:*</code> ")
  public String dependencyName = "";

  @RuleProperty(
    key = "version",
    description = "Dependency version pattern or dash-delimited range. Leave blank for all versions. "
      + "E.G. <code>1.3.*</code>, <code>1.0-3.1</code>, <code>1.0-*</code> or <code>*-3.1</code>")
  public String version = "";

  private MavenDependencyMatcher matcher = null;

  @Override
  public void scanFile(MavenFileScannerContext context) {
    for (Dependency dependency : new MavenDependencyCollector(context.getMavenProject()).allDependencies()) {
      if (getMatcher().matches(dependency)) {
        context.reportIssue(this, dependency, "Remove this forbidden dependency.");
      }
    }
  }

  private MavenDependencyMatcher getMatcher() {
    if (matcher == null) {
      try {
        matcher = new MavenDependencyMatcher(dependencyName, version);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("[" + KEY + "] Unable to build matchers from provided dependency name: " + dependencyName, e);
      }
    }
    return matcher;
  }
}
