package eval;

import java.util.HashSet;

import utils.FileIO;

public class PriorWorkEvaluation {

	public static void main(String[] args) {
		String fop_location = "Replication-package\\Replication-package\\";
		String[] arrFilter = FileIO.readStringFromFile(
				fop_location + "map.csv").split("\n");
		String[] arrContent = FileIO.readStringFromFile(
				fop_location + "ex2.csv").split("\n");
		String strInconsistent = "Inconsistent";
		String strConsistent = "Consistent";
		HashSet<Integer> setFilter=new HashSet<Integer>();
		for (int i = 1; i < arrFilter.length; i++) {
			String[] arrItems = arrFilter[i].trim().split(",");
			if(arrItems.length>=2){
				setFilter.add(Integer.parseInt(arrItems[1].trim()));
			}
		}
		
		
		int numTP = 0, numFP = 0, numTN = 0, numFN = 0;
		for (int i = 0; i < arrContent.length; i++) {
			String[] arrItems = arrContent[i].trim().split(",");
//			if(setFilter.contains(i+1)){
				if (arrItems[7].trim().equals(strInconsistent)&&arrItems[8].trim().equals(strInconsistent)) {
					numTP++;
				} else if(arrItems[7].trim().equals(strInconsistent)&&arrItems[8].trim().equals(strConsistent)){
					numFP++;
				} else if(arrItems[7].trim().equals(strConsistent)&&arrItems[8].trim().equals(strInconsistent)){
					numFN++;
				} else if(arrItems[7].trim().equals(strConsistent)&&arrItems[8].trim().equals(strConsistent)){
					numTN++;
				}
//			}
			
		}
		
		System.out.println("Precision: "+numTP+"/"+(numTP+numFP)+" is "+(numTP*1.0/(numTP+numFP)));
		System.out.println("Recall: "+numTP+"/"+(numTP+numFN)+" is "+(numTP*1.0/(numTP+numFN)));

	}

}
