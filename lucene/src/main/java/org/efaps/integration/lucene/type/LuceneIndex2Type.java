/*
 * Copyright 2007 The eFaps Team
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
 * Author:          jmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.integration.lucene.type;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.integration.lucene.DocFactory;
import org.efaps.util.EFapsException;

public class LuceneIndex2Type {
    private static long ID;

    private static String OID;

    private Map<String, Class<?>> DOCINDEXER = new HashMap<String, Class<?>>();

    private LuceneAnalyzer LUCENEANALYZER;

    private DocFactory LUCENEDOCFACTORY;

    private static Type TYPE;

    public String getOID() {
	return OID;
    }

    public static void setOID(String _OID) {
	OID = _OID;
    }

    public LuceneIndex2Type() {
	// TODO Auto-generated constructor stub
    }

    public LuceneIndex2Type(String _OID) {
	setOID(_OID);
	setType(new Instance(getOID()).getType());
	setDocIndexer();
	setAnalyzer();
	setLuceneDocFactory();
    }

    private void setLuceneDocFactory() {
	LUCENEDOCFACTORY = (DocFactory) new DocFactory(getOID());

    }

    public DocFactory getLuceneDocFactory() {
	return LUCENEDOCFACTORY;
    }

    public static String createNew(String _IndexID, String _TypeID) {

	Insert insert;
	try {
	    insert = new Insert("Lucene_Index2Type");
	    insert.add("Index", _IndexID);
	    insert.add("Type", _TypeID);
	    insert.execute();
	    String IndexTypesID = insert.getId();
	    insert.close();
	    return IndexTypesID;
	} catch (EFapsException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;

    }

    public Class<?> getDocIndexer(String _DocType) {
	Class<?> cl = DOCINDEXER.get(_DocType);
	if (cl == null) {
	    try {
		cl = Class
			.forName("org.efaps.integration.lucene.indexer.NullIndexer");
	    } catch (ClassNotFoundException e) {

		e.printStackTrace();
	    }
	}
	return cl;
    }

    public long getID() {
	return ID;
    }

    public LuceneAnalyzer getLuceneAnalyzer() {
	return LUCENEANALYZER;
    }

    public long getTypeID() {
	return getType().getId();
    }

    private void setAnalyzer() {
	SearchQuery query = new SearchQuery();
	try {
	    query.setExpand(getOID(), "Lucene_Type2Analyzer\\IndexType");
	    query.addSelect("OID");
	    query.addSelect("Analyzer");
	    query.execute();
	    String AnalyzerID = "";
	    while (query.next()) {
		AnalyzerID = (String) query.get("Analyzer").toString();
	    }

	    Long LuceneAnalyzerID = Type.get("Lucene_Analyzer").getId();

	    String LuceneAnalyzerOID = new String(LuceneAnalyzerID.toString()
		    + "." + AnalyzerID);

	    LUCENEANALYZER = new LuceneAnalyzer(LuceneAnalyzerOID);
	    query.close();
	} catch (EFapsException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void setDocIndexer() {
	SearchQuery query = new SearchQuery();
	try {
	    query.setExpand(getOID(), "Lucene_Type2Indexer\\Index2Type");
	    query.addSelect("Indexer");
	    query.execute();

	    while (query.next()) {

		LuceneIndexer indexer = new LuceneIndexer(query.get("Indexer")
			.toString());

		DOCINDEXER.put(indexer.getFileTyp(), indexer.getIndexer());

	    }

	} catch (EFapsException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void setLuceneAnalyzer(LuceneAnalyzer _luceneanalyzer) {
	LUCENEANALYZER = _luceneanalyzer;
    }

    public void setType(Type _type) {
	TYPE = _type;
    }

    public static Type getType() {
	return TYPE;
    }

}
