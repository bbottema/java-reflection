package org.bbottema.javareflection.testmodel;

import java.util.Calendar;

public class Shmoo {
	@Meta("Shmoo.method1-A")
	protected void method1(Integer i) {}
	@Meta("Shmoo.method1-B")
	protected void method1(String i) {}
	@Meta("Shmoo.method1-C")
	protected void method1(Double d, Calendar c) {}
	
	@Meta("Shmoo.method2-A")
	protected void method2(Integer i) {}
}
