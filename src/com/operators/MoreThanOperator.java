package com.operators;

public class MoreThanOperator implements Operator {
	public boolean exec(Double lhs, Double rhs) {
		return  lhs > rhs;
	}
}
