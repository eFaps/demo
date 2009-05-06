package org.efaps.integration.lucene;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.db.SearchQuery;
import org.efaps.integration.lucene.log.LuceneLog;
import org.efaps.integration.lucene.type.LuceneIndex;
import org.efaps.util.EFapsException;

/**
 * Class for executing the whole prozess of indexing, this class can be used to
 * execute the index from other programms
 * 
 * @author jmo
 * 
 */
public class Prozess extends AbstractTransaction implements ProzessInterface {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(Prozess.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    String usage = "java org.efaps.integration.lucene.Prozess <action>";

    if (args.length == 0) {
      LOG.error("main(String[]) - Usage: " + usage, null);
      System.exit(1);
    }
    if (args[0].equals("new")) {
      (new Prozess()).createNewIndex();
    } else if (args[0].equals("run")) {
      (new Prozess()).executeAll();
    } else if (args[0].equals("reset")) {
      (new Prozess()).resetAll();
    }
  }

  public void createNewIndex() {
    loadRunLevel();
    try {
      login("Administrator", "");
      startTransaction();

      CreateLuceneIndex
          .create("/Users/janmoxter/Documents/workspace/eFaps/lucene/index.xml");

      commitTransaction();
    } catch (EFapsException e) {

      LOG.error("createNewIndex()", e);
    } catch (Exception e) {

      LOG.error("createNewIndex()", e);
    }

  }

  private List getAllIndexOID() {
    List<String> oidlist = new ArrayList<String>();
    SearchQuery query = new SearchQuery();
    try {
      startTransaction();

      query.setQueryTypes("Lucene_Index");
      query.addSelect("OID");
      query.execute();

      while (query.next()) {
        oidlist.add(query.get("OID").toString());

      }

      abortTransaction();
      return oidlist;
    } catch (EFapsException e) {

      LOG.error("getAllIndexOID()", e);
    } catch (Exception e) {

      LOG.error("getAllIndexOID()", e);
    }
    return null;
  }

  public void executeAll() {

    loadRunLevel();

    try {
      login("Administrator", "");
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    for (Iterator iter = getAllIndexOID().iterator(); iter.hasNext();) {
      execute((String) iter.next());

    }
  }

  public void execute(String _OID) {
    LuceneIndex index = new LuceneIndex();
    LuceneLog log = new LuceneLog();
    try {
      startTransaction();

      index.setIndex(_OID);
      log.setIndexID(index.getID());
      log.initialise();

      commitTransaction();

      startTransaction();

      int inD = 0;

      index.initialise();

      SearchQuery query = new SearchQuery();
      query.setQueryTypes("TeamWork_SourceVersion");
      query.addSelect("OID");
      query.addSelect("Modified");
      query.execute();
      while (query.next()) {
        Date compareDate = (Date) query.get("Modified");
        if (log.getLastStart().before(compareDate)) {

          index.addObject(query.get("OID").toString());

          inD++;
        }
      }
      query.close();
      if (inD > 0) {
        index.optimize();
        log.end(index.getDeleted(), inD);
      } else {

        log.end(0, 0);
      }

    } catch (EFapsException e) {
      log.terminate();
      LOG.error("execute(String)", e);
    } catch (Exception e) {
      log.terminate();
      LOG.error("execute(String)", e);
    }
    index.close();

    try {
      commitTransaction();
    } catch (EFapsException e) {

      LOG.error("execute(String)", e);
    } catch (Exception e) {

      LOG.error("execute(String)", e);
    }

  }

  public void reset(String _OID) {
    LuceneIndex index = new LuceneIndex();
    LuceneLog log = new LuceneLog();

    try {
      startTransaction();

      index.setIndex(_OID);
      index.setNewIndexFiles(true);
      log.setIndexID(index.getID());
      log.initialise();

      commitTransaction();

      startTransaction();

      int inD = 0;

      index.initialise();

      SearchQuery query = new SearchQuery();
      query.setQueryTypes("TeamWork_SourceVersion");
      query.addSelect("OID");
      query.addSelect("Modified");
      query.execute();
      while (query.next()) {

        index.addObject(query.get("OID").toString());

        inD++;
      }

      query.close();
      if (inD > 0) {
        index.optimize();
        log.end("Reset", index.getDeleted(), inD);
      } else {

        log.end("Reset", 0, 0);
      }
    } catch (EFapsException e) {

      LOG.error("reset(String)", e);
    } catch (Exception e) {

      LOG.error("reset(String)", e);
    }

    index.close();
    try {
      commitTransaction();
    } catch (EFapsException e) {

      LOG.error("reset(String)", e);
    } catch (Exception e) {

      LOG.error("reset(String)", e);
    }
  }

  public void resetAll() {
    loadRunLevel();
    for (Iterator iter = getAllIndexOID().iterator(); iter.hasNext();) {
      reset((String) iter.next());

    }

  }
}
