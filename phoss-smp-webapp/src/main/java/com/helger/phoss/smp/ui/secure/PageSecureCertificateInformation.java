/**
 * Copyright (C) 2014-2021 Philip Helger and contributors
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
package com.helger.phoss.smp.ui.secure;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.html.grouping.HCOL;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.pd.client.PDClientConfiguration;
import com.helger.peppol.utils.PeppolKeyStoreHelper;
import com.helger.phoss.smp.SMPServerConfiguration;
import com.helger.phoss.smp.app.SMPWebAppConfiguration;
import com.helger.phoss.smp.domain.SMPMetaManager;
import com.helger.phoss.smp.security.SMPKeyManager;
import com.helger.phoss.smp.security.SMPTrustManager;
import com.helger.phoss.smp.ui.AbstractSMPWebPage;
import com.helger.phoss.smp.ui.SMPCommonUI;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.buttongroup.BootstrapButtonToolbar;
import com.helger.photon.bootstrap4.nav.BootstrapTabBox;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.security.keystore.LoadedKey;
import com.helger.security.keystore.LoadedKeyStore;

/**
 * This page displays information about the certificate configured in the SMP
 * Server configuration file.
 *
 * @author Philip Helger
 */
public final class PageSecureCertificateInformation extends AbstractSMPWebPage
{
  private enum EPredefinedCert
  {
    // PEPPOL PKI v2
    PEPPOL_PILOT_V2 ("Peppol pilot v2",
                     "CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA,OU=FOR TEST PURPOSES ONLY,O=NATIONAL IT AND TELECOM AGENCY,C=DK",
                     3,
                     true),
    PEPPOL_PRODUCTION_V2 ("Peppol production v2",
                          "CN=PEPPOL SERVICE METADATA PUBLISHER CA, O=NATIONAL IT AND TELECOM AGENCY, C=DK",
                          3,
                          true),
    // PEPPOL PKI v3
    PEPPOL_PILOT_V3 ("Peppol pilot v3",
                     "CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA - G2,OU=FOR TEST ONLY,O=OpenPEPPOL AISBL,C=BE",
                     3,
                     false),
    PEPPOL_PRODUCTION_V3 ("Peppol production v3", "CN=PEPPOL SERVICE METADATA PUBLISHER CA - G2,O=OpenPEPPOL AISBL,C=BE", 3, false),
    // TOOP Pilot PKI
    TOOP_PILOT_SMP ("TOOP pilot", "CN=TOOP PILOTS TEST SMP CA,OU=CCTF,O=TOOP,ST=Belgium,C=EU", 3, false);

    private final String m_sName;
    private final String m_sIssuer;
    private final int m_nCerts;
    private boolean m_bDeprecated;

    /**
     * @param sName
     *        Display name
     * @param sIssuer
     *        Required issuer
     * @param nCerts
     *        Required depth of PKI
     */
    EPredefinedCert (@Nonnull @Nonempty final String sName,
                     @Nonnull @Nonempty final String sIssuer,
                     @Nonnegative final int nCerts,
                     final boolean bDeprecated)
    {
      m_sName = sName;
      m_sIssuer = sIssuer;
      m_nCerts = nCerts;
      m_bDeprecated = bDeprecated;
    }

    @Nonnull
    @Nonempty
    public String getName ()
    {
      return m_sName;
    }

    @Nonnegative
    public int getCertificateTreeLength ()
    {
      return m_nCerts;
    }

    public boolean isDeprecated ()
    {
      return m_bDeprecated;
    }

    @Nullable
    public static EPredefinedCert getFromIssuerOrNull (@Nullable final String sIssuer)
    {
      if (StringHelper.hasText (sIssuer))
        for (final EPredefinedCert e : values ())
          if (e.m_sIssuer.equals (sIssuer))
            return e;
      return null;
    }
  }

  private static final String ACTION_RELOAD_KEYSTORE = "reloadkeystore";
  private static final String ACTION_RELOAD_TRUSTSTORE = "reloadtruststore";
  private static final String ACTION_RELOAD_DIRECTORY_CONFIGURATION = "reloadpdconfig";

