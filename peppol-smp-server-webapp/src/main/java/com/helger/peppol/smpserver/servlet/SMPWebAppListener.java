/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.peppol.smpserver.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.helger.commons.exception.InitializationException;
import com.helger.peppol.smpserver.SMPServerConfiguration;
import com.helger.peppol.smpserver.app.AppSecurity;
import com.helger.peppol.smpserver.app.AppSettings;
import com.helger.peppol.smpserver.data.sql.mgr.SQLManagerProvider;
import com.helger.peppol.smpserver.data.xml.mgr.XMLManagerProvider;
import com.helger.peppol.smpserver.domain.SMPMetaManager;
import com.helger.peppol.smpserver.ui.AppCommonUI;
import com.helger.peppol.smpserver.ui.pub.InitializerPublic;
import com.helger.peppol.smpserver.ui.secure.InitializerSecure;
import com.helger.photon.basic.app.request.ApplicationRequestManager;
import com.helger.photon.core.app.CApplication;
import com.helger.photon.core.app.context.LayoutExecutionContext;
import com.helger.photon.core.app.init.IApplicationInitializer;
import com.helger.photon.core.servlet.AbstractWebAppListenerMultiApp;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * Special SMP web app listener
 *
 * @author Philip Helger
 */
public class SMPWebAppListener extends AbstractWebAppListenerMultiApp <LayoutExecutionContext>
{
  @Override
  protected String getInitParameterDebug (@Nonnull final ServletContext aSC)
  {
    return AppSettings.getGlobalDebug ();
  }

  @Override
  protected String getInitParameterProduction (@Nonnull final ServletContext aSC)
  {
    return AppSettings.getGlobalProduction ();
  }

  @Override
  protected String getDataPath (@Nonnull final ServletContext aSC)
  {
    return AppSettings.getDataPath ();
  }

  @Override
  protected boolean shouldCheckFileAccess (@Nonnull final ServletContext aSC)
  {
    return AppSettings.isCheckFileAccess ();
  }

  @Override
  @Nonnull
  protected Map <String, IApplicationInitializer <LayoutExecutionContext>> getAllInitializers ()
  {
    final Map <String, IApplicationInitializer <LayoutExecutionContext>> ret = new HashMap <String, IApplicationInitializer <LayoutExecutionContext>> ();
    ret.put (CApplication.APP_ID_PUBLIC, new InitializerPublic ());
    ret.put (CApplication.APP_ID_SECURE, new InitializerSecure ());
    return ret;
  }

  public static void initBackendFromConfiguration ()
  {
    // Determine backend
    final String sBackend = SMPServerConfiguration.getBackend ();
    if ("sql".equalsIgnoreCase (sBackend))
      SMPMetaManager.setManagerFactory (new SQLManagerProvider ());
    else
      if ("xml".equalsIgnoreCase (sBackend))
        SMPMetaManager.setManagerFactory (new XMLManagerProvider ());
      else
        throw new InitializationException ("Invalid backend '" +
                                           sBackend +
                                           "' provided. Only 'sql' and 'xml' are supported!");

    // Now we can call getInstance
    SMPMetaManager.getInstance ();
  }

  @Override
  protected void initGlobals ()
  {
    // Internal stuff:

    // JUL to SLF4J
    SLF4JBridgeHandler.removeHandlersForRootLogger ();
    SLF4JBridgeHandler.install ();

    super.initGlobals ();

    if (SMPServerConfiguration.isForceRoot ())
    {
      // Enforce an empty context path according to the specs!
      WebScopeManager.getGlobalScope ().setCustomContextPath ("");
    }
    ApplicationRequestManager.getRequestMgr ().setUsePaths (true);

    // UI stuff
    AppCommonUI.init ();

    // Set all security related stuff
    AppSecurity.init ();

    // Determine backend
    initBackendFromConfiguration ();
  }
}
