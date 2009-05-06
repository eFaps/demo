package ydss.ui;

import org.efaps.admin.ui.Field;
import org.efaps.beans.FormBean;
import org.efaps.beans.form.FormFieldUpdateInterface;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The class is implemeneted as form field update for form
 * <code>YDSS_DocumentForm</code>. The class is used to create a new document
 * version while a new document is created.
 *
 * @author tjx
 * @version $Rev$
 */
public class DocumentCheckinDescription implements FormFieldUpdateInterface  {

  /**
   * The method is called for field <code>description</code> in the create event. The
   * method does:
   * <ul>
   * <li>create a new document version</li>
   * <li>check in the given file</li>
   * </ul>
   */
  public boolean update(final Context _context, final FormBean _form, final Field _field)
      throws EFapsException
  {
    return true;
  }
}