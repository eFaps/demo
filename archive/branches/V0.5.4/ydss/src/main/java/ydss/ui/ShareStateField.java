/*
 * Copyright 2005 The ydss Team
 *
 */

package ydss.ui;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.Field;
import org.efaps.beans.FormBean;
import org.efaps.beans.form.FormFieldUpdateInterface;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * Class to render the State field for the YDSS_Share on edit forms
 *
 * @author MBU
 */
public class ShareStateField implements UIInterface, FormFieldUpdateInterface
{
    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getViewHtml(Context _context, Object _value, Field _field)
        throws EFapsException
    {
        return _value.toString();
    }

    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getEditHtml(Context _context, Object _value, Field _field) 
        throws EFapsException
    {
        String sResult = _value.toString();
        String sOId = _context.getParameter("oid");
        Instance instance = new Instance(sOId);

        boolean isShare = "YDSS_Share".equals(instance.getType().getName());
        boolean isShareHolder = false;

        if (isShare)
        {
            long lShareId = instance.getId();

            SearchQuery query = new SearchQuery();
            query.setQueryTypes("YDSS_Membership");
            query.addSelect("AccessSetToLink.OID");
            query.addWhereExprEqValue("ShareFromLink", lShareId);
            query.addWhereExprEqValue("UserAbstractFromLink",
                                     _context.getPerson().getId());
            query.executeWithoutAccessCheck();
           
            if(query.next() && query.get("AccessSetToLink.OID") != null)
            {
                String sASOid = query.get("AccessSetToLink.OID").toString();
                query = new SearchQuery();
                query.setObject(sASOid);
                query.addSelect("Name");
                query.executeWithoutAccessCheck();
                query.next();
                isShareHolder = "Share Holder".equals(query.get("Name").toString());
            }
        }

        if (isShare && isShareHolder)
        {
            StringBuilder sbResult = new StringBuilder();

            Map<String, String> mPromotes = new HashMap<String, String>();
            mPromotes.put("Admin","Active");
            mPromotes.put("Active","Archived");
            mPromotes.put("Archived","Admin");
            
            String sCurrent = _value.toString().trim();

            sbResult.append("<select name=\"")
                    .append(_field.getName())
                    .append("\">\n")
                    .append("<option selected>")
                    .append(sCurrent)
                    .append("</option>\n")
                    .append("<option>")
                    .append(mPromotes.get(sCurrent))
                    .append("</option>\n")
                    .append("</select>");
            sResult = sbResult.toString();
        }
        else
        {
            StringBuilder sbResult = new StringBuilder();

            sbResult.append(sResult)
                    .append("<input type=\"hidden\" name=\"")
                    .append(_field.getName())
                    .append("\" value=\"")
                    .append(sResult)
                    .append("\">");
            sResult = sbResult.toString();
        }

        return sResult;
    }

    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getCreateHtml(Context _context, Object _value, Field _field) 
        throws EFapsException
    {
        return "Admin";
    }

    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getSearchHtml(Context _context, Object _value, Field _field) 
        throws EFapsException
    {
        StringBuilder ret = new StringBuilder();

        ret.append("<select name=\"")
           .append(_field.getName())
           .append("\">\n")
           .append("<option>*</option>\n")
           .append("<option>Admin</option>\n")
           .append("<option>Active</option>\n")
           .append("<option>Archived</option>\n")
           .append("</select>");

        return ret.toString();
    }

   /**
    * The method is called for field <code>file</code> in the create event. The
    * method does:
    * <ul>
    * <li>create a new document version</li>
    * <li>check in the given file</li>
    * </ul>
    */
    public boolean update(final Context _context, final FormBean _form, final Field _field) throws EFapsException
    {
        // get current state
        SearchQuery query = new SearchQuery();
        query.setObject(_form.getInstance());
        query.addSelect("State");
        query.execute();
        query.next();

        String sCurrent = query.get("State").toString().trim();
        String sTarget  = _form.getParameter("state");

        if (!sCurrent.equals(sTarget))
        {
            try
            {
                Update update = new Update(_form.getInstance());
                update.add("State", sTarget);
                update.executeWithoutAccessCheck();
            }
            catch (EFapsException efex)
            {
                throw efex;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                throw new EFapsException(this.getClass(), "", ex);
            }
        }
        
        return true;
    }
}