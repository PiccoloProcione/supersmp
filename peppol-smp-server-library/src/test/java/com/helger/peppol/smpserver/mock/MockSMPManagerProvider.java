package com.helger.peppol.smpserver.mock;

import java.util.Collection;
import java.util.List;

import com.helger.commons.state.EChange;
import com.helger.peppol.identifier.generic.doctype.IDocumentTypeIdentifier;
import com.helger.peppol.identifier.generic.participant.IParticipantIdentifier;
import com.helger.peppol.identifier.generic.process.IProcessIdentifier;
import com.helger.peppol.smp.ISMPTransportProfile;
import com.helger.peppol.smpserver.domain.ISMPManagerProvider;
import com.helger.peppol.smpserver.domain.businesscard.ISMPBusinessCard;
import com.helger.peppol.smpserver.domain.businesscard.ISMPBusinessCardManager;
import com.helger.peppol.smpserver.domain.businesscard.SMPBusinessCardEntity;
import com.helger.peppol.smpserver.domain.redirect.ISMPRedirect;
import com.helger.peppol.smpserver.domain.redirect.ISMPRedirectManager;
import com.helger.peppol.smpserver.domain.servicegroup.ISMPServiceGroup;
import com.helger.peppol.smpserver.domain.servicegroup.ISMPServiceGroupManager;
import com.helger.peppol.smpserver.domain.serviceinfo.ISMPServiceInformation;
import com.helger.peppol.smpserver.domain.serviceinfo.ISMPServiceInformationManager;
import com.helger.peppol.smpserver.domain.transportprofile.ISMPTransportProfileManager;
import com.helger.peppol.smpserver.domain.user.ISMPUser;
import com.helger.peppol.smpserver.domain.user.ISMPUserManager;
import com.helger.peppol.smpserver.exception.SMPNotFoundException;
import com.helger.peppol.smpserver.exception.SMPUnauthorizedException;
import com.helger.web.http.basicauth.BasicAuthClientCredentials;

public class MockSMPManagerProvider implements ISMPManagerProvider
{
  public ISMPTransportProfileManager createTransportProfileMgr ()
  {
    return new ISMPTransportProfileManager ()
    {
      public EChange updateSMPTransportProfile (final String sSMPTransportProfileID, final String sName)
      {
        return null;
      }

      public EChange removeSMPTransportProfile (final String sSMPTransportProfileID)
      {
        return null;
      }

      public ISMPTransportProfile getSMPTransportProfileOfID (final String sID)
      {
        return null;
      }

      public Collection <? extends ISMPTransportProfile> getAllSMPTransportProfiles ()
      {
        return null;
      }

      public ISMPTransportProfile createSMPTransportProfile (final String sID, final String sName)
      {
        return null;
      }

      public boolean containsSMPTransportProfileWithID (final String sID)
      {
        return false;
      }
    };
  }

  public ISMPUserManager createUserMgr ()
  {
    return new ISMPUserManager ()
    {
      public Object verifyOwnership (final IParticipantIdentifier aServiceGroupID,
                                     final ISMPUser aCurrentUser) throws SMPNotFoundException, SMPUnauthorizedException
      {
        return null;
      }

      public ISMPUser validateUserCredentials (final BasicAuthClientCredentials aCredentials) throws Throwable
      {
        return null;
      }

      public void updateUser (final String sUserName, final String sPassword)
      {}

      public boolean isSpecialUserManagementNeeded ()
      {
        return false;
      }

      public ISMPUser getUserOfID (final String sUserID)
      {
        return null;
      }

      public int getUserCount ()
      {
        return 0;
      }

      public Collection <? extends ISMPUser> getAllUsers ()
      {
        return null;
      }

      public void deleteUser (final String sUserName)
      {}

      public void createUser (final String sUserName, final String sPassword)
      {}
    };
  }

  public ISMPServiceGroupManager createServiceGroupMgr ()
  {
    return new ISMPServiceGroupManager ()
    {
      public EChange updateSMPServiceGroup (final String sSMPServiceGroupID,
                                            final String sOwnerID,
                                            final String sExtension)
      {
        return null;
      }

      public ISMPServiceGroup getSMPServiceGroupOfID (final IParticipantIdentifier aParticipantIdentifier)
      {
        return null;
      }

      public int getSMPServiceGroupCountOfOwner (final String sOwnerID)
      {
        return 0;
      }

      public int getSMPServiceGroupCount ()
      {
        return 0;
      }

      public Collection <? extends ISMPServiceGroup> getAllSMPServiceGroupsOfOwner (final String sOwnerID)
      {
        return null;
      }

      public Collection <? extends ISMPServiceGroup> getAllSMPServiceGroups ()
      {
        return null;
      }

      public EChange deleteSMPServiceGroup (final IParticipantIdentifier aParticipantIdentifier)
      {
        return null;
      }

      public ISMPServiceGroup createSMPServiceGroup (final String sOwnerID,
                                                     final IParticipantIdentifier aParticipantIdentifier,
                                                     final String sExtension)
      {
        return null;
      }

      public boolean containsSMPServiceGroupWithID (final IParticipantIdentifier aParticipantIdentifier)
      {
        return false;
      }
    };
  }

