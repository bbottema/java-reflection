package org.bbottema.javareflection.testmodel;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class B extends A {
	
	public Integer numberB;
	Integer number_privateB;
	
	public static Integer numberB_static;
	
	public B(Fruit f) {
		super(f);
	}
	
	@Override
	String protectedMethod() {
		return "protected 1";
	}
}
