package com.ilsz534;
//ranks results structure
public class RankResults {
	public String docID;
	public double score;

	public RankResults() {
		this.docID = "";
		this.score = 0.0;
	}

	public String getDocID() {
		return docID;
	}

	public void setDocID(String docID) {
		this.docID = docID;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}// end RankResults
