/*
 * Copyright 2006 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package ydss.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.SimpleEmail;

import org.efaps.admin.access.AccessCheckInterface;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

// TODO: introduce AccessSetCollections to enable Typpe specific differences

/**
 * @author mbu
 * @version $Id$
 * @todo description
 */
public class AccessCheckOnInstanceAndType implements AccessCheckInterface
{

    ////////////////////////////////////////////////////////////////////////////
    // static variables

    /**
     * Logging instance used in this class.
     */
    private static final Log LOG 
                        = LogFactory.getLog(AccessCheckOnInstanceAndType.class);

    ////////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * Check for the instance object if the current context user has the
     * access defined in the list of access types.
     */
    public boolean checkAccess(final Instance   _instance, 
                               final AccessType _accessType) 
        throws EFapsException
    {
        return checkAccess(_instance, _accessType, true);
    }

    /**
     * Check for the instance object if the current context user has the
     * access defined in the list of access types.
     */
    public boolean checkAccess(final Instance   _instance, 
                               final AccessType _accessType,
                               final boolean    _notifySubscriptions)
        throws EFapsException
    {
        Instance instance     = _instance;
        AccessType accessType = _accessType;
        boolean bHasAccess = false;
        Context context = Context.getThreadContext();
        boolean isAdmin = false;

        StringBuilder users = new StringBuilder();
        users.append(context.getPersonId());
        for (Role role : context.getPerson().getRoles())
        {
            users.append(",").append(role.getId());

            // dirty fix for role "Administration"
            if (role.getId()==2)
            {
                isAdmin = true;
                bHasAccess = true;
            }
        }
        for (Group group : context.getPerson().getGroups())
        {
            users.append(",").append(group.getId());
        }

        // calculate the instance to evaluate the access on
        boolean isShare      = "YDSS_Share".equals(_instance.getType().getName());
        boolean isDocVersion = "YDSS_DocumentVersion".equals(_instance.getType().getName());
        String sInstanceId = "0";
        if (!bHasAccess)
        {
            String sType = _instance.getType().getName();
            if ("create".equals(accessType.getName()))
            {
                // everyone may add a share
                if (isShare)
                {
                    bHasAccess = true;
                }
                else
                {
                    String[] sOidElements = context.getParameter("oid").split("\\.");
                    sInstanceId = String.valueOf(sOidElements[sOidElements.length-1]);
                }
            }
            else
            {
                // Evaluate DocumentVersion Access on parent Document
                if ("YDSS_DocumentVersion".equals(sType))
                {
                    SearchQuery query = new SearchQuery();
                    query.setObject(_instance);
                    query.addSelect("Document");
                    query.executeWithoutAccessCheck();
                    query.next();
                    sInstanceId = query.get("Document").toString();
                    instance = new Instance("YDSS_Document", sInstanceId);
                }
                else
                {
                    sInstanceId   = String.valueOf(_instance.getId());
                }
            }
        }

        StringBuilder sbCacheKey = new StringBuilder("AccessCheck_");
        String        sCacheKey  = sbCacheKey.append(_instance.getType().getName())
                                             .append("_")
                                             .append(sInstanceId)
                                             .append("_")
                                             .append(accessType.getName())
                                             .toString();
System.out.println("sCacheKey = "+sCacheKey);
System.out.println("context.containsRequestAttribute(sCacheKey) = "+context.containsRequestAttribute(sCacheKey));
        if (context.containsRequestAttribute(sCacheKey))
        {
            bHasAccess = ((Boolean)context.getRequestAttribute(sCacheKey)).booleanValue();
        }
        else
        {
            // get needed infos on the instance
            boolean isStateAdmin    = false;
            boolean isStateActive   = false;
            boolean isStateArchived = false;
            boolean isLocked        = false;
            long    lInstLockerId   = -1;
            String  sInstParentOId  = "";
            String  sInstState      = "";
            String  sInstName       = "?undefined?";

            if ("create".equals(accessType.getName()))
            {
                if (!isShare)
                {
                    sInstParentOId = context.getParameter("oid");
                }
                if ("YDSS_DocumentVersion".equals(instance.getType().getName())
                    && "YDSS_Document".equals(new Instance(sInstParentOId).getType().getName()))
                {
                    // convert "create YDSS_DocumentVersion" to "checkin YDSS_Document" 
                    accessType  = AccessType.getAccessType("checkin");
                    instance    = new Instance(sInstParentOId);
                }
            }
            
            if (!"create".equals(accessType.getName()))
            {
                SearchQuery query = new SearchQuery();
                query.setObject(instance);
                query.addSelect("Locked");
                query.addSelect("Locker");
                query.addSelect("Parent.OID");
                query.addSelect("State");
                query.executeWithoutAccessCheck();
                query.next();

                if (query.get("Locked") != null)
                {
                    isLocked = ((Boolean) query.get("Locked")).booleanValue();
                }
                if (query.get("Locker") != null)
                {
                    lInstLockerId = ((Person) query.get("Locker")).getId();
                }
                if (query.get("Parent.OID") != null)
                {
                    sInstParentOId = query.get("Parent.OID").toString();
                }
                if (query.get("State") != null)
                {
                    sInstState      = query.get("State").toString().trim();
                    isStateAdmin    = "Admin".equals(sInstState);
                    isStateActive   = "Active".equals(sInstState);
                    isStateArchived = "Archived".equals(sInstState);
                }
            }

            List<Long> lASonInstance4User = null;
            if (!bHasAccess)
            {
                lASonInstance4User = getASonInstance4User(context, sInstanceId, users);
            }

            // Basic Access evaluation
            boolean isShareHolder = false;
            if (!bHasAccess)
            {
                for (Long lngAccessSetId : lASonInstance4User)
                {
                    AccessSet as = AccessSet.getAccessSet(lngAccessSetId);
                    isShareHolder = isShareHolder || "Share Holder".equals(as.getName());
                    bHasAccess = as.getAccessTypes().contains(accessType) && 
                                 as.getDataModelTypes().contains(instance.getType()) ||
                                 bHasAccess;
                }
            }
        
            // very dirty fix of conceptional access problem
            if (!bHasAccess && "show".equals(accessType.getName()) && lASonInstance4User != null && lASonInstance4User.size() > 0)
            {
                bHasAccess = true;
            }

            //Basic Access interpreted by State
            if (isStateAdmin)
            {
                // only a share holder or the Admin has access
                bHasAccess = bHasAccess && (isShareHolder || isAdmin);
            }
            else if (isStateActive)
            {
                // basic access equals access for this state
            }
            else if (isStateArchived)
            {
                // only a share holder has access and only for read and show
                bHasAccess = isAdmin
                             || bHasAccess 
                             && isShareHolder 
                             && ("view".equals(accessType.getName())
                                 || "show".equals(accessType.getName())
                                 || (isShare && "modify".equals(accessType.getName())));
            }


            // Additional conditions apply to some use cases
            // 1. a checkin is only possible on a document, the
            //    Document has to be locked and the user has to be the locker,
            //    a share holder or the Admin
            if (bHasAccess && "checkin".equals(accessType.getName()))
            {

                bHasAccess = "YDSS_Document".equals(instance.getType().getName())
                             && isLocked
                             && (isAdmin
                                 || isShareHolder
                                 || lInstLockerId == context.getPerson().getId());
            }
            // 2. a Document may only be modified if it is unlocked or the
            //    user is the Locker, a ShareHolder or the Admin
            if (bHasAccess 
                && "YDSS_Document".equals(instance.getType().getName())
                && "modify".equals(accessType.getName()))
            {
                bHasAccess = !isLocked
                             || (isAdmin
                                || isShareHolder
                                || lInstLockerId == context.getPerson().getId());
            }
            // 3. a Document may only be created under a folder
            if (bHasAccess 
                && "create".equals(accessType.getName())
                && "YDSS_Document".equals(instance.getType().getName()))
            {
                String sParentType = "";
                if (sInstParentOId.length() > 0)
                {
                    Instance insParent = new Instance(sInstParentOId);
                    sParentType = insParent.getType().getName();
                }
                bHasAccess = "YDSS_Folder".equals(sParentType);
            }

            // trigger subscriptions
            if (bHasAccess && _notifySubscriptions)
            {
                try
                {
                    evaluateSubscriptions(context, instance, accessType);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    throw new EFapsException(getClass(), "evalSubscriptionsException", ex);
                }
            }
        }
        context.setRequestAttribute(sCacheKey, new Boolean(bHasAccess));

        return bHasAccess;
    }

