package apps;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;

	/**
	 * Scalar symbols in the expression
	 */
	ArrayList<ScalarSymbol> scalars;

	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;

	/**
	 * String containing all delimiters (characters other than variables and
	 * constants), to be used with StringTokenizer
	 */
	public static final String delims = " \t*+-/()[]";

	/**
	 * Initializes this Expression object with an input expression. Sets all
	 * other fields to null.
	 * 
	 * @param expr
	 *            Expression
	 */
	public Expression(String expr) {
		this.expr = expr;
	}

	/**
	 * Populates the scalars and arrays lists with symbols for scalar and array
	 * variables in the expression. For every variable, a SINGLE symbol is
	 * created and stored, even if it appears more than once in the expression.
	 * At this time, values for all variables are set to zero - they will be
	 * loaded from a file in the loadSymbolValues method.
	 */
	public void buildSymbols() {
		/** COMPLETE THIS METHOD **/

		String tmpExpr = expr.replaceAll(" ", ""); // eliminate spaces
		
		StringTokenizer check = new StringTokenizer(tmpExpr, delims);
		String add = "";
		arrays = new ArrayList<ArraySymbol>();
		ArraySymbol a;
		scalars = new ArrayList<ScalarSymbol>();
		ScalarSymbol s;
		
		if(check.countTokens() == 1){
			add = check.nextToken();
			if(add.charAt(add.length()-1) == ']'){
				a = new ArraySymbol(add);
				arrays.add(a);
			}
			else {
				s = new ScalarSymbol(add);
				scalars.add(s);
			}
		}
		
		
		while(check.hasMoreTokens()){
			add = check.nextToken();
			if(Character.isDigit(add.charAt(0))){
				continue;
			}
			int position = tmpExpr.indexOf(add)+add.length(); // CHECK LENGTH
			// (varx+vary*varz[(vara+varb[(a+b)*33])])/55
			if(position < tmpExpr.length()-1 && tmpExpr.charAt(position) == '['){ // doesn't work if array is end
				a = new ArraySymbol(add);
				if(arrays.contains(a)){
					continue;
				}
				arrays.add(a);  // adjusts the string expression to guarantee indexOf finds first index
				tmpExpr = tmpExpr.substring(position+1);
			}
			else
			{
				s = new ScalarSymbol(add);
				if(scalars.contains(s)){
					continue;
				}
				scalars.add(s);
				if(tmpExpr.length()!=1 && position+1 < tmpExpr.length()){
				tmpExpr = tmpExpr.substring(position+1);
				}
			}
		}
		
	//	for(int i = 0; i < arrays.size(); i++){
	//		System.out.println("Arrays: " +arrays.get(i));
	//	}
		
	//	for(int i = 0; i < arrays.size(); i++){
	//		System.out.println("Scalars: " +arrays.get(i));
	//	}
		
	}

	/**
	 * Loads values for symbols in the expression
	 * 
	 * @param sc
	 *            Scanner for values input
	 * @throws IOException
	 *             If there is a problem with the input
	 */
	public void loadSymbolValues(Scanner sc) throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String sym = st.nextToken();
			ScalarSymbol ssymbol = new ScalarSymbol(sym);
			ArraySymbol asymbol = new ArraySymbol(sym);
			int ssi = scalars.indexOf(ssymbol);
			int asi = arrays.indexOf(asymbol);
			if (ssi == -1 && asi == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				scalars.get(ssi).value = num;
			} else { // array symbol
				asymbol = arrays.get(asi);
				asymbol.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					asymbol.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression, using RECURSION to evaluate subexpressions and
	 * to evaluate array subscript expressions.
	 * 
	 * @return Result of evaluation
	 */
	public float evaluate() {
		/** COMPLETE THIS METHOD **/
		expr = expr.replaceAll(" ", "");	
		return recursEval(expr);
	}
	
	private float recursEval(String expr){
		
		Stack<String> operations = new Stack<String>(); 
		Stack<Float> values = new Stack<Float>();
		StringTokenizer st = new StringTokenizer(expr,delims,true);
		int i = 0;
		int parenCount = 0;
		int bracketExpr = 0;
		int bracketCount = 0;
		String arrName = "";
		String scalarName = "";
		boolean containsBracket = false;
		for(int a = 0; a < expr.length(); a++){
			if(expr.charAt(a) == '['){
				containsBracket = true;
				break;
			}
		}
		
		if(expr.length() == 1){
			if(Character.isDigit(expr.charAt(0))){
			return Integer.parseInt(expr);
			}
			else {
				scalarName = expr;
				values.push(ScalarValue(scalarName));
				return values.pop();
			}
		} 
		while(st.hasMoreTokens()){ //populates stack with expression
			String tmp = "";
			tmp = st.nextToken();
			if(Character.isDigit(tmp.charAt(0))){
		//		System.out.println(tmp + ":Values Pushed");
				i = i + tmp.length();
				values.push(Float.parseFloat(tmp));
			}
			else if(containsBracket == true && Character.isLetter(tmp.charAt(0)) 
					&& expr.charAt(expr.indexOf(tmp)+tmp.length()) == '['){
				
				arrName = tmp;
		//		System.out.println(arrName + " :arrayName");
				i = i+tmp.length();
			}
			else if(Character.isLetter(tmp.charAt(0))){
				scalarName = tmp;
		//		System.out.println(tmp + " :var pushed");
				values.push(ScalarValue(scalarName));
			}
			else if(tmp.equals("[")){
				
		//		System.out.println(expr.substring(expr.indexOf('[')+1) + ":Bracket Expression going into recursion");
				bracketExpr = (int) recursEval(expr.substring(expr.indexOf('[')+1));
				values.push(ArrayValue(arrName,bracketExpr));
				
				bracketCount = 1;
				i = expr.indexOf('[')+1;
			
				while(i < expr.length()){
					if(bracketCount == 0){
						break;
					}
					if(Character.isDigit(expr.charAt(i)) || Character.isLetter(expr.charAt(i))){
						tmp = st.nextToken();
						i = i + tmp.length();
					}
					else if(expr.charAt(i) == '*' || expr.charAt(i) == '-' || expr.charAt(i) == '(' ||
							expr.charAt(i) == '+' || expr.charAt(i) == '/' || expr.charAt(i) == ')'){
						tmp = st.nextToken();
						i++;
					}
					else if(expr.charAt(i) == '['){
						bracketCount++;
						i++;
						tmp = st.nextToken();
						
					}
					else { // expr.charAt(i) == ']'
						bracketCount --;
						i++;
						tmp = st.nextToken();
					}
				}
				
				if(i < expr.length()-1){
				expr = expr.substring(i);
				StringTokenizer newSt = new StringTokenizer(expr,delims,true);
				st = newSt;
				}
				else {
					break;
				}
				
					
				
			}
			else if(tmp.equals("]")){
				break;
			}
			
			else if(tmp.equals("(")){
				
		//		System.out.println(expr.substring(expr.indexOf('(')+1) + ":Parentheses Expression going into recursive method");
				String parenEval = expr.substring(expr.indexOf("(")+1);
				
				values.push(recursEval(parenEval));
				
				parenCount = 1;
				i = expr.indexOf('(')+1;
					
				
				
				while(i < expr.length() && st.hasMoreTokens()){
					if(parenCount == 0){
						break;
					}
					if(Character.isDigit(expr.charAt(i)) || Character.isLetter(expr.charAt(i))){
						tmp = st.nextToken();
						i = i + tmp.length();
					}
					else if(expr.charAt(i) == '*' || expr.charAt(i) == '-' || expr.charAt(i) == ']' ||
							expr.charAt(i) == '+' || expr.charAt(i) == '/' || expr.charAt(i) == '['){
						tmp = st.nextToken();
					//	tmp=st.nextToken();
						i++;
					}
					else if(expr.charAt(i) == '('){
						parenCount++;
						i++;
						tmp = st.nextToken();
						
					}
					else { // expr.charAt(i) == ')'
						parenCount --;
						i++;
						tmp = st.nextToken();
					}
				}
				
				if(i < expr.length()-1){
				expr = expr.substring(i);
				StringTokenizer newSt = new StringTokenizer(expr,delims,true);
				st = newSt;
				}
				else {
					break;
				}
				
				
				
			}		
			else if(tmp.equals(")")){
				break;
			}		
			else {
				
				if(operations.size()>0 && operations.peek().equals("-") && values.size()>1){
					float tmpPop = 0;
					tmpPop = values.pop()*-1;
					values.push(tmpPop);
					operations.pop();
					operations.push("+");
				}
				while(values.size() > 1 && operations.isEmpty() == false 
						&& checkPrecedence(tmp, operations.peek())){ // check precedence operations
					values.push(evaluateTop(values,operations));
				}
		//		System.out.println(tmp + ":Operation Pushed");
				i++;
				operations.push(tmp);
			}
			
		}
		
		while(values.size() > 1){ // evaluates final pluses and minuses 
			if(operations.size() > 0 && values.size() > 1) {
				values.push(evaluateTop(values,operations));
			}
		}
	
	//	System.out.println(values.peek()+ ": return value");
		return values.pop();
	}
	
	private float ArrayValue(String arrName,int index){
		
		for(int i = 0; i < arrays.size(); i++){
			if(arrays.get(i).name.equals(arrName)){
				return arrays.get(i).values[index];
			}
		}
		
		return 0;
	}
	
	private float ScalarValue(String scalarName){
		for(int i=0;i<scalars.size();i++){
			if(scalars.get(i).name.equals(scalarName)){
				return scalars.get(i).value;
			}
		}
		return 0;
	}
	
	private float evaluateTop(Stack<Float> values, Stack<String> operations){
		float operand2 = values.pop();
		String operation = operations.pop();
		float operand1 = values.pop();
		if(operation.equals("*")){
			return operand1 * operand2;
		}
		else if(operation.equals("/")){
			return operand1 / operand2;
		}
		else if(operation.equals("+")){
			return operand1 + operand2;
		}
		else {
			return operand1 - operand2;
		}
		
	}
	
	private boolean checkPrecedence(String op1, String op2){ // returns true if 
		if(op1.equals("(") || op1.equals(")") || op2.equals("(") || op2.equals(")")){ // parentheses should be executed first anyways
			return false;
		}
		else if( ( (op1.equals("*")) || (op1.equals("/")) ) && 
				( (op2.equals("+")) || (op2.equals("-")) ) ){
			return false;
		}
		else {
			return true;
		}
		
	}
	

	/**
	 * Utility method, prints the symbols in the scalars list
	 */
	public void printScalars() {
		for (ScalarSymbol ss : scalars) {
			System.out.println(ss);
		}
	}

	/**
	 * Utility method, prints the symbols in the arrays list
	 */
	public void printArrays() {
		for (ArraySymbol as : arrays) {
			System.out.println(as);
		}
	}
	
	
}	