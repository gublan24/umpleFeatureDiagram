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

	public final static String AND_OP = " & ";
	public final static String OR_OP = " | ";
	public final static String NEG_OP= "~";
	public final static String IMP_OP = " => ";

	public static void main(String[] args) {

		UmpleFile umpfile = new UmpleFile("BerkeleySPL.ump");
		UmpleModel model = new UmpleModel(umpfile); // CompoundFeatureNode l;
		UmpleInternalParser pp;
		FeatureNode d;
		model.setShouldGenerate(true);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();

		FeatureNode rootNode = fmodel.getNode().get(0);
		String featureModelLogicalFormualAsString = getPropostionalFormualFromRootFeatureNode(rootNode) ;

		FeatureLink fLink;
		model.generate();

		try {
			FileWriter myWriter = new FileWriter("logEasyFeatureSPL.txt");
			myWriter.write(featureModelLogicalFormualAsString);
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}
	private static String getPropostionalFormualFromRootFeatureNode(FeatureNode rootNode) {
		return rootNode.getName() + AND_OP + getProppositionalLogicFromFeatureNode(rootNode);
	}
	public static String getProppositionalLogicFromFeatureNode(FeatureNode featureNode) {
		String propositionalFormula = "";
		if (featureNode == null)
			return propositionalFormula;
		
		ArrayList<String> optionalFeatures = new ArrayList<String>();
		ArrayList<String> orFeatures = new ArrayList<String>();
		ArrayList<String> xorFeatures = new ArrayList<String>();
		ArrayList<String> mandatoryFeatures = new ArrayList<String>();
		ArrayList<String> includeFeatures = new ArrayList<String>();
		ArrayList<String> excludeFeatures = new ArrayList<String>();

		List<FeatureLink> outgoingLinks = featureNode.getOutgoingFeatureLinks();

		for (FeatureLink aFeatureLink : outgoingLinks) {

			String featureName = aFeatureLink.getTargetFeatureNode().getName();
			FeatureConnectingOpType featureConnectionType = aFeatureLink.getFeatureConnectingOpType();

			if (featureConnectionType.equals(FeatureConnectingOpType.Include)) {
				includeFeatures.add(featureName); // source --> target 
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Exclude)) {
				excludeFeatures.add(featureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Mandatory)) {
				mandatoryFeatures.add(featureName);

			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Optional)) { // return (2^n)
				optionalFeatures.add(featureName);
			}
			
			if (aFeatureLink.getTargetFeatureNode().getName().equals("and")) {
				getInnerFeatureNode(mandatoryFeatures, aFeatureLink);
			}
			if (aFeatureLink.getTargetFeatureNode().getName().equals("or")) { // return (2^n) - 1
				getInnerFeatureNode(orFeatures, aFeatureLink);
			}
			if (aFeatureLink.getTargetFeatureNode().getName().equals("xor")) {
				getInnerFeatureNode(xorFeatures, aFeatureLink);
			}

		}

		String andGroup = "";
		String optGroup = "";
		String xorGroup = "";
		String childImp = "";
		String includeGroup = "";


		if (mandatoryFeatures.size() > 0) {
			andGroup = formLogicalImpFromSourceToChild(featureNode, mandatoryFeatures, AND_OP);
			childImp = formLogicalImp(featureNode, mandatoryFeatures, AND_OP);
			andGroup += AND_OP + childImp;
		}
		if (optionalFeatures.size() > 0) {
			optGroup = formLogicalSentenceWithImplicationToSource(featureNode, optionalFeatures, OR_OP);
		}
		if (xorFeatures.size() > 0) {
			// prepare xor to be only one value
			ArrayList<String> xorComb = xorCombinations(xorFeatures);
			childImp = formLogicalImp(featureNode, xorFeatures, AND_OP);
			xorGroup = "(" + featureNode.getName() + IMP_OP + formLogicalSentence(featureNode, xorComb, OR_OP); // convert
																												// xor
																												// to
																												// ORs
																												// of
																												// ands.
			xorGroup += ") " + AND_OP + childImp;
		}
		if(includeFeatures.size() > 0)
		{
			
		}

		String connect = "";
		if (andGroup.trim().length() > 1) {
			System.out.println(andGroup);
			propositionalFormula += andGroup;
			connect = " " + AND_OP + " ";
		}
		if (optGroup.trim().length() > 1) {
			propositionalFormula += connect + optGroup;
			connect = " " + AND_OP + " ";
		}
		if (xorGroup.trim().length() > 1) {
			propositionalFormula += connect + xorGroup;
		}

		if (propositionalFormula.trim().length() > 0)
			propositionalFormula = "(" + propositionalFormula + ")\n";

		for (FeatureLink aFeatureLink : featureNode.getOutgoingFeatureLinks()) {
			String aLogicalFormula = getProppositionalLogicFromFeatureNode(aFeatureLink.getTargetFeatureNode());
			if (aLogicalFormula.trim().isEmpty())
				continue;
			propositionalFormula += "\n" + AND_OP + aLogicalFormula;
		}
		return propositionalFormula;

	}
	private static void getInnerFeatureNode(ArrayList<String> innerFeatures, FeatureLink aFeatureLink) {
		FeatureNode andTarget = aFeatureLink.getTargetFeatureNode();
		List<FeatureLink> andLinks = andTarget.getOutgoingFeatureLinks();
		for (FeatureLink targetFeatureLink : andLinks) {
			innerFeatures.add(targetFeatureLink.getTargetFeatureNode().getName());
		}
	}

	private static String formLogicalSentence(FeatureNode featureNode, ArrayList<String> featureList, String joinOp) {
		String sentence = "(";
		for (String featureName : featureList) {
			sentence += featureName + joinOp + " ";
		}
		if (featureList.size() > 0)
			sentence = sentence.substring(0, sentence.lastIndexOf(joinOp));
		sentence += ")";
		return sentence;
	}

	private static String formLogicalImpFromSourceToChild(FeatureNode featureNode, ArrayList<String> featureList,
			String joinOp) {
		String sentence = "(";
		String openBracket = "";
		String closeBracket = " ";
		if (featureList.size() > 1) {
			openBracket = "(";
			closeBracket = ")";
		}
		for (String featureName : featureList) {
			sentence += openBracket + " " + featureNode.getName() + IMP_OP + featureName + closeBracket + joinOp;
		}
		if (featureList.size() > 0)
			sentence = sentence.substring(0, sentence.lastIndexOf(joinOp));
		sentence += ")";
		return sentence;
	}

	private static String formLogicalImp(FeatureNode featureNode, ArrayList<String> featureList, String joinOp) {
		String sentence = "(";
		String openBracket = "";
		String closeBracket = " ";
		if (featureList.size() > 1) {
			openBracket = "(";
			closeBracket = ")";
		}
		for (String featureName : featureList) {
			sentence += openBracket + " " + featureName + IMP_OP + featureNode.getName() + closeBracket + joinOp;
		}
		if (featureList.size() > 0)
			sentence = sentence.substring(0, sentence.lastIndexOf(joinOp));
		sentence += ")";
		return sentence;
	}

	private static String formLogicalSentenceWithImplicationToSource(FeatureNode featureNode,
			ArrayList<String> optionalFeatures, String joinOp) {
		String sentence = "( ";
		sentence += formLogicalSentence(featureNode, optionalFeatures, joinOp);
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
					comb = comb + inputArray.get(j) + " " + AND_OP + " ";
				}
			}
			comb = comb.substring(0, comb.lastIndexOf(AND_OP) - 1);
			comb += ")";
			aSolution.add(comb);

		}
		return aSolution;
	}

	public static ArrayList<String> xorCombinations(List<String> inputArray) {
		ArrayList<String> aSolution = new ArrayList<String>();
		// Start i at 1, so that we do not include the empty set in the results
		for (int i = 0; i < inputArray.size(); i++) {
			String comb = "( " + inputArray.get(i);
			for (int j = 0; j < inputArray.size(); j++) {
				if (j == i)
					continue;
				comb = comb + " " + AND_OP + " " + NEG_OP + " " + inputArray.get(j) + " " + AND_OP + " ";
			}
			comb = comb.substring(0, comb.lastIndexOf(AND_OP) - 1);
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
				result = result + " " + OR_OP + " " + inputArray[i];
			}
			result += exta + ")";
		}
		return result;
	}

}
