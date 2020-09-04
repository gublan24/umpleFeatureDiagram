package umple.featureDiagram;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import cruise.umple.compiler.FeatureLink;
import cruise.umple.compiler.FeatureLink.FeatureConnectingOpType;
import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.FeatureNode;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleInternalParser;
import cruise.umple.compiler.UmpleModel;

public class CountFeatureSAT {

	public final static String andOp = "&";
	public final static String orOp = "|";
	public final static String negOp ="~";

	public static void main(String[] args) {

		UmpleFile umpfile = new UmpleFile("BerkeleySPL.ump");
		UmpleModel model = new UmpleModel(umpfile); // CompoundFeatureNode l;
		UmpleInternalParser pp;
		FeatureNode d;
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
		String logicalFormula = "";
		
		if (featureNode == null)
			return "";

		ArrayList<String> optionalFeatures = new ArrayList<String>();
		ArrayList<String> orFeatures = new ArrayList<String>();
		ArrayList<String> xorFeatures = new ArrayList<String>();
		ArrayList<String> andFeatures = new ArrayList<String>();
		ArrayList<String> includeFeatures = new ArrayList<String>();
		ArrayList<String> excludeFeatures = new ArrayList<String>();
		ArrayList<String> childParentList = new ArrayList<String>();

		List<FeatureLink> outgoingLinks = featureNode.getOutgoingFeatureLinks();

		for (FeatureLink link : outgoingLinks) {

			String featureName = link.getTargetFeatureNode().getName(); 
			FeatureConnectingOpType featureConnectionType = link.getFeatureConnectingOpType();

			if (featureConnectionType.equals(FeatureConnectingOpType.Include)) {
				includeFeatures.add(featureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Exclude)) {
				excludeFeatures.add(featureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Mandatory)) {
				andFeatures.add(featureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Optional)) { // return (2^n)
				optionalFeatures.add(featureName);
			}
			if (link.getTargetFeatureNode().getName().equals("and")){ 
				FeatureNode andTarget = link.getTargetFeatureNode();
				List<FeatureLink> andLinks = andTarget.getOutgoingFeatureLinks();
				for (FeatureLink targetFeatureLink : andLinks) {
					andFeatures.add(targetFeatureLink.getTargetFeatureNode().getName());
				}
			}
			if (link.getTargetFeatureNode().getName().equals("or")){ // return (2^n) - 1
				FeatureNode orTarget = link.getTargetFeatureNode();
				List<FeatureLink> orLinks = orTarget.getOutgoingFeatureLinks();
				for (FeatureLink targetFeatureLink : orLinks) {
					orFeatures.add(targetFeatureLink.getTargetFeatureNode().getName());
				}
			}

			if (link.getTargetFeatureNode().getName().equals("xor")) {
				FeatureNode xorTarget = link.getTargetFeatureNode();
				List<FeatureLink> xorLinks = xorTarget.getOutgoingFeatureLinks();
				for (FeatureLink targetFeatureLink : xorLinks) {
					xorFeatures.add(targetFeatureLink.getTargetFeatureNode().getName());
				}

			}

		}


		String andGroup = "";
		String optGroup = "";
		String xorGroup = "";

		if (andFeatures.size() > 0) {
			andGroup = formLogicalSentence(featureNode, andFeatures, andOp);
		}
		if (optionalFeatures.size() > 0) {
			optGroup = formLogicalSentenceWithImplicationToSource(featureNode, optionalFeatures, orOp);
		}

		if (xorFeatures.size() > 0) {
			// prepare xor to be only one value 
			xorFeatures = xorCombinations(xorFeatures);
			xorGroup = formLogicalSentence(featureNode,xorFeatures,orOp);
			xorGroup += ") & " + featureNode.getName() + " ) ";
		}

		String connect = "";
		if (andGroup.trim().length() > 1) {
			System.out.println(andGroup);
			logicalFormula += andGroup;
			connect = " " + andOp + " ";
		}
		if (optGroup.trim().length() > 1) {
			logicalFormula += connect + optGroup;
			connect = " " + andOp + " ";
		}
		if (xorGroup.trim().length() > 1) {
			logicalFormula += connect + xorGroup;
		}

		if (logicalFormula.trim().length() > 0)
			logicalFormula = "(" + logicalFormula + ")\n";

		for (FeatureLink l : featureNode.getOutgoingFeatureLinks()) {
			String res = getAllValid(l.getTargetFeatureNode());
			if (!res.trim().equals(""))
				if(res.trim().startsWith(andOp))
					logicalFormula += res;
				else
				logicalFormula += "\n" + andOp + res;
		}
		return logicalFormula;

	}

	private static String formLogicalSentence(FeatureNode featureNode, ArrayList<String> featureList, String joinOp) {
		String sentence = "(";
		for (String featureName : featureList) {
			sentence += featureName + joinOp + " ";
		}
		sentence = sentence.substring(0, sentence.lastIndexOf(joinOp));
		sentence += ")";
		return sentence;
	}
	private static String formLogicalSentenceWithImplicationToSource(FeatureNode featureNode, ArrayList<String> optionalFeatures, String joinOp) {
		String sentence = "( ";
		sentence += formLogicalSentence(featureNode, optionalFeatures,joinOp);
		sentence += " => " + featureNode.getName() + " ) ";
		return sentence;
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

	public static ArrayList<String> combinations(List<String> inputArray) {
		ArrayList<String> aSolution = new ArrayList<String>();
		// Start i at 1, so that we do not include the empty set in the results
		for (long i = 1; i < Math.pow(2, inputArray.size()); i++) {
			String comb = "(";

			for (int j = 0; j < inputArray.size(); j++) {
				if ((i & (long) Math.pow(2, j)) > 0) {
					// Include j in set
					comb = comb + inputArray.get(j) + " " + andOp + " ";
				}
			}
			comb = comb.substring(0, comb.lastIndexOf(andOp) - 1);
			comb += ")";
			aSolution.add(comb);

		}
		return aSolution;
	}

	
	public static ArrayList<String> xorCombinations(List<String> inputArray) {
		ArrayList<String> aSolution = new ArrayList<String>();
		// Start i at 1, so that we do not include the empty set in the results
		for (int i = 0; i < inputArray.size(); i++) {
			String comb = "( "+ inputArray.get(i);
			for (int j=0; j < inputArray.size(); j++) {
				if(j==i)
					continue;
					comb = comb +" " + andOp +" "+ negOp  +" "+inputArray.get(j) + " " + andOp + " ";
				}	
			comb = comb.substring(0, comb.lastIndexOf(andOp) - 1);
			comb += ")";
			
			aSolution.add(comb);

		}
		return aSolution;
	}
//	public static ArrayList<String> optCombinations(String[] inputArray) {
//		ArrayList<String> combinationList = combinations(inputArray);
//
//		combinationList.add("TRUE ");
//		return combinationList;
//	}

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
