package org.bbottema.javareflection.testmodel;

@SuppressWarnings({"unused", "SameReturnValue", "WeakerAccess"})
public class C extends B {
	public Integer numberC;
	Integer number_privateC;
	
	public C(Fruit f) {
		super(f);
	}
	
	public C(Pear p) {
		super(p);
	}
	
	@Override
	public String foo(Double value, Fruit fruit, char c) {
		return String.format("%s-%s-%s", value, fruit.getClass().getSimpleName(), c);
	}
	
	@Override
	String protectedMethod() {
		return "protected 2";
	}
	
	@SuppressWarnings("unused")
	private String privateMethod() {
		return "private 2";
	}
}