    protected void evaluateSubscriptions(final Context       _context,
                                         final Instance      _instance,
                                         final AccessType    _accessType)
                         throws Exception
    {
        String        sType                = _instance.getType().getName();
        String        sAccessType          = _accessType.getName();
        StringBuilder sbWhat               = new StringBuilder();
        StringBuilder sbWhere              = new StringBuilder();
        StringBuilder sbWho                = new StringBuilder();
        List<String>  lSubsciptionAdresses = new Vector();

        if (
             ( 
               ( 
                 ( "YDSS_Share".equals(sType) ||
                   "YDSS_Folder".equals(sType) ||
                   "YDSS_Document".equals(sType)
                 ) &&
                 ( "create".equals(sAccessType) || 
                   "delete".equals(sAccessType)
                 )
               ) ||
               ( "YDSS_Document".equals(sType) && 
                 "modify".equals(sAccessType)
               )
             )
           )
        {
             // share,folder,document deleted
            if ("delete".equals(sAccessType))
            {
                // get subscriptions on object and parent
                lSubsciptionAdresses = getSubscriptionAdresses(_context, String.valueOf(_instance.getId()));
                sbWhat.append(_instance.getType().getName().substring(5))
                      .append(" '")
                      .append(getNameFromInstance(_context, _instance))
                      .append("'");

                String[] sOidElements = _context.getParameter("oid").split("\\.");
                String sInstanceId = sOidElements[sOidElements.length-1];

                if (!"YDSS_Share".equals(sType))
                {
                    List<String> lParSubscriptions = getSubscriptionAdresses(_context, sInstanceId);
                    for (String sParSubscription : lParSubscriptions)
                    {
                        if (!lSubsciptionAdresses.contains(sParSubscription))
                        {
                            lSubsciptionAdresses.add(sParSubscription);
                        }
                    }

                    Instance insParent = new Instance(_context.getParameter("oid"));
                    sbWhere.append(insParent.getType().getName().substring(5))
                           .append(" '")
                           .append(getNameFromInstance(_context, insParent))
                           .append("'");
                }
            }
            // folder,document created
            else if ( "create".equals(sAccessType) && 
                      ( "YDSS_Folder".equals(sType) || 
                        "YDSS_Document".equals(sType)
                      )
                    )
            {
                // get subscriptions on parent
                sbWhat.append(_instance.getType().getName().substring(5))
                      .append(" '")
                      .append(_context.getParameter("name"))
                      .append("'");

                String[] sOidElements = _context.getParameter("oid").split("\\.");
                String sInstanceId = sOidElements[sOidElements.length-1];
                lSubsciptionAdresses = getSubscriptionAdresses(_context, sInstanceId);

                Instance insParent = new Instance(_context.getParameter("oid"));
                sbWhere.append(insParent.getType().getName().substring(5))
                       .append(" '")
                       .append(getNameFromInstance(_context, insParent))
                       .append("'");
            }
            // document modified
            else if ("modify".equals(sAccessType) && "YDSS_Document".equals(sType))
            {
                // get subscriptions on object
                lSubsciptionAdresses = getSubscriptionAdresses(_context, String.valueOf(_instance.getId()));
                sbWhat.append(_instance.getType().getName().substring(5))
                      .append(" '")
                      .append(getNameFromInstance(_context, _instance))
                      .append("'");
            }
        }

        sbWho.append(_context.getPerson().getFirstName())
             .append(" ")
             .append(_context.getPerson().getLastName());

        if (lSubsciptionAdresses.size() > 0)
        {
            sendSubscriptionMails(lSubsciptionAdresses,
                                  sAccessType,
                                  sbWhat.toString(),
                                  sbWhere.toString(),
                                  sbWho.toString());
        }
    }

