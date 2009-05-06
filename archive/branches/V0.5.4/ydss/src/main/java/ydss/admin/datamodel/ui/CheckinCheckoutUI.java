/*
 * Copyright 2006 The ydss Team
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package ydss.admin.datamodel.ui;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.Field;
import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.servlet.RequestHandler;
import org.efaps.util.EFapsException;

import ydss.admin.access.AccessCheckOnInstanceAndType;

/**
 * @author mbu
 * @version $Id$
 */
public class CheckinCheckoutUI implements UIInterface  {

    /**
     * @param _locale   locale object
     */
    public String getViewHtml(Context _context, Object _value, Field _field)
        throws EFapsException
    {
        StringBuilder ret = new StringBuilder("");

        String sOid = _value.toString().trim();
        Instance iAbstract = new Instance(sOid);

        boolean isDocumentType = "YDSS_Document".equals(iAbstract.getType().getName());
        boolean isLocked = false;
        boolean isLocker = false;
        String  sVersionOId = "";
        if (isDocumentType)
        {
            SearchQuery query = new SearchQuery();
            query.setObject(iAbstract);
            query.addSelect("Locked");
            query.addSelect("Locker");
            query.addSelect("DocumentVersion.OID");
            query.execute();

            query.next();
            isLocked = ((Boolean) query.get("Locked")).booleanValue();
            if (isLocked)
            {
                isLocker = ((Person) query.get("Locker")).getId() == _context.getPerson().getId();
            }
            sVersionOId = query.get("DocumentVersion.OID").toString();
        }

        String sCheckoutIconActiveURL  = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssCheckinUnlock.gif");
        String sCheckoutIconPassiveURL = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssCheckinUnlockGrey.gif");
        String sCheckinIconActiveURL   = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssCheckoutLock.gif");
        String sCheckinIconPassiveURL  = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssCheckoutLockGrey.gif");
        String sLockIconActiveURL      = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssLock.gif");
        String sLockIconPassiveURL     = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssLockGrey.gif");
        String sUnlockIconActiveURL    = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssUnlock.gif");
        String sUnlockIconPassiveURL   = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssUnlockGrey.gif");
        String sViewIconActiveURL      = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssViewDocument.gif");
        String sViewIconPassiveURL     = RequestHandler.replaceMacrosInUrl("${ROOTURL}/images/ydssViewDocumentGrey.gif");
        String sCheckoutURL            = RequestHandler.replaceMacrosInUrl("${ROOTURL}/ydss/CheckoutLock.jsp");
        String sLockURL                = RequestHandler.replaceMacrosInUrl("${ROOTURL}/ydss/Lock.jsf");
        String sUnlockURL              = RequestHandler.replaceMacrosInUrl("${ROOTURL}/ydss/Unlock.jsf");
        String sViewURL                = RequestHandler.replaceMacrosInUrl("${ROOTURL}/servlet/checkout");
        StringBuilder sbCheckinURL     = new StringBuilder(RequestHandler.replaceMacrosInUrl("${ROOTURL}/common/Link.jsp"));
        sbCheckinURL.append("urlProcess=../ydss/CheckinUnlock.jsp&command=YDSS_DocumentTree_Menu_Action_Checkin&");
        String sCheckinURL             = sbCheckinURL.toString();

        AccessType atCheckIn  = AccessType.getAccessType("checkin");
        AccessType atCheckOut = AccessType.getAccessType("checkout");
        AccessType atModify   = AccessType.getAccessType("modify");
        AccessType atRead     = AccessType.getAccessType("read");

        AccessCheckOnInstanceAndType checker = new AccessCheckOnInstanceAndType();
        boolean hasCheckOutAccess     = checker.checkAccess(iAbstract, atCheckOut, false);
        boolean hasCheckInAccess      = checker.checkAccess(iAbstract, atCheckIn, false);
        boolean hasModifyAccess       = checker.checkAccess(iAbstract, atModify, false);
        boolean hasReadAccess         = checker.checkAccess(iAbstract, atRead, false);
        boolean hasCheckOutLockRight  = hasCheckOutAccess && hasModifyAccess && !isLocked;
        boolean hasCheckInUnlockRight = hasCheckInAccess && hasModifyAccess && isLocked && isLocker;
        boolean hasLockRight          = hasModifyAccess && !isLocked;
        boolean hasUnlockRight        = hasModifyAccess && isLocked;
        boolean hasReadRight          = hasReadAccess && hasCheckOutAccess;

        if (isDocumentType)
        {
            if (hasCheckOutLockRight)
            {
                ret.append("<a href=\"javascript:eFapsCommonOpenUrl('")
                   .append(sCheckoutURL).append("oid=").append(sOid)
                   .append("','Popup');\"><img src=\"")
                   .append(sCheckoutIconActiveURL)
                   .append("\" title=\"Checkout and Lock\" alt=\"Checkout and Lock\" border=0></a>");
            }
            else
            {
                ret.append("<img src=\"")
                   .append(sCheckoutIconPassiveURL)
                   .append("\" title=\"no checkout\" alt=\"No Checkout.\" border=0>");
            }
            ret.append("&nbsp;");
            if (hasCheckInUnlockRight)
            {
                ret.append("<a href=\"javascript:eFapsCommonOpenUrl('")
                   .append(sCheckinURL).append("oid=").append(sOid)
                   .append("','Popup');\"><img src=\"")
                   .append(sCheckinIconActiveURL)
                   .append("\" title=\"Checkin and Unlock\" alt=\"Checkin and Unlock\" border=0></a>");
            }
            else
            {
                ret.append("<img src=\"")
                   .append(sCheckinIconPassiveURL)
                   .append("\" title=\"no checkin\" alt=\"No Checkin.\" border=0>");
            }
            ret.append("&nbsp;");
            if (hasLockRight)
            {
                ret.append("<a href=\"javascript:eFapsCommonOpenUrl('")
                   .append(sLockURL).append("oid=").append(sOid)
                   .append("','eFapsFrameHidden');\"><img src=\"")
                   .append(sLockIconActiveURL)
                   .append("\" title=\"Lock\" alt=title=\"Lock\" border=0></a>");
            }
            else
            {
                ret.append("<img src=\"")
                   .append(sLockIconPassiveURL)
                   .append("\" title=\"no lock\" alt=title=\"No Lock\" border=0>");
            }
            ret.append("&nbsp;");
            if (hasUnlockRight)
            {
                ret.append("<a href=\"javascript:eFapsCommonOpenUrl('")
                   .append(sUnlockURL).append("oid=").append(sOid)
                   .append("','eFapsFrameHidden');\"><img src=\"")
                   .append(sUnlockIconActiveURL)
                   .append("\" title=\"Unlock\" alt=title=\"Unlock\" border=0></a>");
            }
            else
            {
                ret.append("<img src=\"")
                   .append(sUnlockIconPassiveURL)
                   .append("\" title=\"no unlock\" alt=title=\"No Unlock\" border=0>");
            }
            ret.append("&nbsp;");
            if (hasReadRight)
            {
                ret.append("<a href=\"javascript:eFapsCommonOpenUrl('")
                   .append(sViewURL).append("oid=").append(sVersionOId)
                   .append("','Popup');\"><img src=\"")
                   .append(sViewIconActiveURL)
                   .append("\" title=\"View\" alt=title=\"View\" border=0></a>");
            }
            else
            {
                ret.append("<img src=\"")
                   .append(sViewIconPassiveURL)
                   .append("\" title=\"no read\" alt=title=\"No Read\" border=0>");
            }
        }

        return ret.toString();
    }

    /**
     * @param _locale   locale object
     */
    public String getEditHtml(Context _context, Object _value, Field _field) 
        throws EFapsException  
    {
        return "";
    }

    /**
     * @param _locale   locale object
     */
    public String getCreateHtml(Context _context, Object _value, Field _field)
    {
        return "";
    }

    /**
     * @param _locale   locale object
     */
    public String getSearchHtml(Context _context, Object _value, Field _field)
    {
        return "";
    }
}
