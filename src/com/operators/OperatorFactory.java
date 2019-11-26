package com.operators;

import java.util.HashMap;
import java.util.Map;

public class OperatorFactory {
	private static final Map<String, Operator> OPERATORS = new HashMap<String, Operator>();

	static {
		OPERATORS.put("==", new EqualToOperator());
		OPERATORS.put("<", new LessThanOperator());
		OPERATORS.put("<=", new LessOrEqualThanOperator());
		OPERATORS.put(">", new MoreThanOperator());
		OPERATORS.put(">=", new MoreOrEqualThanOperator());
	}

	public static Operator getOperator(String op) {
		return OPERATORS.get(op);
	}
}


