/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	// specficially don't use Bean setter convention here, to avoid breaking existing Bean-related tests
	public void updateNumberC(Integer value) {
		numberC = value;
	}

	// specficially don't use Bean setter convention here, to avoid breaking existing Bean-related tests
	public void updateNumber_privateC(Integer value) {
		number_privateC = value;
	}
}