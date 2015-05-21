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
package com.helger.peppol.smpserver.data.dbms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.busdox.servicemetadata.publishing._1.EndpointType;
import org.busdox.servicemetadata.publishing._1.ExtensionType;
import org.busdox.servicemetadata.publishing._1.ObjectFactory;
import org.busdox.servicemetadata.publishing._1.ProcessListType;
import org.busdox.servicemetadata.publishing._1.ProcessType;
import org.busdox.servicemetadata.publishing._1.ServiceEndpointList;
import org.busdox.servicemetadata.publishing._1.ServiceGroupType;
import org.busdox.servicemetadata.publishing._1.ServiceInformationType;
import org.busdox.servicemetadata.publishing._1.ServiceMetadataType;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.helger.commons.annotations.DevelopersNote;
import com.helger.commons.scopes.mock.ScopeTestRule;
import com.helger.datetime.PDTFactory;
import com.helger.peppol.identifier.CIdentifier;
import com.helger.peppol.identifier.DocumentIdentifierType;
import com.helger.peppol.identifier.IdentifierUtils;
import com.helger.peppol.identifier.ParticipantIdentifierType;
import com.helger.peppol.identifier.doctype.SimpleDocumentTypeIdentifier;
import com.helger.peppol.identifier.participant.SimpleParticipantIdentifier;
import com.helger.peppol.identifier.process.SimpleProcessIdentifier;
import com.helger.peppol.smp.ESMPTransportProfile;
import com.helger.peppol.smp.SMPExtensionConverter;
import com.helger.peppol.smpserver.exception.SMPNotFoundException;
import com.helger.peppol.smpserver.exception.SMPUnauthorizedException;
import com.helger.peppol.smpserver.exception.SMPUnknownUserException;
import com.helger.peppol.smpserver.smlhook.RegistrationHookDoNothing;
import com.helger.peppol.utils.W3CEndpointReferenceUtils;
import com.helger.web.http.basicauth.BasicAuthClientCredentials;

/**
 * @author PEPPOL.AT, BRZ, Philip Helger
 */
// @Ignore
// ("Cannot be enabled by default, because it would fail without the correct configuration")
@DevelopersNote ("You need to adjust your local config.properties file to run this test")
public class DBMSDataManagerTest
{
  private static final String PARTICIPANT_IDENTIFIER_SCHEME = CIdentifier.DEFAULT_PARTICIPANT_IDENTIFIER_SCHEME;
  private static final String DOCUMENT_SCHEME = CIdentifier.DEFAULT_DOCUMENT_TYPE_IDENTIFIER_SCHEME;
  private static final String PROCESS_SCHEME = CIdentifier.DEFAULT_PROCESS_IDENTIFIER_SCHEME;

  private static final String PARTICIPANT_IDENTIFIER1 = "0010:599900000000A";
  private static final String PARTICIPANT_IDENTIFIER2 = "0010:599900000000B";

  private static final String TEST_DOCTYPE_ID = "doc1";
  private static final String TEST_PROCESS_ID = "bis4";

  private static final String USERNAME = "peppol_user";
  private static final String PASSWORD = "Test1234";

  private static final String CERTIFICATE = "VGhpcyBpcyBzdXJlbHkgbm90IGEgdmFsaWQgY2VydGlmaWNhdGUsIGJ1dCBpdCBo\r\n"
                                            + "YXMgbW9yZSB0aGFuIDY0IGNoYXJhY3RlcnM=";
  private static final String ADDRESS = "http://test.eu/accesspoint.svc";
  private static final boolean REQUIRE_SIGNATURE = true;
  private static final String MINIMUM_AUTH_LEVEL = "1";
  private static final LocalDateTime ACTIVIATION_DATE = PDTFactory.getCurrentLocalDateTime ();
  private static final String DESCRIPTION = "description123";
  private static final LocalDateTime EXPIRATION_DATE = PDTFactory.getCurrentLocalDateTime ().plusYears (1);
  private static final String TECH_CONTACT = "fake@peppol.eu";
  private static final String TECH_INFO = "http://fake.peppol.eu/";
  private static final String TRANSPORT_PROFILE = ESMPTransportProfile.TRANSPORT_PROFILE_START.getID ();

