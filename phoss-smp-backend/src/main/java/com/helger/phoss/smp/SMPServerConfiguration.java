/**
 * Copyright (C) 2015-2021 Philip Helger and contributors
 * philip[at]helger[dot]com
 *
 * The Original Code is Copyright The Peppol project (http://www.peppol.eu)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.helger.phoss.smp;

import java.net.Proxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.state.ESuccess;
import com.helger.commons.string.StringHelper;
import com.helger.network.proxy.settings.IProxySettings;
import com.helger.network.proxy.settings.ProxySettings;
import com.helger.peppolid.factory.ESMPIdentifierType;
import com.helger.security.keystore.EKeyStoreType;
import com.helger.settings.exchange.configfile.ConfigFile;
import com.helger.settings.exchange.configfile.ConfigFileBuilder;

/**
 * The central configuration for the SMP server. This class manages the content
 * of the "smp-server.properties" file. The order of the properties file
 * resolving is as follows:
 * <ol>
 * <li>Check for the value of the environment variable
 * <code>SMP_SERVER_CONFIG</code> (since 5.1.0)</li>
 * <li>Check for the value of the system property
 * <code>peppol.smp.server.properties.path</code></li>
 * <li>Check for the value of the system property
 * <code>smp.server.properties.path</code></li>
 * <li>The filename <code>private-smp-server.properties</code> in the root of
 * the classpath</li>
 * <li>The filename <code>smp-server.properties</code> in the root of the
 * classpath</li>
 * </ol>
 * <p>
 * Some of the properties contained in this class can be overwritten in the SMP
 * settings at runtime. That's why the respective direct access methods are
 * deprecated. Use the ones from
 * {@link com.helger.phoss.smp.settings.ISMPSettings} instead!
 * </p>
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class SMPServerConfiguration
{
  public static final String KEY_SMP_BACKEND = "smp.backend";

  public static final String KEY_SMP_KEYSTORE_TYPE = "smp.keystore.type";
  public static final String KEY_SMP_KEYSTORE_PATH = "smp.keystore.path";
  public static final String KEY_SMP_KEYSTORE_PASSWORD = "smp.keystore.password";
  public static final String KEY_SMP_KEYSTORE_KEY_ALIAS = "smp.keystore.key.alias";
  public static final String KEY_SMP_KEYSTORE_KEY_PASSWORD = "smp.keystore.key.password";

  public static final String KEY_SMP_TRUSTSTORE_TYPE = "smp.truststore.type";
  public static final String KEY_SMP_TRUSTSTORE_PATH = "smp.truststore.path";
  public static final String KEY_SMP_TRUSTSTORE_PASSWORD = "smp.truststore.password";

  public static final String KEY_SMP_FORCE_ROOT = "smp.forceroot";
  public static final String KEY_SMP_PUBLIC_URL = "smp.publicurl";
  public static final String KEY_SMP_PUBLIC_URL_MODE = "smp.publicurl.mode";
  public static final String KEY_SMP_IDENTIFIER_TYPE = "smp.identifiertype";
  public static final String KEY_SMP_REST_TYPE = "smp.rest.type";
  public static final String KEY_SMP_REST_WRITABLE_API_DISABLED = "smp.rest.writableapi.disabled";
  public static final String KEY_SMP_REST_LOG_EXCEPTIONS = "smp.rest.log.exceptions";
  public static final String KEY_SMP_REST_PAYLOAD_ON_ERROR = "smp.rest.payload.on.error";
  public static final String KEY_SMP_REST_REMOTE_QUERY_API_DISABLED = "smp.rest.remote.queryapi.disabled";
  public static final String KEY_SMP_STATUS_ENABLED = "smp.status.enabled";
  /* legacy name */
  public static final String KEY_SMP_DIRECTORY_INTEGRATION_ENABLED = "smp.peppol.directory.integration.enabled";
  /* legacy name */
  public static final String KEY_SMP_DIRECTORY_INTEGRATION_REQUIRED = "smp.peppol.directory.integration.required";
  /* legacy name */
  public static final String KEY_SMP_DIRECTORY_INTEGRATION_AUTO_UPDATE = "smp.peppol.directory.integration.autoupdate";
  /* legacy name */
  public static final String KEY_SMP_DIRECTORY_HOSTNAME = "smp.peppol.directory.hostname";
  /* legacy name */
  public static final String KEY_SML_ENABLED = "sml.active";
  /* legacy name */
  public static final String KEY_SML_REQUIRED = "sml.needed";
  public static final String KEY_SML_SMPID = "sml.smpid";
  public static final String KEY_SML_SMP_IP = "sml.smp.ip";
  public static final String KEY_SML_SMP_HOSTNAME = "sml.smp.hostname";
  public static final String KEY_SML_CONNECTION_TIMEOUT_MS = "sml.connection.timeout.ms";
  public static final String KEY_SML_REQUEST_TIMEOUT_MS = "sml.request.timeout.ms";

  public static final boolean DEFAULT_SMP_FORCEROOT = false;
  public static final ESMPIdentifierType DEFAULT_SMP_IDENTIFIER_TYPE = ESMPIdentifierType.PEPPOL;
  public static final ESMPRESTType DEFAULT_SMP_REST_TYPE = ESMPRESTType.PEPPOL;
  public static final boolean DEFAULT_SMP_REST_WRITABLE_API_DISABLED = false;
  public static final boolean DEFAULT_SMP_REST_LOG_EXCEPTIONS = false;
  public static final boolean DEFAULT_SMP_REST_PAYLOAD_ON_ERROR = true;
  public static final boolean DEFAULT_SMP_REST_REMOTE_QUERY_API_DISABLED = true;
  public static final boolean DEFAULT_SMP_STATUS_ENABLED = true;
  public static final boolean DEFAULT_SML_REQUIRED = true;
  public static final boolean DEFAULT_SML_ENABLED = false;
  public static final int DEFAULT_SML_REQUEST_TIMEOUT_MS = 30 * (int) CGlobal.MILLISECONDS_PER_SECOND;
  public static final boolean DEFAULT_SMP_DIRECTORY_INTEGRATION_REQUIRED = true;
  public static final boolean DEFAULT_SMP_DIRECTORY_INTEGRATION_ENABLED = true;
  public static final boolean DEFAULT_SMP_DIRECTORY_INTEGRATION_AUTO_UPDATE = true;
  public static final String DEFAULT_SMP_DIRECTORY_HOSTNAME = "https://directory.peppol.eu";

  /**
   * The name of the primary system property which points to the
   * smp-server.properties files
   */
  public static final String SYSTEM_PROPERTY_PEPPOL_SMP_SERVER_PROPERTIES_PATH = "peppol.smp.server.properties.path";
  /**
   * The name of the secondary system property which points to the
   * smp-server.properties files
   */
  public static final String SYSTEM_PROPERTY_SMP_SERVER_PROPERTIES_PATH = "smp.server.properties.path";

  /** The default primary properties file to load */
  public static final String PATH_PRIVATE_SMP_SERVER_PROPERTIES = "private-smp-server.properties";
  /** The default secondary properties file to load */
  public static final String PATH_SMP_SERVER_PROPERTIES = "smp-server.properties";

  private static final Logger LOGGER = LoggerFactory.getLogger (SMPServerConfiguration.class);
  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static ConfigFile s_aConfigFile;

  static
  {
    reloadConfiguration ();
  }

  /**
   * Reload the configuration file. It checks if the system property
   * {@link #SYSTEM_PROPERTY_PEPPOL_SMP_SERVER_PROPERTIES_PATH} or
   * {@link #SYSTEM_PROPERTY_SMP_SERVER_PROPERTIES_PATH} is present and if so,
   * tries it first, than {@link #PATH_PRIVATE_SMP_SERVER_PROPERTIES} is checked
   * and finally the {@link #PATH_SMP_SERVER_PROPERTIES} path is checked.
   *
   * @return {@link ESuccess}
   */
  @Nonnull
  public static ESuccess reloadConfiguration ()
  {
    final ConfigFileBuilder aCFB = new ConfigFileBuilder ().addPathFromEnvVar ("SMP_SERVER_CONFIG")
                                                           .addPathFromSystemProperty (SYSTEM_PROPERTY_PEPPOL_SMP_SERVER_PROPERTIES_PATH)
                                                           .addPathFromSystemProperty (SYSTEM_PROPERTY_SMP_SERVER_PROPERTIES_PATH)
                                                           .addPath (PATH_PRIVATE_SMP_SERVER_PROPERTIES)
                                                           .addPath (PATH_SMP_SERVER_PROPERTIES);

    return s_aRWLock.writeLockedGet ( () -> {
      s_aConfigFile = aCFB.build ();
      if (s_aConfigFile.isRead ())
      {
        LOGGER.info ("Read Peppol SMP server properties from " + s_aConfigFile.getReadResource ().getPath ());
        return ESuccess.SUCCESS;
      }

      LOGGER.warn ("Failed to read Peppol SMP server properties from " + aCFB.getAllPaths ());
      return ESuccess.FAILURE;
    });
  }

  private SMPServerConfiguration ()
  {}

  /**
   * @return The configuration file. Never <code>null</code>.
   */
  @Nonnull
  public static ConfigFile getConfigFile ()
  {
    return s_aRWLock.readLockedGet ( () -> s_aConfigFile);
  }

  /**
   * @return The backend to be used. Depends on the different possible
   *         implementations. Should not be <code>null</code>. Property
   *         <code>smp.backend</code>.
   * @see com.helger.phoss.smp.backend.SMPBackendRegistry
   */
  @Nullable
  public static String getBackend ()
  {
    return getConfigFile ().getAsString (KEY_SMP_BACKEND);
  }

  /**
   * @return The type to the keystore. This is usually JKS. Property
   *         <code>smp.keystore.type</code>.
   * @since 5.0.4
   */
  @Nonnull
  public static EKeyStoreType getKeyStoreType ()
  {
    final String sType = getConfigFile ().getAsString (KEY_SMP_KEYSTORE_TYPE);
    return EKeyStoreType.getFromIDCaseInsensitiveOrDefault (sType, EKeyStoreType.JKS);
  }

  /**
   * @return The path to the keystore. May be a classpath or an absolute file
   *         path. Property <code>smp.keystore.path</code>.
   */
  @Nullable
  public static String getKeyStorePath ()
  {
    return getConfigFile ().getAsString (KEY_SMP_KEYSTORE_PATH);
  }

  /**
   * @return The password required to open the keystore. Property
   *         <code>smp.keystore.password</code>.
   */
  @Nullable
  public static String getKeyStorePassword ()
  {
    return getConfigFile ().getAsString (KEY_SMP_KEYSTORE_PASSWORD);
  }

  /**
   * @return The alias of the SMP key in the keystore. Property
   *         <code>smp.keystore.key.alias</code>.
   */
  @Nullable
  public static String getKeyStoreKeyAlias ()
  {
    return getConfigFile ().getAsString (KEY_SMP_KEYSTORE_KEY_ALIAS);
  }

  /**
   * @return The password used to access the private key. May be different than
   *         the password to the overall keystore. Property
   *         <code>smp.keystore.key.password</code>.
   */
  @Nullable
  public static char [] getKeyStoreKeyPassword ()
  {
    return getConfigFile ().getAsCharArray (KEY_SMP_KEYSTORE_KEY_PASSWORD);
  }

  /**
   * @return The type to the truststore. This is usually JKS. Property
   *         <code>smp.truststore.type</code>.
   */
  @Nonnull
  public static EKeyStoreType getTrustStoreType ()
  {
    final String sType = getConfigFile ().getAsString (KEY_SMP_TRUSTSTORE_TYPE);
    return EKeyStoreType.getFromIDCaseInsensitiveOrDefault (sType, EKeyStoreType.JKS);
  }

  /**
   * @return The path to the truststore. May be a classpath or an absolute file
   *         path. Property <code>smp.truststore.path</code>.
   */
  @Nullable
  public static String getTrustStorePath ()
  {
    return getConfigFile ().getAsString (KEY_SMP_TRUSTSTORE_PATH);
  }

  /**
   * @return The password required to open the truststore. Property
   *         <code>smp.truststore.password</code>.
   */
  @Nullable
  public static String getTrustStorePassword ()
  {
    return getConfigFile ().getAsString (KEY_SMP_TRUSTSTORE_PASSWORD);
  }

  /**
   * @return <code>true</code> if all paths should be forced to the ROOT ("/")
   *         context, <code>false</code> if the context should remain as it is.
   *         Property <code>smp.forceroot</code>.
   */
  public static boolean isForceRoot ()
  {
    return getConfigFile ().getAsBoolean (KEY_SMP_FORCE_ROOT, DEFAULT_SMP_FORCEROOT);
  }

  /**
   * @return The server URL that should be used to create absolute URLs inside
   *         the application. This may be helpful when running on a proxied
   *         Tomcat behind a web server. Property <code>smp.publicurl</code>.
   */
  @Nullable
  public static String getPublicServerURL ()
  {
    return getConfigFile ().getAsString (KEY_SMP_PUBLIC_URL);
  }

  /**
   * @return The public server URL mode to use. This was introduced for issue
   *         #131. May be <code>null</code>.
   * @since 5.2.4
   */
  @Nullable
  public static String getPublicServerURLMode ()
  {
    return getConfigFile ().getAsString (KEY_SMP_PUBLIC_URL_MODE);
  }

  /**
   * @return The identifier types to be used. Never <code>null</code>. Defaults
   *         to {@link ESMPIdentifierType#PEPPOL}. Property
   *         <code>smp.identifiertype</code>.
   */
  @Nonnull
  public static ESMPIdentifierType getIdentifierType ()
  {
    final String sType = getConfigFile ().getAsString (KEY_SMP_IDENTIFIER_TYPE);
    return ESMPIdentifierType.getFromIDOrDefault (sType, DEFAULT_SMP_IDENTIFIER_TYPE);
  }

  /**
   * @return The REST type to be used. Never <code>null</code>. Defaults to
   *         {@link ESMPRESTType#PEPPOL}. Property <code>smp.rest.type</code>.
   */
  @Nonnull
  public static ESMPRESTType getRESTType ()
  {
    final String sType = getConfigFile ().getAsString (KEY_SMP_REST_TYPE);
    return ESMPRESTType.getFromIDOrDefault (sType, DEFAULT_SMP_REST_TYPE);
  }

  /**
   * @return <code>true</code> if the exceptions in the REST API should be
   *         logged, <code>false</code> if not. By default it is disabled.
   * @since 5.1.0
   */
  public static boolean isRESTLogExceptions ()
  {
    return getConfigFile ().getAsBoolean (KEY_SMP_REST_LOG_EXCEPTIONS, DEFAULT_SMP_REST_LOG_EXCEPTIONS);
  }

  /**
   * @return <code>true</code> if in case of an exception in the REST API´,
   *         payload text should be provided as test, <code>false</code> if not.
   *         By default it is enabled. For security reasons it should be
   *         disabled.
   * @since 5.2.1
   */
  public static boolean isRESTPayloadOnError ()
  {
    return getConfigFile ().getAsBoolean (KEY_SMP_REST_PAYLOAD_ON_ERROR, DEFAULT_SMP_REST_PAYLOAD_ON_ERROR);
  }

  /**
   * @return <code>true</code> if the remote query API (get endpoints and
   *         business cards from other SMP server) is disabled. By default it is
   *         disabled.
   * @since 5.3.0-RC2
   */
  public static boolean isRestRemoteQueryAPIDisabled ()
  {
    return getConfigFile ().getAsBoolean (KEY_SMP_REST_REMOTE_QUERY_API_DISABLED,
                                          DEFAULT_SMP_REST_REMOTE_QUERY_API_DISABLED);
  }

  /**
   * @return <code>true</code> if the status servlet at
   *         <code>/smp-status/</code> is enabled, <code>false</code> if it is
   *         disabled. By default it is enabled.
   * @since 5.0.6
   */
  public static boolean isStatusEnabled ()
  {
    return getConfigFile ().getAsBoolean (KEY_SMP_STATUS_ENABLED, DEFAULT_SMP_STATUS_ENABLED);
  }

  /**
   * @return The SMP-ID to be used in the SML. Only relevant when SML connection
   *         is active. Property <code>sml.smpid</code>.
   */
  @Nullable
  public static String getSMLSMPID ()
  {
    return getConfigFile ().getAsString (KEY_SML_SMPID);
  }

  /**
   * @return The default IP address to be used for the SML registration (in the
   *         form <code>1.2.3.4</code>). May be <code>null</code> in which case
   *         the name must be manually provided.
   * @since 5.0.3
   */
  @Nullable
  public static String getSMLSMPIP ()
  {
    return getConfigFile ().getAsString (KEY_SML_SMP_IP);
  }

  /**
   * @return The default hostname to be used for the SML registration including
   *         an "http://" prefix as in <code>http://smp.example.org</code>. May
   *         be <code>null</code> in which case the name must be manually
   *         provided.
   * @since 5.0.3
   */
  @Nullable
  public static String getSMLSMPHostname ()
  {
    String ret = getConfigFile ().getAsString (KEY_SML_SMP_HOSTNAME);

    // Ensure prefix
    if (StringHelper.hasText (ret) && !ret.startsWith ("http://"))
      ret = "http://" + ret;
    return ret;
  }

  /**
   * @return The connection timeout in milliseconds used for connecting to the
   *         SML server. May be <code>null</code> in which case the system
   *         default timeout should be used.
   * @since 5.0.4
   */
  @Nullable
  public static Integer getSMLConnectionTimeoutMS ()
  {
    return getConfigFile ().getAsIntObj (KEY_SML_CONNECTION_TIMEOUT_MS);
  }

  /**
   * @return The request timeout in milliseconds used for connecting to the SML
   *         server. The default is defined in
   *         {@link #DEFAULT_SML_REQUEST_TIMEOUT_MS} since 5.1.1.
   * @since 5.0.4
   */
  public static int getSMLRequestTimeoutMS ()
  {
    return getConfigFile ().getAsInt (KEY_SML_REQUEST_TIMEOUT_MS, DEFAULT_SML_REQUEST_TIMEOUT_MS);
  }

  /**
   * @return The proxy host to be used for "http" calls. May be
   *         <code>null</code>.
   * @see #getHttpsProxyHost()
   * @since 5.0.7
   */
  @Nullable
  public static String getHttpProxyHost ()
  {
    return s_aConfigFile.getAsString ("http.proxyHost");
  }

  /**
   * @return The proxy port to be used for "http" calls. Defaults to 0.
   * @see #getHttpsProxyPort()
   * @since 5.0.7
   */
  public static int getHttpProxyPort ()
  {
    return s_aConfigFile.getAsInt ("http.proxyPort", 0);
  }

  /**
   * @return The proxy host to be used for "https" calls. May be
   *         <code>null</code>.
   * @see #getHttpProxyHost()
   * @since 5.0.7
   */
  @Nullable
  public static String getHttpsProxyHost ()
  {
    return s_aConfigFile.getAsString ("https.proxyHost");
  }

  /**
   * @return The proxy port to be used for "https" calls. Defaults to 0.
   * @see #getHttpProxyPort()
   * @since 5.0.7
   */
  public static int getHttpsProxyPort ()
  {
    return s_aConfigFile.getAsInt ("https.proxyPort", 0);
  }

  /**
   * @return The username for proxy calls. Valid for https and https proxy. May
   *         be <code>null</code>.
   * @since 5.0.7
   */
  @Nullable
  public static String getProxyUsername ()
  {
    return s_aConfigFile.getAsString ("proxy.username");
  }

  /**
   * @return The password for proxy calls. Valid for https and https proxy. May
   *         be <code>null</code>.
   * @since 5.0.7
   */
  @Nullable
  public static String getProxyPassword ()
  {
    return s_aConfigFile.getAsString ("proxy.password");
  }

  /**
   * @return A single object for all http (but not https) proxy settings. May be
   *         <code>null</code>.
   * @see #getAsHttpsProxySettings()
   * @since 5.0.7
   */
  @Nullable
  public static IProxySettings getAsHttpProxySettings ()
  {
    final String sHostname = getHttpProxyHost ();
    final int nPort = getHttpProxyPort ();
    if (sHostname != null && nPort > 0)
      return new ProxySettings (Proxy.Type.HTTP, sHostname, nPort, getProxyUsername (), getProxyPassword ());
    return null;
  }

  /**
   * @return A single object for all https (but not http) proxy settings. May be
   *         <code>null</code>.
   * @see #getAsHttpProxySettings()
   * @since 5.0.7
   */
  @Nullable
  public static IProxySettings getAsHttpsProxySettings ()
  {
    final String sHostname = getHttpsProxyHost ();
    final int nPort = getHttpsProxyPort ();
    if (sHostname != null && nPort > 0)
      return new ProxySettings (Proxy.Type.HTTP, sHostname, nPort, getProxyUsername (), getProxyPassword ());
    return null;
  }
}
