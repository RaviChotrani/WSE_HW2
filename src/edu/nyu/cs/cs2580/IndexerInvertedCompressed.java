package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.nyu.cs.cs2580.SearchEngine.Options;

public class IndexerInvertedCompressed extends Indexer {

  // Store inverted index compressed with an object of doc+wordcount+position list
  Map<String, Map<Integer, Vector<Integer>>> _compressedIndex = 
		  new LinkedHashMap<String, Map<Integer,Vector<Integer>>>();
	
  public static void main(String[] args) {
	new IndexerInvertedCompressed();
  }
  
  public IndexerInvertedCompressed() {

		try {
			Options options = new Options("conf/engine.conf");
			IndexerInvertedCompressed iido = new IndexerInvertedCompressed(options);
			long start = System.currentTimeMillis();
			iido.constructIndex();
			//iido.loadIndex(); 
			long end = System.currentTimeMillis();
			System.out.println("time = " + (end - start));

			 //testNextDoc(iido);
		} catch (IOException e) { // TODO Auto-generated
			e.printStackTrace();
		}

		// testMerge();
  }
  
  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
	  String corpusDirPath = _options._corpusPrefix;
	  System.out.println("Constructing index from: " + corpusDirPath);
	  File corpusDir = new File(corpusDirPath);
	  for (File corpusFile : corpusDir.listFiles()) {
		  Document doc = Jsoup.parse(corpusFile, "UTF-8");
		  String contents = doc.text();
		  String title = doc.title().trim();

		  if (title.length() == 0) {
			  title = corpusFile.getName();
		  }

		  processDocument(contents, title, _numDocs);

		  if ((_numDocs + 1) % _maxFiles == 0) {
			  // write index to intermediate file
			  writeIndexToFile();
			  _intermediateIndexFiles++;

			  // flush the in memory index
			  _invertedIndex = new LinkedHashMap<String, Map<Integer, Integer>>();
		  }

		  _numDocs++;
	  }

	  // write last batch of info
	  writeIndexToFile();		
	}

  private void processDocument(String content, String title, int docId) {

	  if (content == null || title == null || docId < 0) {
		  return;
	  }

	  System.out.println("Processing : " + docId);
	  Vector<String> terms = getStemmed(content);
	  for (String t : terms) {
		  t = t.trim();
		  if (t.length() > 0) {
			  if (!_compressedIndex.containsKey(t)) {
				  _compressedIndex.put(t, new LinkedHashMap<Integer, Vector<Integer>>());
			  }

			  
			  if (_compressedIndex.get(t).containsKey(docId)) {
				  _invertedIndex.get(t).put(docId,
						  _invertedIndex.get(t).get(docId) + 1);
			  } else {
				  _invertedIndex.get(t).put(docId, 1);
			  }

			  ++_totalTermFrequency;
		  }
	  }

	  // DocumentIndexed docIndexed = new DocumentIndexed(docId);
	  // docIndexed.setTitle(title);
	  // _documents.add(docIndexed);
	  // _docTitles.add(title);
	  Utilities.writeToFile(_titleFile, title + "\n", true);
  }
  
  private Vector<String> getStemmed(String contents) {

	  if (contents == null) {
		  return null;
	  }

	  Vector<String> stemmedContents = new Vector<String>();

	  Scanner s = new Scanner(contents.toLowerCase());
	  s.useDelimiter("[^a-zA-Z0-9]");
	  while (s.hasNext()) {
		  String term = s.next();

		  Stemmer stemmer = new Stemmer();
		  stemmer.add(term.toCharArray(), term.length());
		  stemmer.stem(); // code of stemmer is modified to compute just
		  // step1()

		  stemmedContents.add(stemmer.toString());
	  }
	  s.close();

	  return stemmedContents;
  }
	
  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
  }

  @Override
  public Document getDoc(int docid) {
    SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
    return null;
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}
   */
  @Override
  public Document nextDoc(Query query, int docid) {
    return null;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
    return 0;
  }

  @Override
  public int corpusTermFrequency(String term) {
    return 0;
  }

  /**
   * @CS2580: Implement this for bonus points.
   */
  @Override
  public int documentTermFrequency(String term, String url) {
    return 0;
  }
  
  
}