    protected String getNameFromInstance(final Context _context,
                                       final Instance _instance)
        throws Exception
    {
        String sInstanceName = "?undefined?";

        SearchQuery query = new SearchQuery();
        query.setObject(_context, _instance);
        query.addSelect(_context, "Name");
        query.execute();
        if (query.next())
        {
            sInstanceName = query.get(_context, "Name").toString();
        }

        return sInstanceName;
    }

    protected void sendSubscriptionMails(final List<String> _lAdresses,
                                         final String       _sAccessType,
                                         final String       _sWhat,
                                         final String       _sWhere,
                                         final String       _sWho)
        throws Exception
    {
        ResourceBundle       rb           = ResourceBundle.getBundle("StringResource");
        String               sHostName    = rb.getString("Subscription.MailConfig.SMTPHost").trim();
        String               sFrom        = rb.getString("Subscription.MailConfig.From").trim();
        String               sSubject     = rb.getString("Subscription.MailConfig.Subject").trim();
        String               sAuthReqired = rb.getString("Subscription.MailConfig.AuthenticationRequired").trim();
        String               sUserName    = "";
        String               sPassword    = "";

        if (sAuthReqired != null && "true".equalsIgnoreCase(sAuthReqired))
        {
            sUserName = rb.getString("Subscription.MailConfig.UserName").trim();
            sPassword = rb.getString("Subscription.MailConfig.Password").trim();
        }


        String sAccessVerb = "";
        if ("create".equals(_sAccessType))
        {
            sAccessVerb = "created";
        }
        else if ("delete".equals(_sAccessType))
        {
            sAccessVerb = "deleted";
        }
        else if ("modify".equals(_sAccessType))
        {
            sAccessVerb = "modified";
        }

        StringBuilder sbWhere = new StringBuilder();
        if (_sWhere.length() > 0)
        {
            sbWhere.append(" within ")
                   .append(_sWhere);
        }

        StringBuilder sbText = new StringBuilder();
        sbText.append("An event occured in ydss on an object you subscribed for:\n\n")
              .append(_sWhat)
              .append(" was ")
              .append(sAccessVerb)
              .append(sbWhere)
              .append(" by Member ")
              .append(_sWho)
              .append(".\n\n")
              .append("NOTE: This is an automatic generated eMail, do not reply.");

        for (String sAdress : _lAdresses)
        {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(sHostName);
            email.addTo(sAdress);
            email.setFrom(sFrom);
            email.setSubject(sSubject);
            email.setMsg(sbText.toString());
            if (sAuthReqired != null && "true".equalsIgnoreCase(sAuthReqired))
            {
                email.setAuthentication(sUserName, sPassword);
            }
            email.send();
        }
    }

