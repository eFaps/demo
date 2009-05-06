/*
 * Copyright 2005 The ydss Team
 *
 */

package ydss.ui;

import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Class to render the Loced field for the YDSS_Document on create forms
 *
 * @author MBU
 */
public class DocumentLockedField implements UIInterface
{
    /**
     * @param _context The thread context.
     * @param _value   The current database value for the field
     * @param _field   The field definition
     */
    public String getViewHtml(Context _context, Object _value, Field _field)
        throws EFapsException
    {
System.out.println("DocumentLockedField _value = "+_value);
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
       return _value.toString();
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
           .append("\" type=\"hidden\" value=\"true\">");

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

        ret.append("<select name=\"")
           .append(_field.getName())
           .append("\" type=\"hidden\">\n")
           .append("<option>*</option>\n")
           .append("<option>true</option>\n")
           .append("<option>false</option>\n")
           .append("</select>");

        return ret.toString();
    }
}