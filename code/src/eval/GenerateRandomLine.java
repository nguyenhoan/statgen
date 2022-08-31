package eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.core.internal.utils.FileUtil;

import utils.FileIO;

public class GenerateRandomLine {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop = "SpecMiningProject\\SummerWork2017\\full_2\\";
		//generateRandomly(1, 1677, 50);
		generateRandomExceptCurrentNumber(1, 1677, 450,fop+"checkedLine.txt");
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	public static void generateRandomExceptCurrentNumber(int min, int max,
			int number, String fileExists) {
		String[] arrExistsRow = FileIO.readStringFromFile(fileExists).split(
				"\r\n");
		HashSet<Integer> setExists = new HashSet<Integer>();
		for (int i = 0; i < arrExistsRow.length; i++) {
			setExists.add(Integer.parseInt(arrExistsRow[i]));
		}
		HashSet<Integer> setNumber = new HashSet<Integer>();
		while (setNumber.size() != number) {
			int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
			if ( ! setExists.contains( randomNum ) ) {
				setNumber.add(randomNum);
			}

		}

		for (Integer item : setNumber) {
			System.out.println(item);
		}

	}

	public static void generateRandomly(int min, int max, int number) {
		HashSet<Integer> setNumber = new HashSet<Integer>();
		while (setNumber.size() != number) {
			int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
			setNumber.add(randomNum);
		}

		for (Integer item : setNumber) {
			System.out.println(item);
		}

	}

}
