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
package com.helger.peppol.smpserver.rest;

import static org.junit.Assert.assertNotNull;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.helger.peppol.identifier.participant.SimpleParticipantIdentifier;
import com.helger.peppol.smp.ObjectFactory;
import com.helger.peppol.smp.ServiceGroupType;
import com.helger.photon.basic.mock.PhotonBasicWebTestRule;

/**
 * Test class for class {@link ServiceGroupInterface}
 *
 * @author Philip Helger
 */
@Ignore
public final class ServiceGroupInterfaceTest
{
  @Rule
  public final TestRule m_aRule = new PhotonBasicWebTestRule ();

  private HttpServer m_aServer;
  private WebTarget m_aTarget;
  private final ObjectFactory m_aObjFactory = new ObjectFactory ();

  @Before
  public void setUp ()
  {
    // http only
    m_aServer = MockServer.startRegularServer ();

    final Client aClient = ClientBuilder.newClient ();
    m_aTarget = aClient.target (MockServer.BASE_URI_HTTP);
  }

  @After
  public void tearDown ()
  {
    m_aServer.shutdownNow ();
  }

  @Test
  public void testCreateAndDeleteServiceGroup ()
  {
    final String sPI = "9915:xxx";
    final ServiceGroupType aSG = new ServiceGroupType ();
    aSG.setParticipantIdentifier (SimpleParticipantIdentifier.createWithDefaultScheme (sPI));

    final Response aResponseMsg = m_aTarget.path (sPI)
                                           .request ()
                                           .property (HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME,
                                                      "peppol")
                                           .property (HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD,
                                                      "Test1234")
                                           .put (Entity.xml (m_aObjFactory.createServiceGroup (aSG)));
    assertNotNull (aResponseMsg);
  }
}
