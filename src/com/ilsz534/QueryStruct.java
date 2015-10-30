package com.ilsz534;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

//structure containing important fields from query
public class QueryStruct{
	public static final int MAX= 1000;
	public String queryID;
	public String queryTitle;
	public String queryDesc;
	List<RankResults> rankingList;

	public QueryStruct() {
		this.queryID = "";
		this.queryTitle = "";
		this.queryDesc = "";
		this.rankingList = new LinkedList<RankResults>();
	}

	public void setID(String ID) {
		this.queryID = ID;
	}

	public void setTitle(String title) {
		this.queryTitle = title;
	}

	public void setDesc(String desc) {
		this.queryDesc = desc;
	}

	public String getQueryID() {
		return this.queryID;
	}

	public String getQueryTitle() {
		return this.queryTitle;
	}

	public String getQueryDesc() {
		return this.queryDesc;
	}

	public List<RankResults> getList() {
		return this.rankingList;
	}

	public void add(RankResults rr) {
		this.rankingList.add(rr);
		this.sort();
		
		if (this.rankingList.size() == (MAX+1)) {
			this.rankingList.remove(MAX);
		}
	}

	public void sort() {
		Collections.sort(this.rankingList, new Comparator<RankResults>() {

			@Override
			public int compare(RankResults o1, RankResults o2) {
				if(o2.getScore() > o1.getScore()) return 1;
				else return -1;
			}
			
		});
	}
	
	public void emptyListOfRanks() {
		this.rankingList.clear();
	}
}
