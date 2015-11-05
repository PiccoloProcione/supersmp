/**
 * Copyright (C) 2015 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package com.helger.peppol.smpserver;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.system.SystemProperties;
import com.helger.peppol.utils.ConfigFile;

/**
 * The central configuration for the SMP server. This class manages the content
 * of the "smp-server.properties" file. The order of the properties file
 * resolving is as follows:
 * <ol>
 * <li>Check for the value of the system property
 * <code>smp.server.properties.path</code></li>
 * <li>The filename <code>private-smp-server.properties</code> in the root of
 * the classpath</li>
 * <li>The filename <code>smp-server.properties</code> in the root of the
 * classpath</li>
 * </ol>
 *
 * @author Philip Helger
 */
@Immutable
public final class SMPServerConfiguration
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (SMPServerConfiguration.class);
  private static final ConfigFile s_aConfigFile;

  static
  {
    final List <String> aFilePaths = new ArrayList <> ();
    // Check if the system property is present
    final String sPropertyPath = SystemProperties.getPropertyValue ("smp.server.properties.path");
    if (StringHelper.hasText (sPropertyPath))
      aFilePaths.add (sPropertyPath);

    // Use the default paths
    aFilePaths.add ("private-smp-server.properties");
    aFilePaths.add ("smp-server.properties");

    s_aConfigFile = new ConfigFile (ArrayHelper.newArray (aFilePaths, String.class));
    if (s_aConfigFile.isRead ())
      s_aLogger.info ("Read smp-server.properties from " + s_aConfigFile.getReadResource ().getPath ());
    else
      s_aLogger.warn ("Failed to read smp-server.properties from any of the paths: " + aFilePaths);
  }

  private SMPServerConfiguration ()
  {}

  /**
   * @return The configuration file. Never <code>null</code>.
   */
  @Nonnull
  public static ConfigFile getConfigFile ()
  {
    return s_aConfigFile;
  }

  /**
   * @return The backend to be used. Depends on the different possible
   *         implementations. Should not be <code>null</code>.
   */
  @Nullable
  public static String getBackend ()
  {
    return s_aConfigFile.getString ("smp.backend");
  }

  /**
   * @return The path to the keystore. May be a classpath or an absolute file
   *         path.
   */
  @Nullable
  public static String getKeystorePath ()
  {
    return s_aConfigFile.getString ("smp.keystore.path");
  }

  /**
   * @return The password required to open the keystore.
   */
  @Nullable
  public static String getKeystorePassword ()
  {
    return s_aConfigFile.getString ("smp.keystore.password");
  }

  /**
   * @return The alias of the SMP key in the keystore.
   */
  @Nullable
  public static String getKeystoreKeyAlias ()
  {
    return s_aConfigFile.getString ("smp.keystore.key.alias");
  }

  /**
   * @return The password used to access the private key. MAy be different than
   *         the password to the overall keystore.
   */
  @Nullable
  public static char [] getKeystoreKeyPassword ()
  {
    return s_aConfigFile.getCharArray ("smp.keystore.key.password");
  }

  /**
   * @return <code>true</code> if all paths should be forced to the ROOT ("/")
   *         context, <code>false</code> if the context should remain as it is.
   */
  public static boolean isForceRoot ()
  {
    return s_aConfigFile.getBoolean ("smp.forceroot", false);
  }

  /**
   * Check if the writable parts of the REST API are disabled. If this is the
   * case, only the read-only part of the API can be used. The writable REST API
   * will return an HTTP 404 error.
   * 
   * @return <code>true</code> if it is disabled, <code>false</code> if it is
   *         enabled. By the default the writable API is enabled.
   */
  public static boolean isRESTWritableAPIDisabled ()
  {
    return s_aConfigFile.getBoolean ("smp.rest.writableapi.disabled", false);
  }

  /**
   * @return <code>true</code> if the SML connection is active,
   *         <code>false</code> if not.
   */
  public static boolean isWriteToSML ()
  {
    return s_aConfigFile.getBoolean ("sml.active", false);
  }

  /**
   * @return The SML URL to use. Only relevant when {@link #isWriteToSML()} is
   *         <code>true</code>.
   */
  @Nullable
  public static String getSMLURL ()
  {
    return s_aConfigFile.getString ("sml.url");
  }

  /**
   * @return The SMP-ID to be used in the SML. Only relevant when
   *         {@link #isWriteToSML()} is <code>true</code>.
   */
  @Nullable
  public static String getSMLSMPID ()
  {
    return s_aConfigFile.getString ("sml.smpid");
  }
}
