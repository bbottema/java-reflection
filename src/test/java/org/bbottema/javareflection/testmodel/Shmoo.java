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
