/*******************************************************************************
* Admin_UI_Form:
* ~~~~~~~~~~~~~~
* TeamCenter_FolderForm
*
* Description:
* ~~~~~~~~~~~~
*
* History:
* ~~~~~~~~
* Revision: $Rev$
* Date:     $Date$
* By:       $Author$
*
* Author:
* ~~~~~~~
* TMO
*******************************************************************************/

with (FORM)  {
  with (addField("type"))  {
    addProperty("Columns",                      "20");
    addProperty("Creatable",                    "false");
    addProperty("Editable",                     "false");
    addProperty("Expression",                   "Type");
    addProperty("Searchable",                   "false");
    addProperty("ShowTypeIcon",                 "true");
  }
  with (addField("name"))  {
    addProperty("Columns",                      "40");
    addProperty("Creatable",                    "true");
    addProperty("Editable",                     "true");
    addProperty("Expression",                   "Name");
    addProperty("Required",                     "true");
    addProperty("Searchable",                   "true");
  }
  with (addField("createGroup"))  {
    addProperty("Creatable",                    "false");
    addProperty("GroupCount",                   "2");
  }
  with (addField("creator"))  {
    addProperty("AlternateOID",                 "Creator.OID");
    addProperty("Creatable",                    "false");
    addProperty("Editable",                     "false");
    addProperty("Expression",                   "Creator");
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Searchable",                   "true");
    addProperty("ShowTypeIcon",                 "true");
  }
  with (addField("created"))  {
    addProperty("Creatable",                    "false");
    addProperty("Editable",                     "false");
    addProperty("Expression",                   "Created");
    addProperty("Searchable",                   "true");
  }
  with (addField("modifyGroup"))  {
    addProperty("Creatable",                    "false");
    addProperty("GroupCount",                   "2");
  }
  with (addField("modifier"))  {
    addProperty("AlternateOID",                 "Modifier.OID");
    addProperty("Creatable",                    "false");
    addProperty("Editable",                     "false");
    addProperty("Expression",                   "Modifier");
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Searchable",                   "true");
    addProperty("ShowTypeIcon",                 "true");
  }
  with (addField("modified"))  {
    addProperty("Creatable",                    "false");
    addProperty("Editable",                     "false");
    addProperty("Expression",                   "Modified");
    addProperty("Searchable",                   "true");
  }
}
