package com.operators;

public class EqualToOperator implements Operator {
	public boolean exec(Double lhs, Double rhs) {
		return  lhs == rhs;
	}
}