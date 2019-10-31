package org.bbottema.javareflection.testmodel;

@SuppressWarnings({"unused", "SameReturnValue", "WeakerAccess"})
public abstract class A implements Foo {
	
	public Integer numberA;
	Integer number_privateA;
	
	public A(Fruit f) {
	}
	
	abstract String protectedMethod();
	
	@SuppressWarnings("unused")
	private String privateMethod() {
		return "private 1";
	}
	
	public void bar() {}
}