package parsing;

import java.util.Scanner;

import utils.FileIO;

public class ParallelCorpusParser {
	private String pathList;

	public ParallelCorpusParser(String pathList) {
		this.pathList = pathList;
	}
	
	public void generateParallelCorpus(final String outPath, final boolean recursive, final boolean testing, final boolean buildClauseTree) {
		String content = FileIO.readStringFromFile(pathList);
		Scanner sc = new Scanner(content);
		while (sc.hasNextLine()) {
			final String line = sc.nextLine();
			int index = line.indexOf(' ');
			final String name = line.substring(0, index), path = line.substring(index + 1);
			ProjectCorpusParser pcp = new ProjectCorpusParser(path, testing, buildClauseTree);
			pcp.generateParallelCorpus(outPath + "/" + name, recursive);
			
	        break; // read only the first line					
		}
		sc.close();
	}
	
	public static void main(String[] args) {
		ParallelCorpusParser pcp = new ParallelCorpusParser("list.txt");
		pcp.generateParallelCorpus("T:/spectrans", true, true, true);
	}
}
