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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessCheckInterface;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author mbu
 * @version $Id$
 * @todo description
 */
public class AccessCheckOnMembership implements AccessCheckInterface
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

        boolean bHasAccess = false;
        Context context = Context.getThreadContext();

        String sAccessType     = _accessType.getName();
        Instance insShare      = null;
        List<Instance> insMemberships = new Vector();
        List<Person>   personTargets = new Vector();
        
        if (context.getParameters().containsKey("parentOid"))
        {
            String sOid = context.getParameter("parentOid");
            String sType = (new Instance(sOid)).getType().getName();
            if ("YDSS_Membership".equals(sType))
            {
                insMemberships.add(new Instance(sOid));
            }
            else if ("YDSS_Share".equals(sType) || "YDSS_Folder".equals(sType) || "YDSS_Document".equals(sType))
            {
                insShare = new Instance(sOid);
            }
        }
        if (context.getParameters().containsKey("oid"))
        {
            String sOid = context.getParameter("oid");
            String sType = (new Instance(sOid)).getType().getName();
            if ("YDSS_Membership".equals(sType))
            {
                insMemberships.add(new Instance(sOid));
            }
            else if ("YDSS_Share".equals(sType) || "YDSS_Folder".equals(sType) || "YDSS_Document".equals(sType))
            {
                insShare = new Instance(sOid);
            }
        }
        if (context.getParameters().containsKey("selectedRow"))
        {
            List<String> lOids = Arrays.asList(context.getParameters().get("selectedRow"));
            if (lOids.size() > 0)
            {
                String sType = (new Instance(lOids.get(0))).getType().getName();
                for (String sOid : lOids)
                {
                    if ("Admin_User_Person".equals(sType))
                    {
                        personTargets.add(Person.get(sOid));
                    }
                    else if ("YDSS_Membership".equals(sType))
                    {
                        insMemberships.add(new Instance(sOid));
                    }
                }
            }
        }
        if (insShare == null && insMemberships.size() > 0)
        {
            insShare = getShareFromMembership(insMemberships.get(0));
        }
        Person personUser = context.getPerson();

        int iMembershipCount = 0;
        if (insMemberships.size() > 0)
        {
            if (insMemberships.size() > 1 && context.containsRequestAttribute("MembershipCount"))
            {
                iMembershipCount = ((Integer) context.getRequestAttribute("MembershipCount")).intValue();
                iMembershipCount++;
                context.setRequestAttribute("MembershipCount", new Integer(iMembershipCount));

            }
            else
            {
                context.setRequestAttribute("MembershipCount", new Integer(0));
            }
        }
        else
        {
            insMemberships.add(null);
        }

        if (("create".equals(sAccessType) || "delete".equals(sAccessType))
            && (isShareHolder(insMemberships.get(0), insShare, personUser) || isAdmin(personUser))
            && isShare(insShare))
        {
            bHasAccess = true;
        }
        else if (!("create".equals(sAccessType) || "delete".equals(sAccessType))
                 && isShareHolder(insMemberships.get(0), insShare, personUser) || isAdmin(personUser))
        {
            bHasAccess = true;
        }
        else if (("show".equals(sAccessType) || "read".equals(sAccessType))
                 && isMember(insMemberships.get(0), insShare, personUser))
        {
            bHasAccess = true;
        }
        else if ("delete".equals(sAccessType)
                 && isShare(insShare)
                 && isSelf(insMemberships.get(iMembershipCount), personUser))
        {
            bHasAccess = true;
        }

        // additional conditions:
        // 1. There has to be at last one Share Holder
        if (bHasAccess 
            && ("delete".equals(sAccessType) || "modify".equals(sAccessType)))
        {
            SearchQuery query = new SearchQuery();
            query.setObject(insMemberships.get(iMembershipCount));
            query.addSelect("AccessSetToLink.Name");
            query.executeWithoutAccessCheck();
            query.next();
            if ("Share Holder".equals(query.get("AccessSetToLink.Name").toString().trim()))
            {
                query = new SearchQuery();
                query.setQueryTypes("YDSS_Membership");
                query.addSelect("AccessSetToLink.Name");
                query.addWhereExprEqValue("ShareFromLink", insShare.getId());
                query.executeWithoutAccessCheck();
                int iCount = 0;
                while (query.next())
                {
                    if ("Share Holder".equals(query.get("AccessSetToLink.Name")
                                                   .toString()
                                                   .trim()))
                    {
                        iCount++;
                    }
                }
                if (iCount < 2)
                {
                    bHasAccess = false;
                }
            }
        }

        return bHasAccess;
    }

    public Instance getShareFromMembership(final Instance _insMembership) 
        throws EFapsException
    {
                    SearchQuery query = new SearchQuery();
                    query.setObject(_insMembership);
                    query.addSelect("ShareFromLink.OID");
                    query.executeWithoutAccessCheck();
                    query.next();
                    String sShareFromOId = query.get("ShareFromLink.OID")
                                                .toString()
                                                .trim();
                    
                    return new Instance(sShareFromOId);
    }

    protected boolean isAdmin(Person _personUser) throws EFapsException
    {
        boolean bResult = false;

        Set<Role> lUserRoles = _personUser.getRoles();

        for (Role role : lUserRoles)
        {
            bResult = bResult || (role.getId() == 2);
        }

        return bResult;
    }

    protected boolean isShareHolder(Instance _insMembership, Instance _insShare, Person _personUser)
        throws EFapsException
    {
        boolean bResult = false;

        SearchQuery query   = null;
        String sShareFromId = "";
        if (_insShare == null)
        {
            Instance insShare = getShareFromMembership(_insMembership);
            sShareFromId = String.valueOf(insShare.getId());
        }
        else
        {
            sShareFromId = String.valueOf(_insShare.getId());
        }

        query = new SearchQuery();
        query.setQueryTypes("YDSS_Membership");
        query.addSelect("AccessSetToLink.Name");
        query.addWhereExprEqValue("ShareFromLink", sShareFromId);
        query.addWhereExprEqValue("UserAbstractFromLink",
                                  String.valueOf(_personUser.getId()));
        query.executeWithoutAccessCheck();
        if (query.next() && "Share Holder".equals(query.get("AccessSetToLink.Name")))
        {
            bResult = true;
        }

        return bResult;
    }

    protected boolean isMember(Instance _insMembership, Instance _insShare, Person _personUser)
        throws EFapsException
    {
        boolean bResult = false;

        SearchQuery query   = null;
        String sShareFromId = "";
        if (_insShare == null)
        {
            query = new SearchQuery();
            query.setObject(_insMembership);
            query.addSelect("ShareFromLink");
            query.executeWithoutAccessCheck();
            query.next();
            sShareFromId = query.get("ShareFromLink").toString();
        }
        else
        {
            sShareFromId = String.valueOf(_insShare.getId());
        }

        query = new SearchQuery();
        query.setQueryTypes("YDSS_Membership");
        query.addSelect("ID");
        query.addWhereExprEqValue("ShareFromLink", sShareFromId);
        query.addWhereExprEqValue("UserAbstractFromLink",
                                  String.valueOf(_personUser.getId()));
        query.executeWithoutAccessCheck();
        if (query.next())
        {
            bResult = true;
        }

        return bResult;
    }

    protected boolean isSelf(Instance _insMembership, Person _pB)
        throws EFapsException
    {
        SearchQuery query = new SearchQuery();
        query.setObject(_insMembership);
        query.addSelect("UserAbstractFromLink");
        query.executeWithoutAccessCheck();
        query.next();
        long lPersonId = Long.valueOf(query.get("UserAbstractFromLink").toString());

        return (lPersonId == _pB.getId());
    }

    protected boolean isShare(Instance _insShare)
        throws EFapsException
    {
        return "YDSS_Share".equals(_insShare.getType().getName());
    }
}
