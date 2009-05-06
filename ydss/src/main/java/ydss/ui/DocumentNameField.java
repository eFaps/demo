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
 * Class to render the Name field for the YDSS_Document on different form types
 *
 * @author MBU
 */
public class DocumentNameField implements UIInterface
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

        String sDummyName = "dummy"+String.valueOf(Math.round(Math.random()*100000));

        ret.append("<input name=\"")
           .append(_field.getName())
           .append("\" type=\"hidden\" value=\"")
           .append(sDummyName)
           .append("\"> Document name will be set to the file's name.");

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
           .append("\" type=\"text\" ")
           .append("size=\"")
           .append(_field.getCols())
           .append("\" value=\"*\">");

        return ret.toString();
    }
}