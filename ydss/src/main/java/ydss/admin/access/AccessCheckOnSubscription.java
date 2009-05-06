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
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessCheckInterface;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author mbu
 * @version $Id$
 * @todo description
 */
public class AccessCheckOnSubscription implements AccessCheckInterface
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
                                                      throws EFapsException  {
        boolean bHasAccess = false;
        Context context = Context.getThreadContext();

        // create
        if ("create".equals(_accessType.getName()) || "show".equals(_accessType.getName()) || "read".equals(_accessType.getName()))
        {
            bHasAccess = true;
        }

        // delete
        if (!bHasAccess && "delete".equals(_accessType.getName()))
        {
            bHasAccess = isSubscriber(context, String.valueOf(_instance.getId()));
        }

        return bHasAccess;
    }

    protected boolean isSubscriber( final Context _context,
                                    final String  _sInstanceId)
                         throws EFapsException
    {
        boolean bResult = false;

        StringBuilder cmd = new StringBuilder();
        cmd.append("select USERABSTRACTFROM from T_YDSSSUBSCRIPTION ")
           .append("where ID = ").append(_sInstanceId);

        List<Long> lUsers = new Vector<Long>();
        ConnectionResource cr = null;
        Statement stmt = null;
        try
        {
            cr = _context.getConnectionResource();
            try
            {
                stmt = cr.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(cmd.toString());

                while (rs.next())
                {
                    lUsers.add(rs.getLong(1));
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

        if (lUsers.size() > 0 && ((Long) lUsers.get(0)).longValue() == _context.getPerson().getId())
        {
            bResult = true;
        }

        return bResult;
    }
}
