package com.operators;

public class LessThanOperator implements Operator {
	public boolean exec(Double lhs, Double rhs) {
		return  lhs < rhs;
	}
}
