/*
 * Copyright 2005 The ydss Team
 *
 */

package ydss.ui;

import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.Field;
import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Class to render the Locker field for the YDSS_Document on create forms
 *
 * @author MBU
 */
public class DocumentLockerField implements UIInterface
{
    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getViewHtml(Context _context, Object _value, Field _field)
        throws EFapsException
    {
        String sRet = "";

        if (_value != null)
        {
            sRet = ((Person) _value).getName();
        }

        return sRet;
    }

    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getEditHtml(Context _context, Object _value, Field _field) 
        throws EFapsException
    {
        String sRet = "";

        if (_value != null)
        {
            sRet = ((Person) _value).getName();
        }

        return sRet;
    }

    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getCreateHtml(Context _context, Object _value, Field _field) 
        throws EFapsException
    {
        StringBuilder ret = new StringBuilder();

        ret.append("<input name=\"")
           .append(_field.getName())
           .append("\" type=\"hidden\" value=\"")
           .append(_context.getPerson().getId())
           .append("\">");

        return ret.toString();
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

        ret.append("<input name=\"")
           .append(_field.getName())
           .append("\" value=\"*\">");

        return ret.toString();
    }
}