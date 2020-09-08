package umple.featureDiagram;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import cruise.umple.compiler.FeatureLink;
import cruise.umple.compiler.FeatureLink.FeatureConnectingOpType;
import cruise.umple.compiler.FeatureModel;
import cruise.umple.compiler.FeatureNode;
import cruise.umple.compiler.UmpleFile;
import cruise.umple.compiler.UmpleModel;

public class FeatureModelPropositionalGenerator {

	public final static String AND_OP = " & ";
	public final static String OR_OP = " | ";
	public final static String NEG_OP= "~";
	public final static String IMP_OP = " => ";

	public static void main(String[] args) {

		UmpleFile umpfile = new UmpleFile("featureDependencySPL.ump");
		UmpleModel model = new UmpleModel(umpfile); // CompoundFeatureNode l;

		model.setShouldGenerate(true);
		model.run();
		FeatureModel fmodel = model.getFeatureModel();

		FeatureNode rootNode = fmodel.getNode().get(0);
		String featureModelLogicalFormualAsString = getPropostionalFormualFromRootFeatureNode(rootNode) ;

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
		else if(featureNode.getOutgoingFeatureLinks().size() < 1)
			return propositionalFormula;

		ArrayList<String> optionalFeatures = new ArrayList<String>();
		ArrayList<String> orFeatures= new ArrayList<String>();
		ArrayList<String> xorFeatures = new ArrayList<String>();
		ArrayList<String> mandatoryFeatures = new ArrayList<String>();
		ArrayList<String> includeExcludeFeatures = new ArrayList<String>();
		
		for (FeatureLink aFeatureLink : featureNode.getOutgoingFeatureLinks()) {
			String sourceFeatureName = aFeatureLink.getSourceFeatureNode().getName();
			String targetFeatureName = aFeatureLink.getTargetFeatureNode().getName();
			FeatureConnectingOpType featureConnectionType = aFeatureLink.getFeatureConnectingOpType();

			if (featureConnectionType.equals(FeatureConnectingOpType.Include)) {
				includeExcludeFeatures.add(sourceFeatureName + IMP_OP + targetFeatureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Exclude)) {
				includeExcludeFeatures.add(sourceFeatureName + IMP_OP + NEG_OP + targetFeatureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Mandatory)) {
				mandatoryFeatures.add(targetFeatureName);
			}
			if (featureConnectionType.equals(FeatureConnectingOpType.Optional)) { 
				optionalFeatures.add(targetFeatureName);
			}	
			if (aFeatureLink.getTargetFeatureNode().getName().equals("and")) {
				mandatoryFeatures.addAll(obtainFeatureNamesAsStringList(aFeatureLink));
			}
			if (aFeatureLink.getTargetFeatureNode().getName().equals("or")) { 
				orFeatures.addAll(obtainFeatureNamesAsStringList(aFeatureLink));
			}
			if (aFeatureLink.getTargetFeatureNode().getName().equals("xor")) {
				xorFeatures.addAll(obtainFeatureNamesAsStringList(aFeatureLink));
			}

		}

		String andGroup = "";
		String optGroup = "";
		String xorGroup = "";
		String orGroup = "";
		String childImp = "";
		String inExcludeGroup = "";
		
		//process mandatory features
		if (mandatoryFeatures.size() > 0) {
			andGroup = formLogicalImpFromSourceToChild(featureNode, mandatoryFeatures, AND_OP);
			childImp = formLogicalImp(featureNode, mandatoryFeatures, AND_OP);
			andGroup += AND_OP + childImp;
		}
		//process optional features
		if (optionalFeatures.size() > 0) {
			optGroup = formLogicalSentenceWithImplicationToSource(featureNode, optionalFeatures, OR_OP);
		}
		//process xor features
		if (xorFeatures.size() > 0) {
			// prepare xor to be only one value
			ArrayList<String> xorComb = xorCombinations(xorFeatures);
			childImp = formLogicalImp(featureNode, xorFeatures, AND_OP);
			xorGroup = "(" + featureNode.getName() + IMP_OP + formLogicalSentence(xorComb, OR_OP);
			xorGroup += ") " + AND_OP + childImp;
		}
		//process or features
		if (orFeatures.size() > 0) {
			childImp = formLogicalImp(featureNode, orFeatures, AND_OP);
			orGroup = "(" + featureNode.getName() + IMP_OP + formLogicalSentence(orFeatures, OR_OP);
			orGroup += ") " + AND_OP + childImp;
		}
		//process include/exclude features
		if(includeExcludeFeatures.size() > 0)
		{
			inExcludeGroup=formLogicalSentence(includeExcludeFeatures, AND_OP,true);
		}

		String connect = "";
		if (andGroup.trim().length() > 1) {
			propositionalFormula += andGroup;
			connect = AND_OP;
		}
		if (optGroup.trim().length() > 1) {
			propositionalFormula += connect + optGroup;
			connect = AND_OP;
		}
		if (xorGroup.trim().length() > 1) {
			propositionalFormula += connect + xorGroup;
			connect = AND_OP;

		}
		if (orGroup.trim().length() > 1) {
			propositionalFormula += connect + orGroup;
			connect = AND_OP;
		}
		if (inExcludeGroup.trim().length() > 1) {
			propositionalFormula += connect + inExcludeGroup;
			connect = AND_OP;
		}

		if (propositionalFormula.trim().length() > 0)
			propositionalFormula +="\n";// "(" + propositionalFormula + ")\n";
	
		for (FeatureLink aFeatureLink : featureNode.getOutgoingFeatureLinks()) {
			String aLogicalFormula = getProppositionalLogicFromFeatureNode(aFeatureLink.getTargetFeatureNode());
			if (aLogicalFormula.trim().equals(""))
				continue;
			if(propositionalFormula.endsWith(AND_OP))
				propositionalFormula += aLogicalFormula;
			else
			propositionalFormula += AND_OP + aLogicalFormula;
		}		
		return propositionalFormula;

	}
	private static List<String> obtainFeatureNamesAsStringList(FeatureLink aFeatureLink) {
		ArrayList<String> innerFeatures = new ArrayList<String>();
		FeatureNode targetFeatureNode = aFeatureLink.getTargetFeatureNode();
		List<FeatureLink> outgoingFeatureLinks = targetFeatureNode.getOutgoingFeatureLinks();
		for (FeatureLink targetFeatureLink : outgoingFeatureLinks) {
			innerFeatures.add(targetFeatureLink.getTargetFeatureNode().getName());
		}
		return innerFeatures;
		
	}
	
	private static String formLogicalSentence( ArrayList<String> featureList, String joinOp , boolean withBracket) {
		String sentence = "(";
		String openBracket = "";
		String closeBracket = " ";
		if (withBracket && featureList.size()> 1 ) {
			openBracket = "(";
			closeBracket = ")";
		}
		
		for (String featureName : featureList) {
			sentence += openBracket+featureName + closeBracket + joinOp ;
		}
		if (featureList.size() > 0)
			sentence = sentence.substring(0, sentence.lastIndexOf(joinOp));
		sentence += ")";
		return sentence;
	}

	private static String formLogicalSentence( ArrayList<String> featureList, String joinOp) {
		return formLogicalSentence(featureList,joinOp,false);
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
		sentence += formLogicalSentence(optionalFeatures, joinOp);
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
