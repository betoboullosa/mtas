package mtas.solr.search;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import mtas.parser.cql.MtasCQLParser;
import mtas.solr.handler.component.MtasSolrSearchComponent;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

/**
 * The Class MtasCQLQParser.
 */
public class MtasCQLQParser extends QParser {

  /** The mtas cql qparser field. */
  public static String MTAS_CQL_QPARSER_FIELD = "field";

  /** The mtas cql qparser query. */
  public static String MTAS_CQL_QPARSER_QUERY = "query";

  /** The mtas cql qparser default prefix. */
  public static String MTAS_CQL_QPARSER_DEFAULT_PREFIX = "defaultPrefix";

  /** The mtas cql qparser cql toberemoved. */
  public static String MTAS_CQL_QPARSER_CQL_TOBEREMOVED = "cql";

  /** The field. */
  String field = null;

  /** The query. */
  String query = null;

  /** The default prefix. */
  String defaultPrefix = null;

  /** The variables. */
  HashMap<String, String[]> variables = null;

  /** The msc. */
  MtasSolrSearchComponent msc = null;

  /**
   * Instantiates a new mtas cqlq parser.
   *
   * @param qstr the qstr
   * @param localParams the local params
   * @param params the params
   * @param req the req
   */
  public MtasCQLQParser(String qstr, SolrParams localParams, SolrParams params,
      SolrQueryRequest req) {
    super(qstr, localParams, params, req);

    SearchComponent sc = req.getCore().getSearchComponent("mtas");
    if ((sc != null) && (sc instanceof MtasSolrSearchComponent)) {
      msc = (MtasSolrSearchComponent) sc;
    }
    if ((localParams.getParams(MTAS_CQL_QPARSER_FIELD) != null)
        && (localParams.getParams(MTAS_CQL_QPARSER_FIELD).length == 1)) {
      field = localParams.getParams(MTAS_CQL_QPARSER_FIELD)[0];
    }
    if ((localParams.getParams(MTAS_CQL_QPARSER_QUERY) != null)
        && (localParams.getParams(MTAS_CQL_QPARSER_QUERY).length == 1)) {
      query = localParams.getParams(MTAS_CQL_QPARSER_QUERY)[0];
    } else if ((localParams.getParams(MTAS_CQL_QPARSER_CQL_TOBEREMOVED) != null)
        && (localParams
            .getParams(MTAS_CQL_QPARSER_CQL_TOBEREMOVED).length == 1)) {
      query = localParams.getParams(MTAS_CQL_QPARSER_CQL_TOBEREMOVED)[0];
    }
    if ((localParams.getParams(MTAS_CQL_QPARSER_DEFAULT_PREFIX) != null)
        && (localParams
            .getParams(MTAS_CQL_QPARSER_DEFAULT_PREFIX).length == 1)) {
      defaultPrefix = localParams.getParams(MTAS_CQL_QPARSER_DEFAULT_PREFIX)[0];
    }
    variables = new HashMap<String, String[]>();
    Iterator<String> it = localParams.getParameterNamesIterator();
    while (it.hasNext()) {
      String item = it.next();      
      if (item.startsWith("variable_")) {       
        if(localParams.getParams(item).length==0 || (localParams.getParams(item).length==1 && localParams.getParams(item)[0].equals(""))) {
          variables.put(item.substring(9),new String[0]);
        } else {
          ArrayList<String> list = new ArrayList<String>();
          for(int i=0; i<localParams.getParams(item).length; i++) {
            String[] subList = localParams.getParams(item)[i].split("(?<!\\\\),");
            for(int j=0; j<subList.length; j++) {
              list.add(subList[j].replace("\\,", ",").replace("\\\\", "\\"));
            }
          }
          variables.put(item.substring(9), list.toArray(new String[list.size()]));
        }
      }
    }    
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.solr.search.QParser#parse()
   */
  @Override
  public Query parse() throws SyntaxError {
    if (field == null) {
      throw new SyntaxError("no " + MTAS_CQL_QPARSER_FIELD);
    } else if (query == null) {
      throw new SyntaxError("no " + MTAS_CQL_QPARSER_QUERY);
    } else {
      Reader reader = new BufferedReader(new StringReader(query));
      MtasCQLParser p = new MtasCQLParser(reader);
      SpanQuery q = null;
      try {
        q = p.parse(field, null, variables);
      } catch (mtas.parser.cql.TokenMgrError e) {
        throw new SyntaxError(e.getMessage());
      } catch (mtas.parser.cql.ParseException e) {
        throw new SyntaxError(e.getMessage());
      }
      return q;
    }
  }

}