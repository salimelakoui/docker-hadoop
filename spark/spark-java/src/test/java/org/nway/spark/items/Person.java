package org.nway.spark.items;

import java.io.Serializable;

import scala.math.BigInt;

public class Person implements Serializable {

	private String name;
	private BigInt age;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigInt getAge() {
		return age;
	}

	public void setAge(BigInt age) {
		this.age = age;
	}
}