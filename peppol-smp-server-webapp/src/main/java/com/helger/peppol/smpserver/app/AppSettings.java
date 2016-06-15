/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.peppol.smpserver.app;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.scope.singleton.AbstractGlobalSingleton;
import com.helger.commons.string.StringHelper;
import com.helger.commons.system.SystemProperties;
import com.helger.peppol.utils.ConfigFile;
import com.helger.settings.ISettings;

/**
 * This class provides access to the web application settings. If the system
 * property <code>peppol.smp.webapp.properties.path</code> or
 * <code>smp.webapp.properties.path</code> is defined, the configuration file is
 * read from the absolute path stated there. Otherwise (by default) the
 * configuration settings contained in the
 * <code>src/main/resources/private-webapp.properties</code> or
 * <code>src/main/resources/webapp.properties</code> file are read.
 *
 * @author Philip Helger
 */
public final class AppSettings extends AbstractGlobalSingleton
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AppSettings.class);

  private static final ConfigFile s_aConfigFile;

  static
  {
    final ICommonsList <String> aFilePaths = new CommonsArrayList <> ();
    // Check if the system property is present
    String sPropertyPath = SystemProperties.getPropertyValue ("peppol.smp.webapp.properties.path");
    if (StringHelper.hasText (sPropertyPath))
      aFilePaths.add (sPropertyPath);
    sPropertyPath = SystemProperties.getPropertyValue ("smp.webapp.properties.path");
    if (StringHelper.hasText (sPropertyPath))
      aFilePaths.add (sPropertyPath);

    // Use the default paths
    aFilePaths.add ("private-webapp.properties");
    aFilePaths.add ("webapp.properties");

    s_aConfigFile = ConfigFile.create (aFilePaths);
    if (!s_aConfigFile.isRead ())
      throw new IllegalStateException ("Failed to read PEPPOL SMP UI properties from any of the paths: " + aFilePaths);
    s_aLogger.info ("Read PEPPOL SMP UI properties from " + s_aConfigFile.getReadResource ().getPath ());
  }

  @Deprecated
  @UsedViaReflection
  private AppSettings ()
  {}

  @Nonnull
  public static ISettings getSettingsObject ()
  {
    return s_aConfigFile.getSettings ();
  }

  @Nonnull
  public static IReadableResource getSettingsResource ()
  {
    return s_aConfigFile.getReadResource ();
  }

  @Nullable
  public static String getGlobalDebug ()
  {
    return s_aConfigFile.getAsString ("global.debug");
  }

  @Nullable
  public static String getGlobalProduction ()
  {
    return s_aConfigFile.getAsString ("global.production");
  }

  @Nullable
  public static String getDataPath ()
  {
    return s_aConfigFile.getAsString ("webapp.datapath");
  }

  public static boolean isCheckFileAccess ()
  {
    return s_aConfigFile.getAsBoolean ("webapp.checkfileaccess", true);
  }

  public static boolean isTestVersion ()
  {
    return s_aConfigFile.getAsBoolean ("webapp.testversion", GlobalDebug.isDebugMode ());
  }
}
