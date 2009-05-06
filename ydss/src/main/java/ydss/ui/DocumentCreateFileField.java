package ydss.ui;

import org.apache.commons.fileupload.FileItem;
import org.efaps.admin.ui.Field;
import org.efaps.beans.FormBean;
import org.efaps.beans.form.FormFieldUpdateInterface;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * The class is implemeneted as form field update for form
 * <code>YDSS_DocumentForm</code>. The class is used to create a new document
 * version while a new document is created.
 *
 * @author tjx
 * @version $Rev$
 */
public class DocumentCreateFileField implements FormFieldUpdateInterface  {

  /**
   * The method is called for field <code>file</code> in the create event. The
   * method does:
   * <ul>
   * <li>create a new document version</li>
   * <li>check in the given file</li>
   * </ul>
   */
  public boolean update(final Context _context, final FormBean _form, final Field _field) throws EFapsException  {

    boolean bResult = false;
    try
    {
        SearchQuery query = null;

        // get metadata from document
        query = new SearchQuery();
        query.setObject(_form.getInstance());
        query.addSelect("ID");
        query.addSelect("Name");
        query.addSelect("FileVersion");
        query.execute();
        query.next();

        int   iFileVersion  = 0;
        String sDocumentName = "";
        try
        {
            iFileVersion = Integer.parseInt(query.get("FileVersion").toString());
            sDocumentName = query.get("Name").toString();
        }
        catch (Exception ex)
        {
            query = new SearchQuery();
            query.setObject(_form.getInstance());
            query.addSelect("ID");
            query.execute();
            query.next();
        }

        FileItem fileItem = _form.getFileParameter("file");
        if (fileItem != null)
        {
            String sDescription = "";
            if (_form.getParameter("description") != null
                && _form.getParameter("description").length() > 0)
            {
                sDescription = _form.getParameter("description");
            }

            // check name consistancy
            String sNewName = fileItem.getName();
            sNewName = sNewName.substring(sNewName.lastIndexOf("/")+1);
            sNewName = sNewName.substring(sNewName.lastIndexOf("\\")+1);
            if (iFileVersion == 0 || sDocumentName.equals(sNewName))
            {
                //generate new document version
                Insert insert = new Insert("YDSS_DocumentVersion");
                insert.add("Description", sDescription);
                insert.add("Document", String.valueOf(_form.getInstance().getId()));
                insert.add("Version", String.valueOf(++iFileVersion));
                insert.execute();

                //get id of generated document version
                long lId = 0;
                query = new SearchQuery();
                query.setQueryTypes("YDSS_DocumentVersion");
                query.addSelect("ID");
                query.addWhereExprEqValue("Document", _form.getInstance().getId());
                query.addWhereExprEqValue("Version", iFileVersion);
                query.execute();
                if (query.next())
                {
                    lId = ((Long) query.get("ID")).longValue();
                }
                else
                {
                    throw new Exception("Insert YDSS_DocumentVersion failed!");
                }

                // checkin file for YDSS_DocumentVersion and set meta data for
                // filename and filesize
                Instance instance = new Instance("YDSS_DocumentVersion", String.valueOf(lId));
                Checkin checkin = new Checkin(instance);
                checkin.execute(fileItem.getName(), fileItem.getInputStream(), (int)fileItem.getSize());

                // unlock document connected to the document version
                // (set name on document creation)
                Update update = new Update(_form.getInstance().getOid());
                update.add("Locked", "false");
                update.add("Locker", "0");
                if (iFileVersion == 1)
                {
                    String fileName = fileItem.getName();

                    // remove the path from the filename
                    int lastSeperatorPosX   = fileName.lastIndexOf("/");
                    int lastSeperatorPosWin = fileName.lastIndexOf("\\");
                    int lastSeperatorPosMac = fileName.lastIndexOf(":");

                    int lastSeperatorPos = lastSeperatorPosX;
                    if (lastSeperatorPos < lastSeperatorPosWin)
                    {
                        lastSeperatorPos = lastSeperatorPosWin;
                    }
                    if (lastSeperatorPos < lastSeperatorPosMac)
                    {
                        lastSeperatorPos = lastSeperatorPosMac;
                    }

                    if (lastSeperatorPos > -1 && lastSeperatorPos < fileName.length()-1)
                    {
                        fileName = fileName.substring(lastSeperatorPos+1);
                    }

                    update.add("Name", fileName);
                }
                update.execute();
            }
            else
            {
                String[] sPatternRelpacements = new String[2];
                sPatternRelpacements[0] = sDocumentName;
                sPatternRelpacements[1] = sNewName;
                throw new EFapsException(this.getClass(),
                                         "FileNameNotMatchesDocumentName",
                                         sPatternRelpacements);
            }
        }
        else
        {
            // update description on document
//            Update update = new Update(_form.getInstance().getOid());
//            update.add("Description", _form.getParameter("description"));
//            update.execute();
        }

        bResult = true;
    }
    catch (EFapsException e)
    {
        e.printStackTrace();
        throw e;
    }
    catch (Exception e)
    {
        e.printStackTrace();
        throw new EFapsException(getClass(), "updateException", e);
    }

    return bResult;
  }

}