    protected List<String> getSubscriptionAdresses(final Context _context,
                                                   final String  _sId)
                         throws EFapsException
    {
        List<String> lSubscriptionAdresses = new Vector<String>();

        StringBuilder cmd = new StringBuilder();
        cmd.append("select EMAIL from USERPERSON, T_YDSSSUBSCRIPTION where ")
           .append("(USERPERSON.ID = T_YDSSSUBSCRIPTION.USERABSTRACTFROM) AND ")
           .append("T_YDSSSUBSCRIPTION.SHAREFROM = ")
           .append(_sId);

        ConnectionResource cr = null;
        try
        {
            cr = _context.getConnectionResource();
            Statement stmt = null;
            try
            {
                stmt = cr.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(cmd.toString());

                String sSubscriptionAdress = null;
                while (rs.next())
                {
                    sSubscriptionAdress = rs.getString(1);
                    if (!lSubscriptionAdresses.contains(sSubscriptionAdress))
                    {
                        lSubscriptionAdresses.add(sSubscriptionAdress);
                    }
                }
                rs.close();
            }
            finally
            {
                if (stmt != null)
                {
                    stmt.close();
                }
            }
            cr.commit();
        }
        catch (SQLException e)
        {
            LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } 
        finally
        {
            if ((cr != null) && cr.isOpened())
            {
                cr.abort();
            }
        }

        return lSubscriptionAdresses;
    }

    protected List<Long> getASonInstance4User(final Context       _context,
                                              final String        _sInstanceId, 
                                              final StringBuilder _users)
                         throws EFapsException
    {

        List<Long> lAccessSets = new Vector<Long>();
        
        StringBuilder cmd = new StringBuilder();
        cmd.append("select ACCESSSETTO from T_YDSSMEMBERSHIP ")
           .append("where SHAREFROM = ").append(_sInstanceId).append(" ")
           .append("and USERABSTRACTFROM in (").append(_users).append(")");
        ConnectionResource cr = null;
        try
        {
            cr = _context.getConnectionResource();
            Statement stmt = null;
            try
            {
                stmt = cr.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(cmd.toString());

                while (rs.next())
                {
                    lAccessSets.add(rs.getLong(1));
                }
                rs.close();
            }
            finally
            {
                if (stmt != null)
                {
                    stmt.close();
                }
            }
            cr.commit();

            if (lAccessSets.contains(new Long(AccessSet.getAccessSet("Share Holder").getId())))
            {
                lAccessSets.add(AccessSet.getAccessSet("Folder Create/Delete").getId());
            }
            if (lAccessSets.contains(new Long(AccessSet.getAccessSet("Folder Create").getId()))
                || lAccessSets.contains(new Long(AccessSet.getAccessSet("Folder Create/Delete").getId())))
            {
                lAccessSets.add(AccessSet.getAccessSet("Document Create/Delete").getId());
            }
        }
        catch (SQLException e)
        {
            LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        } 
        finally
        {
            if ((cr != null) && cr.isOpened())
            {
                cr.abort();
            }
        }

        return lAccessSets;
    }
}