  private static final ParticipantIdentifierType PARTY_ID = SimpleParticipantIdentifier.createWithDefaultScheme (PARTICIPANT_IDENTIFIER1);
  private static final ParticipantIdentifierType SERVICEGROUP_ID = PARTY_ID;
  private static final DocumentIdentifierType DOCTYPE_ID = new SimpleDocumentTypeIdentifier (DOCUMENT_SCHEME,
                                                                                             TEST_DOCTYPE_ID);
  private static final BasicAuthClientCredentials CREDENTIALS = new BasicAuthClientCredentials (USERNAME, PASSWORD);

  private static DBMSDataManager s_aDataMgr;

  private static final class SMPTestRule extends ScopeTestRule
  {
    @Override
    public void before ()
    {
      super.before ();
      if (s_aDataMgr == null)
      {
        // Do it only once :)
        SMPEntityManagerFactory.getInstance ();
        s_aDataMgr = new DBMSDataManager (new RegistrationHookDoNothing ());
      }
    }
  }

  @ClassRule
  public static TestRule s_aTestRule = new SMPTestRule ();

  private ServiceGroupType m_aServiceGroup;
  private ServiceMetadataType m_aServiceMetadata;

  @Before
  public void beforeTest () throws Throwable
  {
    final ExtensionType aExtension = SMPExtensionConverter.convertOrNull ("<root><any>value</any></root>");
    assertNotNull (aExtension);
    assertNotNull (aExtension.getAny ());

    final ObjectFactory aObjFactory = new ObjectFactory ();
    m_aServiceGroup = aObjFactory.createServiceGroupType ();
    m_aServiceGroup.setParticipantIdentifier (PARTY_ID);

    // Be sure to delete if it exists.
    try
    {
      s_aDataMgr.deleteServiceGroup (SERVICEGROUP_ID, CREDENTIALS);
    }
    catch (final SMPNotFoundException ex)
    {}

    // Create a new one
    s_aDataMgr.saveServiceGroup (m_aServiceGroup, CREDENTIALS);

    m_aServiceMetadata = aObjFactory.createServiceMetadataType ();
    final ServiceInformationType aServiceInformation = aObjFactory.createServiceInformationType ();
    aServiceInformation.setDocumentIdentifier (DOCTYPE_ID);
    aServiceInformation.setParticipantIdentifier (PARTY_ID);
    aServiceInformation.setExtension (aExtension);
    {
      final ProcessListType processList = aObjFactory.createProcessListType ();
      {
        final ProcessType process = aObjFactory.createProcessType ();
        process.setProcessIdentifier (new SimpleProcessIdentifier (PROCESS_SCHEME, TEST_PROCESS_ID));
        process.setExtension (aExtension);
        {
          final ServiceEndpointList serviceEndpointList = aObjFactory.createServiceEndpointList ();
          {
            final EndpointType endpoint = aObjFactory.createEndpointType ();
            endpoint.setCertificate (CERTIFICATE);
            endpoint.setEndpointReference (W3CEndpointReferenceUtils.createEndpointReference (ADDRESS));
            endpoint.setMinimumAuthenticationLevel (MINIMUM_AUTH_LEVEL);
            endpoint.setRequireBusinessLevelSignature (REQUIRE_SIGNATURE);
            endpoint.setServiceActivationDate (ACTIVIATION_DATE);
            endpoint.setServiceDescription (DESCRIPTION);
            endpoint.setServiceExpirationDate (EXPIRATION_DATE);
            endpoint.setExtension (aExtension);
            endpoint.setTechnicalContactUrl (TECH_CONTACT);
            endpoint.setTechnicalInformationUrl (TECH_INFO);
            endpoint.setTransportProfile (TRANSPORT_PROFILE);
            serviceEndpointList.getEndpoint ().add (endpoint);
          }
          process.setServiceEndpointList (serviceEndpointList);
        }
        processList.getProcess ().add (process);
      }
      aServiceInformation.setProcessList (processList);
    }
    m_aServiceMetadata.setServiceInformation (aServiceInformation);
  }

