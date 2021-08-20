/**
 * Copyright (C) 2015-2020 Philip Helger and contributors
 * philip[at]helger[dot]com
 *
 * The Original Code is Copyright The Peppol project (http://www.peppol.eu)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.helger.phoss.smp.domain.extension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.w3c.dom.Element;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.smpclient.bdxr1.utils.BDXR1ExtensionConverter;
import com.helger.xml.serialize.write.XMLWriter;

/**
 * Abstract implementation class for {@link ISMPHasExtension}. All extensions
 * are internally stored as instances of
 * {@link com.helger.xsds.bdxr.smp1.ExtensionType} since this the biggest data
 * type which can be used for Peppol SMP and BDXR SMP.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public abstract class AbstractSMPHasExtension implements ISMPHasExtension
{
  private final ICommonsList <com.helger.xsds.bdxr.smp1.ExtensionType> m_aExtensions = new CommonsArrayList <> ();

  protected AbstractSMPHasExtension ()
  {}

  @Nonnull
  @ReturnsMutableObject
  public final ICommonsList <com.helger.xsds.bdxr.smp1.ExtensionType> extensions ()
  {
    return m_aExtensions;
  }

  @Nullable
  public String getExtensionsAsString ()
  {
    if (m_aExtensions.isEmpty ())
      return null;
    return BDXR1ExtensionConverter.convertToString (m_aExtensions);
  }

  @Nullable
  public String getFirstExtensionXML ()
  {
    if (m_aExtensions.isEmpty ())
      return null;

    final Object aFirst = m_aExtensions.getFirst ().getAny ();
    if (!(aFirst instanceof Element))
      return null;

    // Use only the XML element of the first extension
    final Element aAny = (Element) aFirst;
    return XMLWriter.getNodeAsString (aAny);
  }

  @Nonnull
  public final EChange setExtensionAsString (@Nullable final String sExtension)
  {
    ICommonsList <com.helger.xsds.bdxr.smp1.ExtensionType> aNewExt = null;
    if (StringHelper.hasText (sExtension))
    {
      // Soft migration :)
      if (sExtension.charAt (0) == '<')
        aNewExt = BDXR1ExtensionConverter.convertXMLToSingleExtension (sExtension);
      else
        aNewExt = BDXR1ExtensionConverter.convert (sExtension);
    }
    if (m_aExtensions.equals (aNewExt))
      return EChange.UNCHANGED;
    m_aExtensions.setAll (aNewExt);
    return EChange.CHANGED;
  }

  @Nullable
  @ReturnsMutableCopy
  public com.helger.xsds.peppol.smp1.ExtensionType getAsPeppolExtension ()
  {
    if (m_aExtensions.isEmpty ())
      return null;

    // Use only the XML element of the first extension
    final com.helger.xsds.peppol.smp1.ExtensionType ret = new com.helger.xsds.peppol.smp1.ExtensionType ();
    ret.setAny ((Element) m_aExtensions.getFirst ().getAny ());
    return ret;
  }

  @Nullable
  @ReturnsMutableCopy
  public ICommonsList <com.helger.xsds.bdxr.smp1.ExtensionType> getAsBDXRExtension ()
  {
    if (m_aExtensions.isEmpty ())
      return null;

    return m_aExtensions.getClone ();
  }

  @Nullable
  @ReturnsMutableCopy
  public com.helger.xsds.bdxr.smp2.ec.SMPExtensionsType getAsBDXR2Extension ()
  {
    if (m_aExtensions.isEmpty ())
      return null;

    // TODO BDXR2 Extensions
    return null;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;

    final AbstractSMPHasExtension rhs = (AbstractSMPHasExtension) o;
    return m_aExtensions.equals (rhs.m_aExtensions);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aExtensions).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Extensions", m_aExtensions).getToString ();
  }
}
