package com.operators;

public class MoreOrEqualThanOperator implements Operator {
	public boolean exec(Double lhs, Double rhs) {
		return  lhs >= rhs;
	}
}