  @Test
  public void testCreateServiceGroup () throws Throwable
  {
    m_aServiceGroup.getParticipantIdentifier ().setValue (PARTICIPANT_IDENTIFIER2);
    s_aDataMgr.saveServiceGroup (m_aServiceGroup, CREDENTIALS);

    final ParticipantIdentifierType aParticipantIdentifier2 = SimpleParticipantIdentifier.createWithDefaultScheme (PARTICIPANT_IDENTIFIER2);
    final ServiceGroupType result = s_aDataMgr.getServiceGroup (aParticipantIdentifier2);
    assertNotNull (result);

    assertNull (result.getServiceMetadataReferenceCollection ());
    assertEquals (PARTICIPANT_IDENTIFIER_SCHEME, result.getParticipantIdentifier ().getScheme ());
    assertTrue (IdentifierUtils.areParticipantIdentifierValuesEqual (PARTICIPANT_IDENTIFIER2,
                                                                     result.getParticipantIdentifier ().getValue ()));
  }

  @Test
  public void testCreateServiceGroupInvalidPassword () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials (USERNAME, "WRONG_PASSWORD");

    m_aServiceGroup.getParticipantIdentifier ().setValue (PARTICIPANT_IDENTIFIER2);
    try
    {
      s_aDataMgr.saveServiceGroup (m_aServiceGroup, aCredentials);
      fail ();
    }
    catch (final SMPUnauthorizedException ex)
    {}
  }

  @Test
  public void testCreateServiceGroupUnknownUser () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials ("Unknown_User", PASSWORD);

    m_aServiceGroup.getParticipantIdentifier ().setValue (PARTICIPANT_IDENTIFIER2);
    try
    {
      s_aDataMgr.saveServiceGroup (m_aServiceGroup, aCredentials);
      fail ();
    }
    catch (final SMPUnknownUserException ex)
    {}
  }

  @Test
  public void testDeleteServiceGroup () throws Throwable
  {
    s_aDataMgr.deleteServiceGroup (SERVICEGROUP_ID, CREDENTIALS);

    assertNull (s_aDataMgr.getServiceGroup (SERVICEGROUP_ID));
  }

  @Test
  public void testDeleteServiceGroupUnknownID () throws Throwable
  {
    final ParticipantIdentifierType aServiceGroupID2 = SimpleParticipantIdentifier.createWithDefaultScheme (PARTICIPANT_IDENTIFIER2);
    try
    {
      s_aDataMgr.deleteServiceGroup (aServiceGroupID2, CREDENTIALS);
    }
    catch (final SMPNotFoundException ex)
    {}
    assertNull (s_aDataMgr.getServiceGroup (aServiceGroupID2));
  }

  @Test
  public void testDeleteServiceGroupUnknownUser () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials ("Unknown_User", PASSWORD);
    try
    {
      s_aDataMgr.deleteServiceGroup (SERVICEGROUP_ID, aCredentials);
      fail ();
    }
    catch (final SMPUnknownUserException ex)
    {}
  }

  @Test
  public void testDeleteServiceGroupWrongPass () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials (USERNAME, "WrongPassword");
    try
    {
      s_aDataMgr.deleteServiceGroup (SERVICEGROUP_ID, aCredentials);
      fail ();
    }
    catch (final SMPUnauthorizedException ex)
    {}
  }

  @Test
  public void testCreateServiceMetadata () throws Throwable
  {
    // Save to DB
    s_aDataMgr.saveService (m_aServiceMetadata.getServiceInformation (), CREDENTIALS);

    // Retrieve from DB
    final ServiceMetadataType aDBServiceMetadata = s_aDataMgr.getService (SERVICEGROUP_ID, DOCTYPE_ID);
    assertNotNull (aDBServiceMetadata);

    final ProcessListType aOrigProcessList = m_aServiceMetadata.getServiceInformation ().getProcessList ();
    assertEquals (1, aOrigProcessList.getProcess ().size ());
    final ProcessType aOrigProcess = aOrigProcessList.getProcess ().get (0);
    assertEquals (1, aOrigProcess.getServiceEndpointList ().getEndpoint ().size ());
    final EndpointType aOrigEndpoint = aOrigProcess.getServiceEndpointList ().getEndpoint ().get (0);

    final ProcessType aDBProcess = aDBServiceMetadata.getServiceInformation ().getProcessList ().getProcess ().get (0);
    final EndpointType aDBEndpoint = aDBProcess.getServiceEndpointList ().getEndpoint ().get (0);

    assertTrue (IdentifierUtils.areDocumentTypeIdentifiersEqual (m_aServiceMetadata.getServiceInformation ()
                                                                                   .getDocumentIdentifier (),
                                                                 aDBServiceMetadata.getServiceInformation ()
                                                                                   .getDocumentIdentifier ()));
    assertTrue (IdentifierUtils.areParticipantIdentifiersEqual (m_aServiceMetadata.getServiceInformation ()
                                                                                  .getParticipantIdentifier (),
                                                                aDBServiceMetadata.getServiceInformation ()
                                                                                  .getParticipantIdentifier ()));
    assertTrue (IdentifierUtils.areProcessIdentifiersEqual (aOrigProcess.getProcessIdentifier (),
                                                            aDBProcess.getProcessIdentifier ()));
    assertEquals (aOrigEndpoint.getCertificate (), aDBEndpoint.getCertificate ());
    assertEquals (aOrigEndpoint.getMinimumAuthenticationLevel (), aDBEndpoint.getMinimumAuthenticationLevel ());
    assertEquals (aOrigEndpoint.getServiceDescription (), aDBEndpoint.getServiceDescription ());
    assertEquals (aOrigEndpoint.getTechnicalContactUrl (), aDBEndpoint.getTechnicalContactUrl ());
    assertEquals (aOrigEndpoint.getTechnicalInformationUrl (), aDBEndpoint.getTechnicalInformationUrl ());
    assertEquals (aOrigEndpoint.getTransportProfile (), aDBEndpoint.getTransportProfile ());
    assertEquals (W3CEndpointReferenceUtils.getAddress (aOrigEndpoint.getEndpointReference ()),
                  W3CEndpointReferenceUtils.getAddress (aDBEndpoint.getEndpointReference ()));
  }

  @Test
  public void testCreateServiceMetadataUnknownUser () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials ("Unknown_User", PASSWORD);
    try
    {
      s_aDataMgr.saveService (m_aServiceMetadata.getServiceInformation (), aCredentials);
      fail ();
    }
    catch (final SMPUnknownUserException ex)
    {}
  }

  @Test
  public void testCreateServiceMetadataWrongPass () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials (USERNAME, "WrongPassword");
    try
    {
      s_aDataMgr.saveService (m_aServiceMetadata.getServiceInformation (), aCredentials);
      fail ();
    }
    catch (final SMPUnauthorizedException ex)
    {}
  }

  @Test
  public void testPrintServiceMetadata () throws Throwable
  {
    // Ensure something is present :)
    s_aDataMgr.saveService (m_aServiceMetadata.getServiceInformation (), CREDENTIALS);
    System.out.println (s_aDataMgr.getService (SERVICEGROUP_ID, DOCTYPE_ID));
  }

  @Test
  public void testDeleteServiceMetadata () throws Throwable
  {
    // Ensure something is present :)
    s_aDataMgr.saveService (m_aServiceMetadata.getServiceInformation (), CREDENTIALS);

    // First deletion succeeds
    s_aDataMgr.deleteService (SERVICEGROUP_ID, DOCTYPE_ID, CREDENTIALS);
    try
    {
      // Second deletion fails
      s_aDataMgr.deleteService (SERVICEGROUP_ID, DOCTYPE_ID, CREDENTIALS);
      fail ();
    }
    catch (final SMPNotFoundException ex)
    {}
  }

  @Test
  public void testDeleteServiceMetadataUnknownUser () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials ("Unknown_User", PASSWORD);
    try
    {
      s_aDataMgr.deleteService (SERVICEGROUP_ID, DOCTYPE_ID, aCredentials);
      fail ();
    }
    catch (final SMPUnknownUserException ex)
    {}
  }

  @Test
  public void testDeleteServiceMetadataWrongPass () throws Throwable
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials (USERNAME, "WrongPassword");
    try
    {
      s_aDataMgr.deleteService (SERVICEGROUP_ID, DOCTYPE_ID, aCredentials);
      fail ();
    }
    catch (final SMPUnauthorizedException ex)
    {}
  }
}
