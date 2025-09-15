package com.example.generator;

import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Handles the actual code emission for Withers and Lens classes.
 * Contains well-refactored methods to avoid code duplication.
 */
public class CodeEmitter {
    
    private static final String INDENT = "    ";
    
    public void emitPackage(PrintWriter writer, String packageName) {
        writer.println("package " + packageName + ";");
        writer.println();
    }
    
    public void emitImports(PrintWriter writer, Set<String> imports) {
        imports.stream()
                .sorted()
                .forEach(imp -> writer.println("import " + imp + ";"));
        writer.println();
    }
    
    public void emitWithersClass(PrintWriter writer, Class<?> recordClass) {
        String className = recordClass.getSimpleName() + "Withers";
        String recordName = recordClass.getSimpleName();
        
        writer.println("public final class " + className + " {");
        writer.println();
        writer.println(INDENT + "private " + className + "() {}");
        writer.println();
        
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            emitWitherMethod(writer, recordName, component, components);
        }
        
        writer.println("}");
    }
    
    private void emitWitherMethod(PrintWriter writer, String recordName, RecordComponent targetComponent, RecordComponent[] allComponents) {
        String methodName = "with" + capitalize(targetComponent.getName());
        String paramType = getTypeString(targetComponent.getGenericType());
        String paramName = targetComponent.getName();
        
        writer.println(INDENT + "public static " + recordName + " " + methodName + "(" + recordName + " entity, " + paramType + " " + paramName + ") {");
        writer.println(INDENT + INDENT + "return new " + recordName + "(");
        
        for (int i = 0; i < allComponents.length; i++) {
            RecordComponent comp = allComponents[i];
            String value = comp.equals(targetComponent) ? paramName : "entity." + comp.getName() + "()";
            String suffix = (i == allComponents.length - 1) ? "" : ",";
            writer.println(INDENT + INDENT + INDENT + value + suffix);
        }
        
        writer.println(INDENT + INDENT + ");");
        writer.println(INDENT + "}");
        writer.println();
    }
    
    public void emitLensClass(PrintWriter writer, Class<?> recordClass) {
        String className = recordClass.getSimpleName() + "Lens";
        String recordName = recordClass.getSimpleName();
        String withersName = recordName + "Withers";
        
        writer.println("public final class " + className + " {");
        writer.println();
        
        // Emit convenience methods
        emitConvenienceMethods(writer, recordName);
        
        // Emit lens methods for each field
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            emitLensMethod(writer, recordName, withersName, component);
        }
        
        // Emit inner lens classes - only for record types to avoid duplicates
        Set<String> emittedClasses = new HashSet<>();
        for (RecordComponent component : components) {
            Type type = component.getGenericType();
            if (isRecordType(type)) {
                String typeName = getTypeString(type);
                if (!emittedClasses.contains(typeName)) {
                    emittedClasses.add(typeName);
                    emitNestedLensClass(writer, recordName, component);
                }
            } else if (isOptionalType(type) && isRecordType(getOptionalElementType(type))) {
                String elementType = getTypeString(getOptionalElementType(type));
                String lensClassName = "Optional" + elementType + "Lens";
                if (!emittedClasses.contains(lensClassName)) {
                    emittedClasses.add(lensClassName);
                    emitNestedLensClass(writer, recordName, component);
                }
            } else if (type.getTypeName().equals("model.RecursiveNested")) {
                if (!emittedClasses.contains("RecursiveNestedLens")) {
                    emittedClasses.add("RecursiveNestedLens");
                    emitNestedLensClass(writer, recordName, component);
                }
            }
        }
        
        writer.println("}");
    }
    
    private void emitConvenienceMethods(PrintWriter writer, String recordName) {
        writer.println(INDENT + "public static Mutations.BoundMutations<" + recordName + "> on(" + recordName + " entity) {");
        writer.println(INDENT + INDENT + "return Mutations.forValue(entity);");
        writer.println(INDENT + "}");
        writer.println();
        
        // Convenience set methods
        writer.println(INDENT + "public static <T> " + recordName + " set(" + recordName + " entity, Mutations.LensProvider<" + recordName + ", T> lensProvider, T newValue) {");
        writer.println(INDENT + INDENT + "return on(entity).set(lensProvider, newValue).apply();");
        writer.println(INDENT + "}");
        writer.println();
        
        writer.println(INDENT + "public static <T> " + recordName + " set(" + recordName + " entity, Lens<" + recordName + ", T> lens, T newValue) {");
        writer.println(INDENT + INDENT + "return on(entity).set(lens, newValue).apply();");
        writer.println(INDENT + "}");
        writer.println();
        
        // Convenience mod methods
        writer.println(INDENT + "public static <T> " + recordName + " mod(" + recordName + " entity, Mutations.LensProvider<" + recordName + ", T> lensProvider, UnaryOperator<T> modifier) {");
        writer.println(INDENT + INDENT + "return on(entity).mod(lensProvider, modifier).apply();");
        writer.println(INDENT + "}");
        writer.println();
        
        writer.println(INDENT + "public static <T> " + recordName + " mod(" + recordName + " entity, Lens<" + recordName + ", T> lens, UnaryOperator<T> modifier) {");
        writer.println(INDENT + INDENT + "return on(entity).mod(lens, modifier).apply();");
        writer.println(INDENT + "}");
        writer.println();
    }
    
    private void emitLensMethod(PrintWriter writer, String recordName, String withersName, RecordComponent component) {
        String methodName = component.getName();
        String fieldType = getTypeString(component.getGenericType());
        String withersMethod = withersName + "::with" + capitalize(methodName);
        
        Type type = component.getGenericType();
        
        if (isOptionalType(type) && isRecordType(getOptionalElementType(type))) {
            // Optional record type
            String elementType = getTypeString(getOptionalElementType(type));
            String lensClassName = "Optional" + elementType + "Lens";
            
            writer.println(INDENT + "public static " + lensClassName + " " + methodName + "() {");
            writer.println(INDENT + INDENT + "return new " + lensClassName + "(" + recordName + "::" + methodName + ", " + withersMethod + ", " + elementType + "Lens::new);");
            writer.println(INDENT + "}");
            writer.println();
            
        } else if (isListType(type) && isRecordType(getListElementType(type))) {
            // List of records
            String elementType = getTypeString(getListElementType(type));
            String lensClassName = elementType + "Lens";
            
            writer.println(INDENT + "public static ObjectListLensWrapper<" + recordName + ", " + elementType + ", " + lensClassName + "> " + methodName + "() {");
            writer.println(INDENT + INDENT + "return new ObjectListLensWrapper<>(" + recordName + "::" + methodName + ", " + withersMethod + ", " + lensClassName + "::new);");
            writer.println(INDENT + "}");
            writer.println();
            
        } else if (isMapType(type) && isRecordType(getMapValueType(type))) {
            // Map with record values
            String keyType = getTypeString(getMapKeyType(type));
            String valueType = getTypeString(getMapValueType(type));
            String lensClassName = valueType + "Lens";
            
            writer.println(INDENT + "public static ObjectMapLensWrapper<" + recordName + ", " + keyType + ", " + valueType + ", " + lensClassName + "> " + methodName + "() {");
            writer.println(INDENT + INDENT + "return new ObjectMapLensWrapper<>(" + recordName + "::" + methodName + ", " + withersMethod + ", " + lensClassName + "::new);");
            writer.println(INDENT + "}");
            writer.println();
            
        } else if (isListType(type)) {
            // List of primitives
            String elementType = getTypeString(getListElementType(type));
            
            writer.println(INDENT + "public static ListLensWrapper<" + recordName + ", " + elementType + "> " + methodName + "() {");
            writer.println(INDENT + INDENT + "return new ListLensWrapper<>(" + recordName + "::" + methodName + ", " + withersMethod + ");");
            writer.println(INDENT + "}");
            writer.println();
            
        } else if (isMapType(type)) {
            // Map of primitives
            String keyType = getTypeString(getMapKeyType(type));
            String valueType = getTypeString(getMapValueType(type));
            
            writer.println(INDENT + "public static MapLensWrapper<" + recordName + ", " + keyType + ", " + valueType + "> " + methodName + "() {");
            writer.println(INDENT + INDENT + "return new MapLensWrapper<>(" + recordName + "::" + methodName + ", " + withersMethod + ");");
            writer.println(INDENT + "}");
            writer.println();
            
        } else if (isRecordType(type)) {
            // Direct record type
            String lensClassName = getTypeString(type) + "Lens";
            
            writer.println(INDENT + "public static " + lensClassName + " " + methodName + "() {");
            writer.println(INDENT + INDENT + "return new " + lensClassName + "(" + recordName + "::" + methodName + ", " + withersMethod + ");");
            writer.println(INDENT + "}");
            writer.println();
            
        } else if (type.getTypeName().equals("model.RecursiveNested")) {
            // Special case for recursive nested
            writer.println(INDENT + "public static RecursiveNestedLens " + methodName + "() {");
            writer.println(INDENT + INDENT + "return RecursiveNestedLens.fromRequired(" + recordName + "::" + methodName + ", " + withersMethod + ");");
            writer.println(INDENT + "}");
            writer.println();
            
        } else {
            // Primitive or simple types
            writer.println(INDENT + "public static Lens<" + recordName + ", " + fieldType + "> " + methodName + "() {");
            writer.println(INDENT + INDENT + "return Lens.of(" + recordName + "::" + methodName + ", " + withersMethod + ");");
            writer.println(INDENT + "}");
            writer.println();
        }
    }
    
    private void emitNestedLensClass(PrintWriter writer, String recordName, RecordComponent component) {
        Type type = component.getGenericType();
        
        if (isRecordType(type)) {
            String typeName = getTypeString(type);
            String lensClassName = typeName + "Lens";
            emitInnerRecordLensClass(writer, recordName, lensClassName, (Class<?>) type);
            
        } else if (isOptionalType(type) && isRecordType(getOptionalElementType(type))) {
            String elementType = getTypeString(getOptionalElementType(type));
            String lensClassName = "Optional" + elementType + "Lens";
            emitOptionalRecordLensClass(writer, recordName, lensClassName, (Class<?>) getOptionalElementType(type));
        }
        
        // Handle RecursiveNested special case
        if (type.getTypeName().equals("model.RecursiveNested")) {
            emitRecursiveNestedLensClass(writer, recordName);
        }
    }
    
    private void emitInnerRecordLensClass(PrintWriter writer, String recordName, String lensClassName, Class<?> recordType) {
        writer.println(INDENT + "public static class " + lensClassName + " extends AbstractDomainLens<" + recordName + ", " + recordType.getSimpleName() + "> {");
        writer.println();
        
        // Constructors
        writer.println(INDENT + INDENT + "public " + lensClassName + "(Lens<" + recordName + ", " + recordType.getSimpleName() + "> lens) {");
        writer.println(INDENT + INDENT + INDENT + "super(lens);");
        writer.println(INDENT + INDENT + "}");
        writer.println();
        
        writer.println(INDENT + INDENT + "public " + lensClassName + "(Function<" + recordName + ", " + recordType.getSimpleName() + "> getter, BiFunction<" + recordName + ", " + recordType.getSimpleName() + ", " + recordName + "> setter) {");
        writer.println(INDENT + INDENT + INDENT + "super(getter, setter);");
        writer.println(INDENT + INDENT + "}");
        writer.println();
        
        // Field lens methods
        for (RecordComponent comp : recordType.getRecordComponents()) {
            emitNestedFieldLensMethod(writer, recordName, recordType.getSimpleName(), comp);
        }
        
        writer.println(INDENT + "}");
        writer.println();
    }
    
    private void emitOptionalRecordLensClass(PrintWriter writer, String recordName, String lensClassName, Class<?> recordType) {
        String elementType = recordType.getSimpleName();
        String elementLensClass = elementType + "Lens";
        
        writer.println(INDENT + "public static class " + lensClassName + " extends ObjectOptionalLensWrapper<" + recordName + ", " + elementType + ", " + elementLensClass + "> {");
        writer.println();
        
        // Constructor
        writer.println(INDENT + INDENT + "public " + lensClassName + "(");
        writer.println(INDENT + INDENT + INDENT + INDENT + "final Function<" + recordName + ", Optional<" + elementType + ">> getter,");
        writer.println(INDENT + INDENT + INDENT + INDENT + "final BiFunction<" + recordName + ", Optional<" + elementType + ">, " + recordName + "> setter,");
        writer.println(INDENT + INDENT + INDENT + INDENT + "final Function<Lens<" + recordName + ", " + elementType + ">, " + elementLensClass + "> lensCreator) {");
        writer.println(INDENT + INDENT + INDENT + "super(getter, setter, lensCreator);");
        writer.println(INDENT + INDENT + "}");
        writer.println();
        
        // Field lens methods
        for (RecordComponent comp : recordType.getRecordComponents()) {
            emitOptionalFieldLensMethod(writer, recordName, recordType.getSimpleName(), comp);
        }
        
        writer.println(INDENT + "}");
        writer.println();
    }
    
    private void emitRecursiveNestedLensClass(PrintWriter writer, String recordName) {
        writer.println(INDENT + "public static class RecursiveNestedLens extends AbstractDomainLens<" + recordName + ", Optional<RecursiveNested>> {");
        writer.println();
        
        writer.println(INDENT + INDENT + "private RecursiveNestedLens(Lens<" + recordName + ", Optional<RecursiveNested>> lens) {");
        writer.println(INDENT + INDENT + INDENT + "super(lens);");
        writer.println(INDENT + INDENT + "}");
        writer.println();
        
        writer.println(INDENT + INDENT + "public static RecursiveNestedLens fromRequired(Function<" + recordName + ", RecursiveNested> getter, BiFunction<" + recordName + ", RecursiveNested, " + recordName + "> setter) {");
        writer.println(INDENT + INDENT + INDENT + "return new RecursiveNestedLens(Lens.of(");
        writer.println(INDENT + INDENT + INDENT + INDENT + "entity -> Optional.of(getter.apply(entity)),");
        writer.println(INDENT + INDENT + INDENT + INDENT + "(entity, optValue) -> optValue.map(value -> setter.apply(entity, value)).orElse(entity)");
        writer.println(INDENT + INDENT + INDENT + "));");
        writer.println(INDENT + INDENT + "}");
        writer.println();
        
        writer.println(INDENT + INDENT + "public static RecursiveNestedLens fromOptional(Function<" + recordName + ", Optional<RecursiveNested>> getter, BiFunction<" + recordName + ", Optional<RecursiveNested>, " + recordName + "> setter) {");
        writer.println(INDENT + INDENT + INDENT + "return new RecursiveNestedLens(Lens.of(getter, setter));");
        writer.println(INDENT + INDENT + "}");
        writer.println();
        
        writer.println(INDENT + INDENT + "public Lens<" + recordName + ", String> value() {");
        writer.println(INDENT + INDENT + INDENT + "return this.lens.andThen(Lens.of(");
        writer.println(INDENT + INDENT + INDENT + INDENT + "opt -> opt.map(RecursiveNested::value).orElse(\"\"),");
        writer.println(INDENT + INDENT + INDENT + INDENT + "(opt, newValue) -> opt.map(nested -> new RecursiveNested(newValue, nested.child()))");
        writer.println(INDENT + INDENT + INDENT + "));");
        writer.println(INDENT + INDENT + "}");
        writer.println();
        
        writer.println(INDENT + INDENT + "public RecursiveNestedLens child() {");
        writer.println(INDENT + INDENT + INDENT + "return RecursiveNestedLens.fromOptional(");
        writer.println(INDENT + INDENT + INDENT + INDENT + "entity -> lens.get(entity).flatMap(RecursiveNested::child),");
        writer.println(INDENT + INDENT + INDENT + INDENT + "(entity, newChild) -> lens.set(entity,");
        writer.println(INDENT + INDENT + INDENT + INDENT + INDENT + "lens.get(entity).map(nested -> new RecursiveNested(nested.value(), newChild)))");
        writer.println(INDENT + INDENT + INDENT + ");");
        writer.println(INDENT + INDENT + "}");
        
        writer.println(INDENT + "}");
        writer.println();
    }
    
    private void emitNestedFieldLensMethod(PrintWriter writer, String recordName, String recordType, RecordComponent component) {
        String methodName = component.getName();
        String fieldType = getTypeString(component.getGenericType());
        String withersMethod = recordType + "Withers::with" + capitalize(methodName);
        Type type = component.getGenericType();
        
        if (isRecordType(type)) {
            String nestedLensClass = getTypeString(type) + "Lens";
            writer.println(INDENT + INDENT + "public " + nestedLensClass + " " + methodName + "() {");
            writer.println(INDENT + INDENT + INDENT + "return new " + nestedLensClass + "(entity -> lens.get(entity)." + methodName + "(), (entity, new" + capitalize(methodName) + ") -> lens.set(entity, " + withersMethod.replace("::", ".") + "(lens.get(entity), new" + capitalize(methodName) + ")));");
            writer.println(INDENT + INDENT + "}");
        } else {
            writer.println(INDENT + INDENT + "public Lens<" + recordName + ", " + fieldType + "> " + methodName + "() {");
            writer.println(INDENT + INDENT + INDENT + "return this.lens.andThen(Lens.of(" + recordType + "::" + methodName + ", " + withersMethod + "));");
            writer.println(INDENT + INDENT + "}");
        }
        writer.println();
    }
    
    private void emitOptionalFieldLensMethod(PrintWriter writer, String recordName, String recordType, RecordComponent component) {
        String methodName = component.getName();
        String fieldType = getTypeString(component.getGenericType());
        String withersMethod = recordType + "Withers::with" + capitalize(methodName);
        Type type = component.getGenericType();
        
        if (isRecordType(type)) {
            String nestedLensClass = getTypeString(type) + "Lens";
            writer.println(INDENT + INDENT + "public " + nestedLensClass + " " + methodName + "() {");
            writer.println(INDENT + INDENT + INDENT + "return createNestedLens(" + recordType + "::" + methodName + ", " + withersMethod + ", " + nestedLensClass + "::new);");
            writer.println(INDENT + INDENT + "}");
        } else {
            String defaultValue = getDefaultValue(type);
            writer.println(INDENT + INDENT + "public Lens<" + recordName + ", " + fieldType + "> " + methodName + "() {");
            writer.println(INDENT + INDENT + INDENT + "return createPropertyLens(" + recordType + "::" + methodName + ", " + withersMethod + ", " + defaultValue + ");");
            writer.println(INDENT + INDENT + "}");
        }
        writer.println();
    }
    
    // Utility methods
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private String getTypeString(Type type) {
        if (type instanceof ParameterizedType paramType) {
            Class<?> rawType = (Class<?>) paramType.getRawType();
            Type[] args = paramType.getActualTypeArguments();
            
            if (rawType == Optional.class) {
                return "Optional<" + getTypeString(args[0]) + ">";
            } else if (rawType == List.class) {
                return "List<" + getTypeString(args[0]) + ">";
            } else if (rawType == Map.class) {
                return "Map<" + getTypeString(args[0]) + ", " + getTypeString(args[1]) + ">";
            }
        }
        
        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }
        
        return type.getTypeName().replaceAll(".*\\.", "");
    }
    
    private boolean isOptionalType(Type type) {
        return type instanceof ParameterizedType paramType && 
               paramType.getRawType() == Optional.class;
    }
    
    private boolean isListType(Type type) {
        return type instanceof ParameterizedType paramType && 
               paramType.getRawType() == List.class;
    }
    
    private boolean isMapType(Type type) {
        return type instanceof ParameterizedType paramType && 
               paramType.getRawType() == Map.class;
    }
    
    private boolean isRecordType(Type type) {
        return type instanceof Class<?> clazz && clazz.isRecord();
    }
    
    private Type getOptionalElementType(Type type) {
        if (type instanceof ParameterizedType paramType && paramType.getRawType() == Optional.class) {
            return paramType.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Not an Optional type: " + type);
    }
    
    private Type getListElementType(Type type) {
        if (type instanceof ParameterizedType paramType && paramType.getRawType() == List.class) {
            return paramType.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Not a List type: " + type);
    }
    
    private Type getMapKeyType(Type type) {
        if (type instanceof ParameterizedType paramType && paramType.getRawType() == Map.class) {
            return paramType.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Not a Map type: " + type);
    }
    
    private Type getMapValueType(Type type) {
        if (type instanceof ParameterizedType paramType && paramType.getRawType() == Map.class) {
            return paramType.getActualTypeArguments()[1];
        }
        throw new IllegalArgumentException("Not a Map type: " + type);
    }
    
    private String getDefaultValue(Type type) {
        if (type == String.class) {
            return "\"\"";
        } else if (type == int.class || type == Integer.class) {
            return "0";
        } else if (type == boolean.class || type == Boolean.class) {
            return "false";
        }
        return "null";
    }
}
