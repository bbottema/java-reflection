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

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Skree {
	public void methodWithArray(int[] ints) {
		//
	}
	
	public void methodWithCollection1(Iterable<Integer> ints) {
		//
	}
	
	public void methodWithCollection2(Collection<Double> doubles) {
		//
	}
	
	public void methodWithCollection3(List<String> ints) {
		//
	}
	
	public void methodWithCollection4(HashMap<String, String> ints) {
		//
	}
	
	public void methodWithCollection5(String[] strings, Collection<Calendar> calendars) {
		//
	}
	
	public void methodWithoutCollection1(String strings, Calendar calendars) {
		//
	}
	
	public void methodWithoutCollection2(String s) {
		//
	}
	
	public void methodWithoutCollection3() {
		//
	}
}
