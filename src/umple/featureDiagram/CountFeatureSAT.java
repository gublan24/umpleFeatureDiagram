package umple.featureDiagram;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import cruise.umple.analysis.AndOpAnalyzer;
import cruise.umple.compiler.FeatureLink;
import cruise.umple.compiler.FeatureLink.FeatureConnectingOpType;
import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.FeatureNode;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleModel;
import java.util.logging.Logger;

import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Literal;
import org.logicng.formulas.Variable;

public class CountFeatureSAT {

	public final static String andOp = "&";
	public final static String orOp = "|";

	public static void main(String[] args) {

		UmpleFile umpfile = new UmpleFile("BerkeleySPL.ump");
		UmpleModel model = new UmpleModel(umpfile);

		model.setShouldGenerate(true);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();

		FeatureNode rootNode = fmodel.getNode().get(0);
		String s = getAllValid(rootNode);

		FeatureLink fLink;
		model.generate();

		try {
			FileWriter myWriter = new FileWriter("logEasyFeatureSPL.txt");
			myWriter.write(s);
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	public static String getAllValid(FeatureNode featureNode) {
		String logicalFormula = " ";

		if (featureNode == null)
			return "";
		else if (featureNode.getSourceFeatureLink() == null)
			return "";

		ArrayList<String> optionalFeatures = new ArrayList<String>();
		ArrayList<String> orFeatures = new ArrayList<String>();
		ArrayList<String> xorFeatures = new ArrayList<String>();
		ArrayList<String> andFeatures = new ArrayList<String>();

		List<FeatureLink> outgoingLinks = featureNode.getSourceFeatureLink();

		for (FeatureLink link : outgoingLinks) {
			String featureName = link.getTargetFeature().get(0).getName(); // get the feature name of the target.
			FeatureConnectingOpType featureConnectionType = link.getFeatureConnectingOpType();

			if (featureConnectionType.equals(FeatureConnectingOpType.Include)) { // This must be mandutory
				String childFormula = getAllValid(link.getTargetFeature().get(0));
				if (!childFormula.trim().equals(""))
					andFeatures.add(featureName + " " + andOp + " " + childFormula + " "); // <==>
				else
					andFeatures.add(featureName);

			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Optional)) { // return (2^n)
				String childFormula = getAllValid(link.getTargetFeature().get(0));
				if (!childFormula.trim().equals(""))
					optionalFeatures.add(featureName + " " + andOp + "  \t" + childFormula + " ");
				else
					optionalFeatures.add(featureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Disjunctive)) { // return (2^n) - 1
				String childFormula = getAllValid(link.getTargetFeature().get(0));
				if (!childFormula.trim().equals("or")) {
					orFeatures.add(featureName);
				} else
					orFeatures.add(" ^ " + childFormula + " ");

			}

			if (featureConnectionType.equals(FeatureConnectingOpType.XOR)) {
				String childFormula = getAllValid(link.getTargetFeature().get(0));
				if (!featureName.trim().equals("xor")) {
					xorFeatures.add(featureName);
				} else
					xorFeatures.add(featureName + " " + andOp + "   " + childFormula + " ");

			}

		}

		logicalFormula = " ";

		String andGroup = "";
		String optGrouo = "";
		String xorGroup = "";

		if (andFeatures.size() > 0) {
			andGroup += "(";
			for (String s : andFeatures) {
				andGroup += s + " " + andOp + " ";
			}
			andGroup = andGroup.substring(0, andGroup.lastIndexOf(andOp) - 1);
			andGroup += ")";
		}
		if (optionalFeatures.size() > 0) {
			optGrouo += "( (";
			for (String comb : optionalFeatures) {
				optGrouo += comb + orOp + " ";
			}
			optGrouo = optGrouo.substring(0, optGrouo.lastIndexOf(orOp));
			optGrouo += ") => " + featureNode.getName() + " ) ";

		}

		if (xorFeatures.size() > 0) {
			ArrayList<String> result = optCombinations(optionalFeatures.toArray(new String[optionalFeatures.size()]));
			xorGroup += "<<";
			for (String comb : result) {
				optGrouo += comb + " ";
			}
			xorGroup += ">>";

		}

		String connect = "";

		if (andGroup.trim().length() > 1) {
			System.out.println(andGroup);
			logicalFormula += andGroup;
			connect = " " + andOp + " ";

		}
		if (optGrouo.trim().length() > 1) {
			logicalFormula += connect + optGrouo;
			connect = " " + andOp + " ";

		}
		if (xorGroup.trim().length() > 1) {
			logicalFormula += connect + xorGroup;

		}
		// requiredFeatures += combinationsAsString(orFeatures.toArray(new
		// String[orFeatures.size()]), "\n");
		// requiredFeatures = obatinFromList(requiredFeatures, xorFeatures, a ->
		// combinations(a)); // xorFeatures,

		if (logicalFormula.trim().length() > 0)
			logicalFormula = "(" + logicalFormula + ")\n";
		return logicalFormula;

	}

	private static String obatinFromList(String predicateString, ArrayList<String> featureArrayList,
			Function<String[], List<ArrayList<String>>> combinationMrthod) {
		if (featureArrayList.size() > 0) {
			String[] featureAsArray = new String[featureArrayList.size()];
			for (int i = 0; i < featureAsArray.length; i++) {
				featureAsArray[i] = featureArrayList.get(i);
			}

			List<ArrayList<String>> allCombinationOfInputList = combinationMrthod.apply(featureAsArray);
			if (allCombinationOfInputList.size() > 0) {
				predicateString += " [ ";

				for (ArrayList<String> solution : allCombinationOfInputList) {
					predicateString += " ( ";
					for (String single : solution) {
						predicateString += "  " + single.toString();
						if (solution.indexOf(single) + 1 != solution.size())
							predicateString += " ^ ";
					}
					predicateString += " ) ";
					if (allCombinationOfInputList.indexOf(solution) + 1 != allCombinationOfInputList.size())

						predicateString += " v ";
					predicateString += " \n ";

				}

				predicateString += " ] ";

			}
			predicateString += " \n ";

		}
		return predicateString;
	}

	public static ArrayList<String> combinations(String[] inputArray) {
		ArrayList<String> aSolution = new ArrayList<String>();
		// Start i at 1, so that we do not include the empty set in the results
		for (long i = 1; i < Math.pow(2, inputArray.length); i++) {
			String comb = "";

			for (int j = 0; j < inputArray.length; j++) {
				if ((i & (long) Math.pow(2, j)) > 0) {
					// Include j in set
					comb = comb + inputArray[j] + " " + andOp + " ";
				}
			}
			comb = comb.substring(0, comb.lastIndexOf(andOp) - 1);
			aSolution.add(comb);

		}
		return aSolution;
	}

	public static ArrayList<String> optCombinations(String[] inputArray) {
		ArrayList<String> combinationList = combinations(inputArray);

		combinationList.add("TRUE ");
		return combinationList;
	}

	public static String combinationsAsString(String[] inputArray, String exta) {
		String result = "";
		int arraySize = inputArray.length;
		if (arraySize < 1) {
			return result;
		} else if (arraySize == 1) {
			return "(" + inputArray[0] + exta + ")";
		} else {

			result = "(" + inputArray[0];
			for (int i = 1; i < arraySize; i++) {
				result = result + " " + orOp + " " + inputArray[i];
			}
			result += exta + ")";
		}
		return result;
	}

}
