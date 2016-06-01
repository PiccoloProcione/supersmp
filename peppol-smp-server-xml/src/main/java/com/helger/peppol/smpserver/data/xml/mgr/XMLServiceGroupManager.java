/**
 * Copyright (C) 2015-2016 Philip Helger (www.helger.com)
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
package com.helger.peppol.smpserver.data.xml.mgr;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.ICommonsCollection;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.MicroDocument;
import com.helger.commons.microdom.convert.MicroTypeConverter;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.peppol.identifier.IdentifierHelper;
import com.helger.peppol.identifier.generic.participant.IParticipantIdentifier;
import com.helger.peppol.smpserver.domain.SMPMetaManager;
import com.helger.peppol.smpserver.domain.redirect.ISMPRedirect;
import com.helger.peppol.smpserver.domain.redirect.ISMPRedirectManager;
import com.helger.peppol.smpserver.domain.servicegroup.ISMPServiceGroup;
import com.helger.peppol.smpserver.domain.servicegroup.ISMPServiceGroupManager;
import com.helger.peppol.smpserver.domain.servicegroup.SMPServiceGroup;
import com.helger.peppol.smpserver.domain.serviceinfo.ISMPServiceInformation;
import com.helger.peppol.smpserver.domain.serviceinfo.ISMPServiceInformationManager;
import com.helger.peppol.smpserver.smlhook.IRegistrationHook;
import com.helger.peppol.smpserver.smlhook.RegistrationHookFactory;
import com.helger.photon.basic.app.dao.impl.AbstractWALDAO;
import com.helger.photon.basic.app.dao.impl.DAOException;
import com.helger.photon.basic.app.dao.impl.EDAOActionType;
import com.helger.photon.basic.audit.AuditHelper;

public final class XMLServiceGroupManager extends AbstractWALDAO <SMPServiceGroup> implements ISMPServiceGroupManager
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (XMLServiceGroupManager.class);

  private static final String ELEMENT_ROOT = "servicegroups";
  private static final String ELEMENT_ITEM = "servicegroup";

  private final ICommonsMap <String, SMPServiceGroup> m_aMap = new CommonsHashMap<> ();
  private final IRegistrationHook m_aHook;

  public XMLServiceGroupManager (@Nonnull @Nonempty final String sFilename) throws DAOException
  {
    super (SMPServiceGroup.class, sFilename);
    m_aHook = RegistrationHookFactory.getOrCreateInstance ();
    initialRead ();
  }

  @Override
  protected void onRecoveryCreate (@Nonnull final SMPServiceGroup aElement)
  {
    _addSMPServiceGroup (aElement, false);
  }

  @Override
  protected void onRecoveryUpdate (@Nonnull final SMPServiceGroup aElement)
  {
    _addSMPServiceGroup (aElement, true);
  }

  @Override
  protected void onRecoveryDelete (@Nonnull final SMPServiceGroup aElement)
  {
    m_aMap.remove (aElement.getID ());
  }

  @Override
  @Nonnull
  protected EChange onRead (@Nonnull final IMicroDocument aDoc)
  {
    for (final IMicroElement eSMPServiceGroup : aDoc.getDocumentElement ().getAllChildElements (ELEMENT_ITEM))
      _addSMPServiceGroup (MicroTypeConverter.convertToNative (eSMPServiceGroup, SMPServiceGroup.class), false);
    return EChange.UNCHANGED;
  }

  @Override
  @Nonnull
  protected IMicroDocument createWriteData ()
  {
    final IMicroDocument aDoc = new MicroDocument ();
    final IMicroElement eRoot = aDoc.appendElement (ELEMENT_ROOT);
    for (final ISMPServiceGroup aSMPServiceGroup : CollectionHelper.getSortedByKey (m_aMap).values ())
      eRoot.appendChild (MicroTypeConverter.convertToMicroElement (aSMPServiceGroup, ELEMENT_ITEM));
    return aDoc;
  }

  private void _addSMPServiceGroup (@Nonnull final SMPServiceGroup aSMPServiceGroup, final boolean bUpdate)
  {
    ValueEnforcer.notNull (aSMPServiceGroup, "SMPServiceGroup");

    final String sSMPServiceGroupID = aSMPServiceGroup.getID ();
    if (!bUpdate && m_aMap.containsKey (sSMPServiceGroupID))
      throw new IllegalArgumentException ("SMPServiceGroup ID '" + sSMPServiceGroupID + "' is already in use!");
    m_aMap.put (aSMPServiceGroup.getID (), aSMPServiceGroup);
  }

  @Nonnull
  public SMPServiceGroup createSMPServiceGroup (@Nonnull @Nonempty final String sOwnerID,
                                                @Nullable @Nonnull final IParticipantIdentifier aParticipantIdentifier,
                                                @Nullable final String sExtension)
  {
    s_aLogger.info ("createSMPServiceGroup (" +
                    sOwnerID +
                    ", " +
                    IdentifierHelper.getIdentifierURIEncoded (aParticipantIdentifier) +
                    ", " +
                    (StringHelper.hasText (sExtension) ? "with extension" : "without extension") +
                    ")");

    final SMPServiceGroup aSMPServiceGroup = new SMPServiceGroup (sOwnerID, aParticipantIdentifier, sExtension);

    // It's a new service group - throws exception in case of an error
    m_aHook.createServiceGroup (aParticipantIdentifier);

    m_aRWLock.writeLock ().lock ();
    try
    {
      _addSMPServiceGroup (aSMPServiceGroup, false);
      markAsChanged (aSMPServiceGroup, EDAOActionType.CREATE);
    }
    catch (final RuntimeException ex)
    {
      // An error occurred - remove from SML again
      m_aHook.undoCreateServiceGroup (aParticipantIdentifier);
      throw ex;
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    AuditHelper.onAuditCreateSuccess (SMPServiceGroup.OT,
                                      aSMPServiceGroup.getID (),
                                      sOwnerID,
                                      IdentifierHelper.getIdentifierURIEncoded (aParticipantIdentifier),
                                      sExtension);
    s_aLogger.info ("createSMPServiceGroup succeeded");
    return aSMPServiceGroup;
  }

  @Nonnull
  public EChange updateSMPServiceGroup (@Nullable final String sSMPServiceGroupID,
                                        @Nonnull @Nonempty final String sNewOwnerID,
                                        @Nullable final String sExtension)
  {
    s_aLogger.info ("updateSMPServiceGroup (" +
                    sSMPServiceGroupID +
                    ", " +
                    sNewOwnerID +
                    ", " +
                    (StringHelper.hasText (sExtension) ? "with extension" : "without extension") +
                    ")");

    final SMPServiceGroup aSMPServiceGroup = _getSMPServiceGroupOfID (sSMPServiceGroupID);
    if (aSMPServiceGroup == null)
    {
      AuditHelper.onAuditModifyFailure (SMPServiceGroup.OT, sSMPServiceGroupID, "no-such-id");
      return EChange.UNCHANGED;
    }

    m_aRWLock.writeLock ().lock ();
    try
    {
      EChange eChange = EChange.UNCHANGED;
      eChange = eChange.or (aSMPServiceGroup.setOwnerID (sNewOwnerID));
      eChange = eChange.or (aSMPServiceGroup.setExtension (sExtension));
      if (eChange.isUnchanged ())
        return EChange.UNCHANGED;
      markAsChanged (aSMPServiceGroup, EDAOActionType.UPDATE);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    AuditHelper.onAuditModifySuccess (SMPServiceGroup.OT, "all", sSMPServiceGroupID, sNewOwnerID, sExtension);
    s_aLogger.info ("updateSMPServiceGroup succeeded");
    return EChange.CHANGED;
  }

  @Nonnull
  public EChange deleteSMPServiceGroup (@Nullable final IParticipantIdentifier aParticipantID)
  {
    s_aLogger.info ("deleteSMPServiceGroup (" + IdentifierHelper.getIdentifierURIEncoded (aParticipantID) + ")");

    final SMPServiceGroup aSMPServiceGroup = getSMPServiceGroupOfID (aParticipantID);
    if (aSMPServiceGroup == null)
    {
      AuditHelper.onAuditDeleteFailure (SMPServiceGroup.OT, "no-such-id", aParticipantID);
      return EChange.UNCHANGED;
    }

    // Delete in SML - throws exception in case of error
    m_aHook.deleteServiceGroup (aSMPServiceGroup.getParticpantIdentifier ());

    final ISMPRedirectManager aRedirectMgr = SMPMetaManager.getRedirectMgr ();
    final ISMPServiceInformationManager aServiceInfoMgr = SMPMetaManager.getServiceInformationMgr ();
    ICommonsCollection <? extends ISMPRedirect> aOldRedirects = null;
    ICommonsCollection <? extends ISMPServiceInformation> aOldServiceInformation = null;

    m_aRWLock.writeLock ().lock ();
    try
    {
      if (m_aMap.remove (aSMPServiceGroup.getID ()) == null)
      {
        AuditHelper.onAuditDeleteFailure (SMPServiceGroup.OT, "no-such-id", aSMPServiceGroup.getID ());
        return EChange.UNCHANGED;
      }

      // Save all redirects (in case of an error) and delete them
      aOldRedirects = aRedirectMgr.getAllSMPRedirectsOfServiceGroup (aSMPServiceGroup);
      aRedirectMgr.deleteAllSMPRedirectsOfServiceGroup (aSMPServiceGroup);

      // Save all service information (in case of an error) and delete them
      aOldServiceInformation = aServiceInfoMgr.getAllSMPServiceInformationsOfServiceGroup (aSMPServiceGroup);
      aServiceInfoMgr.deleteAllSMPServiceInformationOfServiceGroup (aSMPServiceGroup);

      markAsChanged (aSMPServiceGroup, EDAOActionType.DELETE);
    }
    catch (final RuntimeException ex)
    {
      // Deletion failed - shit

      // Try to rollback the actions
      if (!m_aMap.containsKey (aSMPServiceGroup.getID ()))
        _addSMPServiceGroup (aSMPServiceGroup, false);

      // Restore redirects (if any)
      if (CollectionHelper.isNotEmpty (aOldRedirects))
        for (final ISMPRedirect aOldRedirect : aOldRedirects)
          aRedirectMgr.createOrUpdateSMPRedirect (aSMPServiceGroup,
                                                  aOldRedirect.getDocumentTypeIdentifier (),
                                                  aOldRedirect.getTargetHref (),
                                                  aOldRedirect.getSubjectUniqueIdentifier (),
                                                  aOldRedirect.getExtension ());

      // Restore service information (if any)
      if (CollectionHelper.isNotEmpty (aOldServiceInformation))
        for (final ISMPServiceInformation aOldServiceInfo : aOldServiceInformation)
          aServiceInfoMgr.mergeSMPServiceInformation (aOldServiceInfo);

      // An error occurred - restore in SML again
      m_aHook.undoDeleteServiceGroup (aSMPServiceGroup.getParticpantIdentifier ());
      throw ex;
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    AuditHelper.onAuditDeleteSuccess (SMPServiceGroup.OT, aSMPServiceGroup.getID ());
    s_aLogger.info ("deleteSMPServiceGroup succeeded");
    return EChange.CHANGED;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsCollection <? extends ISMPServiceGroup> getAllSMPServiceGroups ()
  {
    return m_aRWLock.readLocked ( () -> m_aMap.copyOfValues ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsCollection <? extends ISMPServiceGroup> getAllSMPServiceGroupsOfOwner (@Nonnull final String sOwnerID)
  {
    final ICommonsList <ISMPServiceGroup> ret = new CommonsArrayList<> ();
    m_aRWLock.readLocked ( () -> CollectionHelper.findAll (m_aMap.values (),
                                                           aSG -> aSG.getOwnerID ().equals (sOwnerID),
                                                           ret::add));
    return ret;
  }

  @Nonnegative
  public int getSMPServiceGroupCountOfOwner (@Nonnull final String sOwnerID)
  {
    return m_aRWLock.readLocked ( () -> CollectionHelper.getCount (m_aMap.values (),
                                                                   aSG -> aSG.getOwnerID ().equals (sOwnerID)));
  }

  @Nullable
  private SMPServiceGroup _getSMPServiceGroupOfID (@Nullable final String sID)
  {
    if (StringHelper.hasNoText (sID))
      return null;

    return m_aRWLock.readLocked ( () -> m_aMap.get (sID));
  }

  public SMPServiceGroup getSMPServiceGroupOfID (@Nullable final IParticipantIdentifier aParticipantIdentifier)
  {
    if (aParticipantIdentifier == null)
      return null;

    final String sID = SMPServiceGroup.createSMPServiceGroupID (aParticipantIdentifier);
    return _getSMPServiceGroupOfID (sID);
  }

  public boolean containsSMPServiceGroupWithID (@Nullable final IParticipantIdentifier aParticipantIdentifier)
  {
    if (aParticipantIdentifier == null)
      return false;

    final String sID = SMPServiceGroup.createSMPServiceGroupID (aParticipantIdentifier);
    return m_aRWLock.readLocked ( () -> m_aMap.containsKey (sID));
  }

  @Nonnegative
  public int getSMPServiceGroupCount ()
  {
    return m_aRWLock.readLocked ( () -> m_aMap.size ());
  }
}
