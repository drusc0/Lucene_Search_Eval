package com.ilsz534;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

public class CompareAlgorithms {

	public static final String TOPIC_FILE = "/Users/drusc0/Documents/IUB/ILS-Z 534/Assignments/assgt2/topics.51-100";
	public static final String ROOT = "/Users/drusc0/Documents/IUB/ILS-Z 534/Assignments/assgt2/";
	public static final String PATH = "/Volumes/SEAGATE1TB/indexAssgt2";
	private SearchTRECTopics t2;
	
	public CompareAlgorithms() throws IOException {
		this.t2 = new SearchTRECTopics();
	}
	
	
	// Type is an enumeration of the type of query needed
	public enum Type {
		SHORT, LONG
	}

	/*
	 * parses topic.51-100 title and description are used query the corpus makes
	 * use of QueryStruct to store the query string and query id
	 */
	public void parseQueryFile() throws IOException {
		t2.parseQueryFile();
	}

	
	/*
	 * getQueryList
	 */
	public List<QueryStruct> getQueryList() {
		return this.t2.getListOfQueries();
	}
	
	
	/*
	 * getTask1
	 */
	public EasySearch getTask1() {
		return this.t2.getTask1();
	}
	
	/*
	 * performRank takes a querylist, searcher, file path to store results, and
	 * type of query saves the results to a file for comparison between
	 * similarity algorithms
	 */
	public static void performRank(List<QueryStruct> queryList,
			IndexSearcher searcher, String route, Type type)
			throws IOException, ParseException {

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		NumberFormat formatter = new DecimalFormat("#0.00000");
		String queryString = "";
		BufferedWriter bw = new BufferedWriter(new FileWriter(route));
		int tmp = 0;

		for (QueryStruct q : queryList) {
			// perform term parsing
			if (type.equals(Type.SHORT))
				queryString = q.getQueryTitle();
			else
				queryString = q.getQueryDesc();
			
			Query query = parser.parse(queryString);
			System.out.println("Searching for: " + query.toString("TEXT"));

			// get the top 1000 results from query
			TopDocs results = searcher.search(query, 1000);

			ScoreDoc[] hits = results.scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				tmp = i + 1;
				Document doc=searcher.doc(hits[i].doc);
				bw.write(q.getQueryID() + " Q0 " + doc.get("DOCNO") + " " + tmp
						+ " " + formatter.format(hits[i].score) + " run-1\n");
			}
		}

		bw.close();
	}// end performRank

	// MAIN
	public static void main(String[] args) throws IOException, ParseException {
		
		CompareAlgorithms ca = new CompareAlgorithms();

		// results files names
		String bm25Long = "BM25longQuery.txt";
		String bm25Short = "BM25shortQuery.txt";
		String vsmLong = "VectorSpacelongQuery.txt";
		String vsmShort = "VectorSpaceshortQuery.txt";
		String dirichletLong = "DirichletlongQuery.txt";
		String dirichletShort = "DirichletshortQuery.txt";
		String jelinekLong = "JenileklongQuery.txt";
		String jelinekShort = "JenilekshortQuery.txt";

		ca.parseQueryFile();

		// perform ranking with BM25, Vector Space, Dirichlet, and Jelinek
		ca.getTask1().getSearcher().setSimilarity(new BM25Similarity());
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + bm25Short, Type.SHORT);
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + bm25Long, Type.LONG);
		ca.getTask1().getSearcher().setSimilarity(new ClassicSimilarity());
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + vsmShort, Type.SHORT);
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + vsmLong, Type.LONG);
		ca.getTask1().getSearcher().setSimilarity(new LMDirichletSimilarity());
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + dirichletShort, Type.SHORT);
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + dirichletLong, Type.LONG);
		ca.getTask1().getSearcher().setSimilarity(new LMJelinekMercerSimilarity((float) .7));
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + jelinekShort, Type.SHORT);
		performRank(ca.getQueryList(), ca.getTask1().getSearcher(), ROOT + jelinekLong, Type.LONG);

	}// end MAIN
}
