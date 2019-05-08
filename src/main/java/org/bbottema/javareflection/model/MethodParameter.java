package org.bbottema.javareflection.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

@Value
@SuppressFBWarnings(justification = "Generated code")
public class MethodParameter {
    final int index;
    final Class<?> type;
    final Type genericType;
    final Collection<Annotation> annotations;
}