  public PageSecureCertificateInformation (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Certificate information");
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();
    final ZonedDateTime aNowZDT = PDTFactory.getCurrentZonedDateTime ();
    final LocalDateTime aNowLDT = aNowZDT.toLocalDateTime ();
    final String sDirectoryName = SMPWebAppConfiguration.getDirectoryName ();

    if (aWPEC.hasAction (ACTION_RELOAD_KEYSTORE))
    {
      SMPKeyManager.reloadFromConfiguration ();
      aWPEC.postRedirectGetInternal (info ("The keystore was updated from the configuration at " +
                                           DateTimeFormatter.ISO_DATE_TIME.format (aNowZDT) +
                                           ". The changes are reflected below."));
    }
    else
      if (aWPEC.hasAction (ACTION_RELOAD_TRUSTSTORE))
      {
        SMPTrustManager.reloadFromConfiguration ();
        aWPEC.postRedirectGetInternal (info ("The truststore was updated from the configuration at " +
                                             DateTimeFormatter.ISO_DATE_TIME.format (aNowZDT) +
                                             ". The changes are reflected below."));
      }
      else
        if (aWPEC.hasAction (ACTION_RELOAD_DIRECTORY_CONFIGURATION))
        {
          PDClientConfiguration.reloadConfiguration ();
          aWPEC.postRedirectGetInternal (info ("The " +
                                               sDirectoryName +
                                               " configuration was reloaded at " +
                                               DateTimeFormatter.ISO_DATE_TIME.format (aNowZDT) +
                                               ". The changes are reflected below."));
        }

    {
      final BootstrapButtonToolbar aToolbar = new BootstrapButtonToolbar (aWPEC);
      aToolbar.addChild (new BootstrapButton ().addChild ("Reload keystore")
                                               .setIcon (EDefaultIcon.REFRESH)
                                               .setOnClick (aWPEC.getSelfHref ().add (CPageParam.PARAM_ACTION, ACTION_RELOAD_KEYSTORE)));
      aToolbar.addChild (new BootstrapButton ().addChild ("Reload truststore")
                                               .setIcon (EDefaultIcon.REFRESH)
                                               .setOnClick (aWPEC.getSelfHref ().add (CPageParam.PARAM_ACTION, ACTION_RELOAD_TRUSTSTORE)));
      if (SMPMetaManager.getSettings ().isDirectoryIntegrationEnabled ())
      {
        aToolbar.addChild (new BootstrapButton ().addChild ("Reload " + sDirectoryName + " configuration")
                                                 .setIcon (EDefaultIcon.REFRESH)
                                                 .setOnClick (aWPEC.getSelfHref ()
                                                                   .add (CPageParam.PARAM_ACTION, ACTION_RELOAD_DIRECTORY_CONFIGURATION)));
      }
      aNodeList.addChild (aToolbar);
    }

    final BootstrapTabBox aTabBox = aNodeList.addAndReturnChild (new BootstrapTabBox ());

    // key store
    {
      final HCNodeList aTab = new HCNodeList ();
      if (!SMPKeyManager.isKeyStoreValid ())
      {
        aTab.addChild (error (SMPKeyManager.getInitializationError ()));
      }
      else
      {
        // Successfully loaded private key
        final SMPKeyManager aKeyMgr = SMPKeyManager.getInstance ();
        final PrivateKeyEntry aKeyEntry = aKeyMgr.getPrivateKeyEntry ();
        if (aKeyEntry != null)
        {
          final Certificate [] aChain = aKeyEntry.getCertificateChain ();

          // Key store path and password are fine
          aTab.addChild (success (div ("Keystore is located at '" +
                                       SMPServerConfiguration.getKeyStorePath () +
                                       "' and was successfully loaded.")).addChild (div ("The private key with the alias '" +
                                                                                         SMPServerConfiguration.getKeyStoreKeyAlias () +
                                                                                         "' was successfully loaded.")));

          if (aChain.length > 0 && aChain[0] instanceof X509Certificate)
          {
            final X509Certificate aHead = (X509Certificate) aChain[0];
            final String sIssuer = aHead.getIssuerX500Principal ().getName ();
            final EPredefinedCert eCert = EPredefinedCert.getFromIssuerOrNull (sIssuer);
            if (eCert != null)
            {
              if (eCert.isDeprecated ())
                aTab.addChild (warn ("You are currently using a ").addChild (strong ("deprecated"))
                                                                  .addChild (" " + eCert.getName () + " certificate!"));
              else
                aTab.addChild (info ("You are currently using a " + eCert.getName () + " certificate!"));
              if (aChain.length != eCert.getCertificateTreeLength ())
                aTab.addChild (error ("The private key should be a chain of " +
                                      eCert.getCertificateTreeLength () +
                                      " certificates but it has " +
                                      aChain.length +
                                      " certificates. Please ensure that the respective root certificates are contained correctly!"));
            }
            // else: we don't care
          }

          final String sAlias = SMPServerConfiguration.getKeyStoreKeyAlias ();
          final HCOL aOL = new HCOL ();
          for (final Certificate aCert : aChain)
          {
            if (aCert instanceof X509Certificate)
            {
              final X509Certificate aX509Cert = (X509Certificate) aCert;
              final BootstrapTable aCertDetails = SMPCommonUI.createCertificateDetailsTable (sAlias, aX509Cert, aNowLDT, aDisplayLocale);
              aOL.addItem (aCertDetails);
            }
            else
              aOL.addItem ("The certificate is not an X.509 certificate! It is internally a " + ClassHelper.getClassName (aCert));
          }
          aTab.addChild (aOL);
        }
      }
      aTabBox.addTab ("keystore", "Keystore", aTab);
    }

    // Trust store
    {
      final HCNodeList aTab = new HCNodeList ();
      if (!SMPTrustManager.isTrustStoreValid ())
      {
        aTab.addChild (warn (SMPTrustManager.getInitializationError ()));
      }
      else
      {
        // Successfully loaded trust store
        final SMPTrustManager aTrustMgr = SMPTrustManager.getInstance ();
        final KeyStore aTrustStore = aTrustMgr.getTrustStore ();

        // Trust store path and password are fine
        aTab.addChild (success (div ("Truststore is located at '" +
                                     SMPServerConfiguration.getTrustStorePath () +
                                     "' and was successfully loaded.")));

        final HCOL aOL = new HCOL ();
        try
        {
          for (final String sAlias : CollectionHelper.newList (aTrustStore.aliases ()))
          {
            final Certificate aCert = aTrustStore.getCertificate (sAlias);
            if (aCert instanceof X509Certificate)
            {
              final X509Certificate aX509Cert = (X509Certificate) aCert;
              final BootstrapTable aCertDetails = SMPCommonUI.createCertificateDetailsTable (sAlias, aX509Cert, aNowLDT, aDisplayLocale);
              aOL.addItem (aCertDetails);
            }
            else
              aOL.addItem ("The certificate is not an X.509 certificate! It is internally a " + ClassHelper.getClassName (aCert));
          }
        }
        catch (final GeneralSecurityException ex)
        {
          aOL.addItem (error ("Error iterating trust store.").addChild (SMPCommonUI.getTechnicalDetailsUI (ex)));
        }
        aTab.addChild (aOL);
      }
      aTabBox.addTab ("truststore", "Truststore", aTab);
    }

    // Peppol Directory client certificate
    if (SMPMetaManager.getSettings ().isDirectoryIntegrationEnabled ())
    {
      // Directory client keystore
      {
        final HCNodeList aTab = new HCNodeList ();

        final LoadedKeyStore aKeyStoreLR = PDClientConfiguration.loadKeyStore ();
        if (aKeyStoreLR.isFailure ())
        {
          aTab.addChild (error (PeppolKeyStoreHelper.getLoadError (aKeyStoreLR)));
        }
        else
        {
          final String sKeyStorePath = PDClientConfiguration.getKeyStorePath ();
          final LoadedKey <KeyStore.PrivateKeyEntry> aKeyLoading = PDClientConfiguration.loadPrivateKey (aKeyStoreLR.getKeyStore ());
          if (aKeyLoading.isFailure ())
          {
            aTab.addChild (success (div ("Keystore is located at '" + sKeyStorePath + "' and was successfully loaded.")));
            aTab.addChild (error (PeppolKeyStoreHelper.getLoadError (aKeyLoading)));
          }
          else
          {
            // Successfully loaded private key
            final String sAlias = PDClientConfiguration.getKeyStoreKeyAlias ();
            final PrivateKeyEntry aKeyEntry = aKeyLoading.getKeyEntry ();
            final Certificate [] aChain = aKeyEntry.getCertificateChain ();

            // Key store path and password are fine
            aTab.addChild (success (div ("Keystore is located at '" +
                                         sKeyStorePath +
                                         "' and was successfully loaded.")).addChild (div ("The private key with the alias '" +
                                                                                           sAlias +
                                                                                           "' was successfully loaded.")));

            if (aChain.length > 0 && aChain[0] instanceof X509Certificate)
            {
              final X509Certificate aHead = (X509Certificate) aChain[0];
              final String sIssuer = aHead.getIssuerX500Principal ().getName ();
              final EPredefinedCert eCert = EPredefinedCert.getFromIssuerOrNull (sIssuer);
              if (eCert != null)
              {
                if (eCert.isDeprecated ())
                {
                  aTab.addChild (warn ("You are currently using a ").addChild (strong ("deprecated"))
                                                                    .addChild (" " + eCert.getName () + " certificate!"));
                }
                else
                  aTab.addChild (info ("You are currently using a " + eCert.getName () + " certificate!"));
                if (aChain.length != eCert.getCertificateTreeLength ())
                  aTab.addChild (error ("The private key should be a chain of " +
                                        eCert.getCertificateTreeLength () +
                                        " certificates but it has " +
                                        aChain.length +
                                        " certificates. Please ensure that the respective root certificates are contained!"));
              }
              // else: we don't care
            }

            final HCOL aUL = new HCOL ();
            for (final Certificate aCert : aChain)
            {
              if (aCert instanceof X509Certificate)
              {
                final X509Certificate aX509Cert = (X509Certificate) aCert;
                final BootstrapTable aCertDetails = SMPCommonUI.createCertificateDetailsTable (sAlias, aX509Cert, aNowLDT, aDisplayLocale);
                aUL.addItem (aCertDetails);
              }
              else
                aUL.addItem ("The certificate is not an X.509 certificate! It is internally a " + ClassHelper.getClassName (aCert));
            }
            aTab.addChild (aUL);
          }
        }
        aTabBox.addTab ("pdkeystore", sDirectoryName + " Keystore", aTab);
      }

      // Directory client truststore
      {
        final HCNodeList aTab = new HCNodeList ();

        final LoadedKeyStore aTrustStoreLR = PDClientConfiguration.loadTrustStore ();
        if (aTrustStoreLR.isFailure ())
        {
          aTab.addChild (error (PeppolKeyStoreHelper.getLoadError (aTrustStoreLR)));
        }
        else
        {
          // Successfully loaded trust store
          final String sTrustStorePath = PDClientConfiguration.getTrustStorePath ();
          final KeyStore aTrustStore = aTrustStoreLR.getKeyStore ();

          // Trust store path and password are fine
          aTab.addChild (success (div ("Truststore is located at '" + sTrustStorePath + "' and was successfully loaded.")));

          final HCOL aOL = new HCOL ();
          try
          {
            for (final String sAlias : CollectionHelper.newList (aTrustStore.aliases ()))
            {
              final Certificate aCert = aTrustStore.getCertificate (sAlias);
              if (aCert instanceof X509Certificate)
              {
                final X509Certificate aX509Cert = (X509Certificate) aCert;
                final BootstrapTable aCertDetails = SMPCommonUI.createCertificateDetailsTable (sAlias, aX509Cert, aNowLDT, aDisplayLocale);
                aOL.addItem (aCertDetails);
              }
              else
                aOL.addItem ("The certificate is not an X.509 certificate! It is internally a " + ClassHelper.getClassName (aCert));
            }
          }
          catch (final GeneralSecurityException ex)
          {
            aOL.addItem (error ("Error iterating trust store.").addChild (SMPCommonUI.getTechnicalDetailsUI (ex)));
          }
          aTab.addChild (aOL);
        }
        aTabBox.addTab ("pdtruststore", sDirectoryName + " Truststore", aTab);
      }
    }
  }
}
