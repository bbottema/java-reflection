package org.bbottema.javareflection.model;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.AccessibleObject;

@Data
@SuppressFBWarnings(justification = "Generated code")
public class InvokableObject<T extends AccessibleObject> {
	@NonNull T method;
	@NonNull Class<?>[] inputSignature;
	@NonNull Class<?>[] compatibleSignature;
}