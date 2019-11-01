package org.bbottema.javareflection.testmodel;

public class Moo extends Shmoo {
	@Override
	@Meta("Moo.method1-A")
	protected void method1(Integer i) {}
	@Meta("Moo.method1-B")
	protected void method1(Object o) {}
	@Meta("Moo.method1-C")
	protected void method1(Object o, Integer i) {}
	
	@Meta("Moo.method2-A")
	protected void method2(Object i) {}
}
