package com.ilsz534;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class EasySearch {

	public static final String PATH = "/Volumes/SEAGATE1TB/indexAssgt2";
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private Set<Term> listOfTerms;

	public EasySearch() throws IOException {
		this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(PATH)));
		this.searcher = new IndexSearcher(reader);
		this.analyzer = new StandardAnalyzer();
		this.listOfTerms = new LinkedHashSet<Term>();
	}

	/*
	 * parseQuery tokenizes the query string into tokens and stores them as a
	 * set
	 */
	public void parseQuery(String queryString) throws ParseException,
			IOException {
		QueryParser parser = new QueryParser("TEXT", this.analyzer);
		Query query = parser.parse(queryString);
		this.searcher.createNormalizedWeight(query, false).extractTerms(
				listOfTerms);
	}// end parseQuery

	/*
	 * rankDocuments() TF*IDF implementation using individual term ranking added
	 * up score the doc
	 */
	public void rankDocuments() throws IOException {

		List<LeafReaderContext> leafContexts = this.reader.getContext()
				.reader().leaves();

		// different segments in index
		for (int i = 0; i < leafContexts.size(); i++) {
			LeafReaderContext leafContext = leafContexts.get(i);

			// individual documents
			for (int docID = 0; docID < leafContext.reader().maxDoc(); docID++) {

				rankDoc(leafContext, docID);

			} // end of individual documents rank
		} // end of individual segments

	}// end rankDocuments

	/*
	 * rankDoc performs term ranking and sums it up to provide document score
	 */
	public double rankDoc(LeafReaderContext leaf, int docID) throws IOException {

		int N = this.reader.maxDoc();
		double docScore = 0.0;
		DefaultSimilarity dSimi = new DefaultSimilarity();

		for (Term term : this.listOfTerms) {

			PostingsEnum de = MultiFields.getTermDocsEnum(leaf.reader(),
					"TEXT", new BytesRef(term.text()));
			if (de != null) {
				while(de.docID() < docID) {
					de.nextDoc();
				}
				//de.advance(docID);
				if (de.docID() == docID && de.freq() > 0) {

					int df = this.reader.docFreq(new Term("TEXT", term.text()));
					double IDF = Math.log(1 + (N / df));
					double normDocLength = dSimi.decodeNormValue(leaf.reader()
							.getNormValues("TEXT").get(docID));
					int freq = de.freq();
					double length = 1 / (normDocLength * normDocLength);
					double TF = (double) freq / length;
					double res = TF * IDF;
					//System.out.println(term + ": " + res);
					docScore += res;
				}
			}
		}
		System.out.println(this.searcher.doc(docID + leaf.docBase).get("DOCNO")
				+ ": " + docScore);
		return docScore;
	} // end rankDoc

	/*
	 * emptyListOfTerms
	 */
	public void emptyListOfTerms() {
		this.listOfTerms.clear();
	}

	/*
	 * getReader returns index reader
	 */
	public IndexReader getReader() {
		return this.reader;
	}

	/*
	 * getSearcher returns index searcher
	 */
	public IndexSearcher getSearcher() {
		return this.searcher;
	}

	// MAIN
	public static void main(String[] args) throws ParseException, IOException {
		// allow for parameter to be passed as a query, else use default
		// "New York"
		String queryString = "";
		EasySearch t1 = new EasySearch();

		if (args.length > 0) {
			// set the query to the phrase passed
			for (int i = 0; i < args.length; i++) {
				queryString += args[i];
				queryString += " ";
			}
			queryString = queryString.substring(0, queryString.length() - 1);
		} else if (args.length == 0) {
			queryString = "New York";
		}

		// parse query terms
		t1.parseQuery(queryString);
		// rank documents
		t1.rankDocuments();

	}// end MAIN

}// end task1