  public ISMPRedirectManager createRedirectMgr ()
  {
    return new ISMPRedirectManager ()
    {
      public ISMPRedirect getSMPRedirectOfServiceGroupAndDocumentType (final ISMPServiceGroup aServiceGroup,
                                                                       final IDocumentTypeIdentifier aDocTypeID)
      {
        return null;
      }

      public int getSMPRedirectCount ()
      {
        return 0;
      }

      public Collection <? extends ISMPRedirect> getAllSMPRedirectsOfServiceGroup (final ISMPServiceGroup aServiceGroup)
      {
        return null;
      }

      public Collection <? extends ISMPRedirect> getAllSMPRedirects ()
      {
        return null;
      }

      public EChange deleteSMPRedirect (final ISMPRedirect aSMPRedirect)
      {
        return null;
      }

      public EChange deleteAllSMPRedirectsOfServiceGroup (final ISMPServiceGroup aServiceGroup)
      {
        return null;
      }

      public ISMPRedirect createOrUpdateSMPRedirect (final ISMPServiceGroup aServiceGroup,
                                                     final IDocumentTypeIdentifier aDocumentTypeIdentifier,
                                                     final String sTargetHref,
                                                     final String sSubjectUniqueIdentifier,
                                                     final String sExtension)
      {
        return null;
      }
    };
  }

  public ISMPServiceInformationManager createServiceInformationMgr ()
  {
    return new ISMPServiceInformationManager ()
    {
      public void mergeSMPServiceInformation (final ISMPServiceInformation aServiceInformation)
      {}

      public ISMPServiceInformation getSMPServiceInformationOfServiceGroupAndDocumentType (final ISMPServiceGroup aServiceGroup,
                                                                                           final IDocumentTypeIdentifier aDocumentTypeIdentifier)
      {
        return null;
      }

      public int getSMPServiceInformationCount ()
      {
        return 0;
      }

      public Collection <? extends ISMPServiceInformation> getAllSMPServiceInformationsOfServiceGroup (final ISMPServiceGroup aServiceGroup)
      {
        return null;
      }

      public Collection <? extends ISMPServiceInformation> getAllSMPServiceInformation ()
      {
        return null;
      }

      public Collection <IDocumentTypeIdentifier> getAllSMPDocumentTypesOfServiceGroup (final ISMPServiceGroup aServiceGroup)
      {
        return null;
      }

      public ISMPServiceInformation findServiceInformation (final ISMPServiceGroup aServiceGroup,
                                                            final IDocumentTypeIdentifier aDocTypeID,
                                                            final IProcessIdentifier aProcessID,
                                                            final ISMPTransportProfile aTransportProfile)
      {
        return null;
      }

      public EChange deleteSMPServiceInformation (final ISMPServiceInformation aSMPServiceInformation)
      {
        return null;
      }

      public EChange deleteAllSMPServiceInformationOfServiceGroup (final ISMPServiceGroup aServiceGroup)
      {
        return null;
      }
    };
  }

  public ISMPBusinessCardManager createBusinessCardMgr ()
  {
    return new ISMPBusinessCardManager ()
    {
      public ISMPBusinessCard getSMPBusinessCardOfServiceGroup (final ISMPServiceGroup aServiceGroup)
      {
        return null;
      }

      public ISMPBusinessCard getSMPBusinessCardOfID (final String sID)
      {
        return null;
      }

      public int getSMPBusinessCardCount ()
      {
        return 0;
      }

      public Collection <? extends ISMPBusinessCard> getAllSMPBusinessCards ()
      {
        return null;
      }

      public EChange deleteSMPBusinessCard (final ISMPBusinessCard aSMPBusinessCard)
      {
        return null;
      }

      public ISMPBusinessCard createOrUpdateSMPBusinessCard (final ISMPServiceGroup aServiceGroup,
                                                             final List <SMPBusinessCardEntity> aEntities)
      {
        return null;
      }
    };
  }
}
