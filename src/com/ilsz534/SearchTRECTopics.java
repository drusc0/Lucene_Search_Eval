package com.ilsz534;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.ParseException;

public class SearchTRECTopics {

	private static final String TOPIC_FILE = "/Users/drusc0/Documents/IUB/ILS-Z 534/Assignments/assgt2/topics.51-100";
	private static final String ROOT_PATH = "/Users/drusc0/Documents/IUB/ILS-Z 534/Assignments/assgt2/";
	private List<QueryStruct> listOfQueries;
	private EasySearch t1;
	private File topicFile;

	public SearchTRECTopics() throws IOException {
		this.t1 = new EasySearch();
		this.topicFile = new File(TOPIC_FILE);
		this.listOfQueries = new LinkedList<QueryStruct>();
	}

	/*
	 * parseQueryFile parses the topic query file into the the parts needed
	 * QueryID, QueryTitle, and QueryDescription
	 */
	public void parseQueryFile() throws IOException {

		BufferedReader bf = new BufferedReader(new FileReader(this.topicFile));
		StringBuilder builder = new StringBuilder();
		Pattern rexTitle = Pattern.compile("<title>\\s+Topic:(.*?)<desc>",
				Pattern.DOTALL);
		Pattern rexDesc = Pattern.compile("<desc>\\s+Description:(.*?)<smry>",
				Pattern.DOTALL);
		Matcher matcherTitle, matcherDescription;
		String line, query;

		// iterate thru lines of topics.51-100
		while ((line = bf.readLine()) != null) {
			if (line.contains("<top>")) {
				QueryStruct queryStruct = new QueryStruct();
				// read info in between top tags
				while ((line = bf.readLine()) != null
						&& !line.contains("</top>")) {

					if (line.contains("<num>")) {
						queryStruct.setID(line.replaceAll("\\D+", ""));
					}

					builder.append(line);
					builder.append(System.getProperty("line.separator"));
				}

				// extract each query block
				query = builder.toString();
				builder.setLength(0);

				// process query block
				matcherTitle = rexTitle.matcher(query);
				matcherDescription = rexDesc.matcher(query);

				if (matcherTitle.find()) {
					queryStruct.setTitle(matcherTitle.group(1).trim()
							.replaceAll("\\s+", " ").replaceAll("/", "//"));
				}
				if (matcherDescription.find()) {
					queryStruct.setDesc(matcherDescription.group(1).trim()
							.replaceAll("\\s+", " ").replaceAll("/", "//"));
				}

				this.listOfQueries.add(queryStruct);
			}// end of top
		} // end while EOF
		bf.close();
	} // end parseQueryFile

	/*
	 * parseQuery performs the same action from task1, in fact, we call task1
	 * parseQuery
	 */
	public void parseQuery(String queryString) throws ParseException,
			IOException {
		this.t1.parseQuery(queryString);
	}// parseQuery

	
	/*
	 * rankDocuments() TF*IDF implementation using individual term ranking added
	 * up score the doc
	 */
	public void rankDocuments(QueryStruct qStruct) throws IOException {

		List<LeafReaderContext> leafContexts = this.t1.getReader().getContext()
				.reader().leaves();

		// different segments in index
		//for (int i = 0; i < leafContexts.size(); i++) {
			LeafReaderContext leafContext = leafContexts.get(0);
			int docBase = leafContext.docBase;
			
			// individual documents
			for (int docID = 0; docID < leafContext.reader().maxDoc(); docID++) {

				double score = this.t1.rankDoc(leafContext, docID);
				RankResults rr = new RankResults();
				rr.setDocID(this.t1.getSearcher().doc(docID+docBase).get("DOCNO"));
				rr.setScore(score);
				qStruct.add(rr);

			} // end of individual documents rank
		//} // end of individual segments
	}// end rankDocuments
	
	

	/*
	 * getListOfQueries
	 */
	public List<QueryStruct> getListOfQueries() {
		return this.listOfQueries;
	}
	
	
	/*
	 * getTask1
	 */
	public EasySearch getTask1() {
		return this.t1;
	}
	
	
	/*
	 * emptyListOfTerms
	 */
	public void emptyListOfTerms() {
		this.t1.emptyListOfTerms();
	}
	

	// MAIN
	public static void main(String[] args) throws IOException, ParseException {
		// task2 instantiates task1 and its methods
		SearchTRECTopics t2 = new SearchTRECTopics();
		
		NumberFormat formatter = new DecimalFormat("#0.00000");
		BufferedWriter bwLong = new BufferedWriter(
				new FileWriter(ROOT_PATH+"algolongQuery.txt"));
		//BufferedWriter bwShort = new BufferedWriter(
		//		new FileWriter(ROOT_PATH+"algoshortQuery.txt"));

		// parse the query files
		t2.parseQueryFile();

		// Print title (short query) and description (long query)
		for (QueryStruct queryStruct : t2.getListOfQueries()) {
			System.out.println(queryStruct.getQueryID());
			System.out.println(queryStruct.getQueryTitle());
			System.out.println(queryStruct.getQueryDesc());
		}

		// rank documents for each query for short queries (title)
		/*for (QueryStruct queryStruct : t2.getListOfQueries()) {
			t2.parseQuery(queryStruct.getQueryTitle());
			t2.rankDocuments(queryStruct);

			// write to file
			for (int i = 0; i < queryStruct.getList().size(); i++) {
				RankResults rr = queryStruct.getList().get(i);
				bwShort.write(queryStruct.getQueryID() + " Q0 "
						+ rr.getDocID() + " " + (i+1) + " "
						+ formatter.format(rr.getScore())
						+ " run-1\n");
			}
			
			t2.emptyListOfTerms();
		}
		bwShort.close();*/

		// rank documents for each query for long queries (description)
		for (QueryStruct queryStruct : t2.getListOfQueries()) {
			t2.parseQuery(queryStruct.getQueryDesc());
			t2.rankDocuments(queryStruct);
			
			// write to file
			for (int i = 0; i < queryStruct.getList().size(); i++) {
				RankResults rr = queryStruct.getList().get(i);
				bwLong.write(queryStruct.getQueryID() + " Q0 "
						+ rr.getDocID() + " " + (i+1) + " "
						+ formatter.format(rr.getScore())
						+ " run-1\n");
			}
			
			t2.emptyListOfTerms();
		}
		bwLong.close();
	} // end MAIN

}