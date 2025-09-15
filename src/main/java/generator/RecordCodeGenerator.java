package generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic code generator for Withers and Lens classes from ANY Java record.
 * Uses reflection to analyze records and generates immutable update utilities.
 * Highly refactored to avoid code repetition.
 */
public class RecordCodeGenerator {
    
    private final String targetPackage;
    private final Path outputDirectory;
    
    public RecordCodeGenerator(String targetPackage, Path outputDirectory) {
        this.targetPackage = targetPackage;
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Generate Withers and Lens classes for the given record class
     */
    public void generateForRecord(Class<?> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Class must be a record: " + recordClass.getName());
        }
        
        try {
            Files.createDirectories(outputDirectory);
            generateWithers(recordClass);
            generateLens(recordClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate code for " + recordClass.getSimpleName(), e);
        }
    }
    
    /**
     * Generate code for a main record class and discover all nested records
     */
    public void generateForMainRecord(Class<?> mainRecordClass) {
        if (!mainRecordClass.isRecord()) {
            throw new IllegalArgumentException("Class must be a record: " + mainRecordClass.getName());
        }
        
        try {
            Files.createDirectories(outputDirectory);
            
            // Generate for the main record
            System.out.println("Generating code for main record: " + mainRecordClass.getSimpleName());
            generateForRecord(mainRecordClass);
            
            // Discover and generate withers for all nested record types
            Set<Class<?>> nestedRecordTypes = discoverNestedRecordTypes(mainRecordClass);
            for (Class<?> nestedType : nestedRecordTypes) {
                System.out.println("Generating withers for nested record: " + nestedType.getSimpleName());
                generateWithers(nestedType);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate code for main record: " + mainRecordClass.getSimpleName(), e);
        }
    }
    
    private void generateWithers(Class<?> recordClass) throws IOException {
        String className = recordClass.getSimpleName() + "Withers";
        Path filePath = outputDirectory.resolve(className + ".java");
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            JavaCodeWriter codeWriter = new JavaCodeWriter(writer);
            
            codeWriter.writePackage(targetPackage);
            codeWriter.writeImports(getWithersImports(recordClass));
            codeWriter.writeClassDeclaration("public final class " + className, () -> {
                codeWriter.writePrivateConstructor(className);
                codeWriter.writeBlankLine();
                
                RecordComponent[] components = recordClass.getRecordComponents();
                for (RecordComponent component : components) {
                    generateWitherMethod(codeWriter, recordClass, component, components);
                }
            });
        }
    }
    
    private void generateLens(Class<?> recordClass) throws IOException {
        String className = recordClass.getSimpleName() + "Lens";
        Path filePath = outputDirectory.resolve(className + ".java");
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            JavaCodeWriter codeWriter = new JavaCodeWriter(writer);
            
            codeWriter.writePackage(targetPackage);
            codeWriter.writeImports(getLensImports(recordClass));
            codeWriter.writeClassDeclaration("public final class " + className, () -> {
                generateConvenienceMethods(codeWriter, recordClass);
                
                RecordComponent[] components = recordClass.getRecordComponents();
                for (RecordComponent component : components) {
                    generateLensMethod(codeWriter, recordClass, component);
                }
                
                // Generate inner lens classes for nested records
                generateAllInnerLensClasses(codeWriter, recordClass, components);
            });
        }
    }
    
    private void generateWitherMethod(JavaCodeWriter writer, Class<?> recordClass, 
                                     RecordComponent component, RecordComponent[] allComponents) {
        String methodName = "with" + capitalize(component.getName());
        String paramType = getTypeString(component.getGenericType());
        String paramName = component.getName();
        String recordName = recordClass.getSimpleName();
        
        writer.writeMethod("public static " + recordName + " " + methodName + 
                          "(" + recordName + " entity, " + paramType + " " + paramName + ")", () -> {
            writer.writeLine("return new " + recordName + "(");
            writer.increaseIndent();
            for (int i = 0; i < allComponents.length; i++) {
                RecordComponent comp = allComponents[i];
                String value = comp.equals(component) ? paramName : "entity." + comp.getName() + "()";
                String suffix = (i == allComponents.length - 1) ? "" : ",";
                writer.writeLine(value + suffix);
            }
            writer.decreaseIndent();
            writer.writeLine(");");
        });
        writer.writeBlankLine();
    }
    
    private void generateConvenienceMethods(JavaCodeWriter writer, Class<?> recordClass) {
        String recordName = recordClass.getSimpleName();
        
        writer.writeMethod("public static Mutations.BoundMutations<" + recordName + "> on(" + recordName + " entity)", () -> {
            writer.writeLine("return Mutations.forValue(entity);");
        });
        writer.writeBlankLine();
        
        // Generic set methods
        writer.writeMethod("public static <T> " + recordName + " set(" + recordName + " entity, Mutations.LensProvider<" + recordName + ", T> lensProvider, T newValue)", () -> {
            writer.writeLine("return on(entity).set(lensProvider, newValue).apply();");
        });
        writer.writeBlankLine();
        
        writer.writeMethod("public static <T> " + recordName + " set(" + recordName + " entity, Lens<" + recordName + ", T> lens, T newValue)", () -> {
            writer.writeLine("return on(entity).set(lens, newValue).apply();");
        });
        writer.writeBlankLine();
        
        // Generic mod methods
        writer.writeMethod("public static <T> " + recordName + " mod(" + recordName + " entity, Mutations.LensProvider<" + recordName + ", T> lensProvider, UnaryOperator<T> modifier)", () -> {
            writer.writeLine("return on(entity).mod(lensProvider, modifier).apply();");
        });
        writer.writeBlankLine();
        
        writer.writeMethod("public static <T> " + recordName + " mod(" + recordName + " entity, Lens<" + recordName + ", T> lens, UnaryOperator<T> modifier)", () -> {
            writer.writeLine("return on(entity).mod(lens, modifier).apply();");
        });
        writer.writeBlankLine();
    }
    
    private void generateLensMethod(JavaCodeWriter writer, Class<?> recordClass, RecordComponent component) {
        String methodName = component.getName();
        String recordName = recordClass.getSimpleName();
        String withersName = recordName + "Withers";
        String fieldType = getTypeString(component.getGenericType());
        Type type = component.getGenericType();
        
        if (isRecursiveNestedType(type)) {
            // Special case for recursive record
            String recursiveTypeName = getTypeString(type);
            String recursiveLensClassName = recursiveTypeName + "Lens";
            writer.writeMethod("public static " + recursiveLensClassName + " " + methodName + "()", () -> {
                writer.writeLine("return " + recursiveLensClassName + ".fromRequired(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ");");
            });
        } else if (isRecordType(type)) {
            // Direct record type
            String lensClassName = getTypeString(type) + "Lens";
            writer.writeMethod("public static " + lensClassName + " " + methodName + "()", () -> {
                writer.writeLine("return new " + lensClassName + "(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ");");
            });
        } else if (isOptionalType(type) && isRecordType(getOptionalElementType(type))) {
            // Optional record type
            String elementType = getTypeString(getOptionalElementType(type));
            String lensClassName = "Optional" + elementType + "Lens";
            writer.writeMethod("public static " + lensClassName + " " + methodName + "()", () -> {
                writer.writeLine("return new " + lensClassName + "(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ", " + elementType + "Lens::new);");
            });
        } else if (isListType(type) && isRecordType(getListElementType(type))) {
            // List of records
            String elementType = getTypeString(getListElementType(type));
            String lensClassName = elementType + "Lens";
            writer.writeMethod("public static ObjectListLensWrapper<" + recordName + ", " + elementType + ", " + lensClassName + "> " + methodName + "()", () -> {
                writer.writeLine("return new ObjectListLensWrapper<>(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ", " + lensClassName + "::new);");
            });
        } else if (isMapType(type) && isRecordType(getMapValueType(type))) {
            // Map with record values
            String keyType = getTypeString(getMapKeyType(type));
            String valueType = getTypeString(getMapValueType(type));
            String lensClassName = valueType + "Lens";
            writer.writeMethod("public static ObjectMapLensWrapper<" + recordName + ", " + keyType + ", " + valueType + ", " + lensClassName + "> " + methodName + "()", () -> {
                writer.writeLine("return new ObjectMapLensWrapper<>(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ", " + lensClassName + "::new);");
            });
        } else if (isListType(type)) {
            // List of primitives
            String elementType = getTypeString(getListElementType(type));
            writer.writeMethod("public static ListLensWrapper<" + recordName + ", " + elementType + "> " + methodName + "()", () -> {
                writer.writeLine("return new ListLensWrapper<>(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ");");
            });
        } else if (isMapType(type)) {
            // Map of primitives
            String keyType = getTypeString(getMapKeyType(type));
            String valueType = getTypeString(getMapValueType(type));
            writer.writeMethod("public static MapLensWrapper<" + recordName + ", " + keyType + ", " + valueType + "> " + methodName + "()", () -> {
                writer.writeLine("return new MapLensWrapper<>(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ");");
            });
        } else {
            // Primitive or simple types
            writer.writeMethod("public static Lens<" + recordName + ", " + fieldType + "> " + methodName + "()", () -> {
                writer.writeLine("return Lens.of(" + recordName + "::" + methodName + ", " + withersName + "::with" + capitalize(methodName) + ");");
            });
        }
        writer.writeBlankLine();
    }
    
    private void generateAllInnerLensClasses(JavaCodeWriter writer, Class<?> recordClass, RecordComponent[] components) {
        Set<String> generatedClasses = new HashSet<>();
        Set<Class<?>> allNestedTypes = discoverNestedRecordTypes(recordClass);
        
        // Generate inner lens classes for all discovered nested record types (except RecursiveNested)
        for (Class<?> nestedType : allNestedTypes) {
            if (!isRecursiveNestedType(nestedType)) {  // Skip RecursiveNested - it's handled specially
                String lensClassName = nestedType.getSimpleName() + "Lens";
                if (!generatedClasses.contains(lensClassName)) {
                    generatedClasses.add(lensClassName);
                    generateInnerLensClass(writer, recordClass, nestedType);
                }
            }
        }
        
        // Generate optional lens classes for optional nested records
        for (RecordComponent component : components) {
            Type type = component.getGenericType();
            
            if (isOptionalType(type) && isRecordType(getOptionalElementType(type))) {
                Class<?> nestedType = (Class<?>) getOptionalElementType(type);
                String lensClassName = "Optional" + nestedType.getSimpleName() + "Lens";
                if (!generatedClasses.contains(lensClassName)) {
                    generatedClasses.add(lensClassName);
                    generateOptionalInnerLensClass(writer, recordClass, nestedType);
                }
            } else if (isRecursiveNestedType(type)) {
                // Special case for recursive record
                Class<?> recursiveType = (Class<?>) type;
                String lensClassName = recursiveType.getSimpleName() + "Lens";
                if (!generatedClasses.contains(lensClassName)) {
                    generatedClasses.add(lensClassName);
                    generateRecursiveLensClass(writer, recordClass, recursiveType);
                }
            }
        }
    }
    
    private void generateInnerLensClass(JavaCodeWriter writer, Class<?> recordClass, Class<?> nestedRecordClass) {
        String recordName = recordClass.getSimpleName();
        String nestedName = nestedRecordClass.getSimpleName();
        String lensClassName = nestedName + "Lens";
        String nestedWithersName = nestedName + "Withers";
        
        writer.writeClassDeclaration("public static class " + lensClassName + " extends AbstractDomainLens<" + recordName + ", " + nestedName + ">", () -> {
            // Constructors
            writer.writeMethod("public " + lensClassName + "(Lens<" + recordName + ", " + nestedName + "> lens)", () -> {
                writer.writeLine("super(lens);");
            });
            writer.writeBlankLine();
            
            writer.writeMethod("public " + lensClassName + "(Function<" + recordName + ", " + nestedName + "> getter, BiFunction<" + recordName + ", " + nestedName + ", " + recordName + "> setter)", () -> {
                writer.writeLine("super(getter, setter);");
            });
            writer.writeBlankLine();
            
            // Field lens methods for nested record
            RecordComponent[] nestedComponents = nestedRecordClass.getRecordComponents();
            for (RecordComponent component : nestedComponents) {
                generateNestedFieldLensMethod(writer, recordName, nestedName, nestedWithersName, component);
            }
        });
        writer.writeBlankLine();
    }
    
    private void generateNestedFieldLensMethod(JavaCodeWriter writer, String recordName, String nestedName, 
                                             String nestedWithersName, RecordComponent component) {
        String methodName = component.getName();
        String fieldType = getTypeString(component.getGenericType());
        Type type = component.getGenericType();
        
        if (isRecordType(type)) {
            String nestedLensClass = getTypeString(type) + "Lens";
            writer.writeMethod("public " + nestedLensClass + " " + methodName + "()", () -> {
                writer.writeLine("return new " + nestedLensClass + "(entity -> lens.get(entity)." + methodName + "(), (entity, new" + capitalize(methodName) + ") -> lens.set(entity, " + nestedWithersName + ".with" + capitalize(methodName) + "(lens.get(entity), new" + capitalize(methodName) + ")));");
            });
        } else {
            writer.writeMethod("public Lens<" + recordName + ", " + fieldType + "> " + methodName + "()", () -> {
                writer.writeLine("return this.lens.andThen(Lens.of(" + nestedName + "::" + methodName + ", " + nestedWithersName + "::with" + capitalize(methodName) + "));");
            });
        }
        writer.writeBlankLine();
    }
    
    private void generateOptionalInnerLensClass(JavaCodeWriter writer, Class<?> recordClass, Class<?> nestedRecordClass) {
        String recordName = recordClass.getSimpleName();
        String nestedName = nestedRecordClass.getSimpleName();
        String lensClassName = "Optional" + nestedName + "Lens";
        String nestedLensClassName = nestedName + "Lens";
        String nestedWithersName = nestedName + "Withers";
        
        writer.writeClassDeclaration("public static class " + lensClassName + " extends ObjectOptionalLensWrapper<" + recordName + ", " + nestedName + ", " + nestedLensClassName + ">", () -> {
            // Constructor
            writer.writeLine("public " + lensClassName + "(");
            writer.increaseIndent();
            writer.writeLine("final Function<" + recordName + ", Optional<" + nestedName + ">> getter,");
            writer.writeLine("final BiFunction<" + recordName + ", Optional<" + nestedName + ">, " + recordName + "> setter,");
            writer.writeLine("final Function<Lens<" + recordName + ", " + nestedName + ">, " + nestedLensClassName + "> lensCreator) {");
            writer.decreaseIndent();
            writer.increaseIndent();
            writer.writeLine("super(getter, setter, lensCreator);");
            writer.decreaseIndent();
            writer.writeLine("}");
            writer.writeBlankLine();
            
            // Field lens methods for nested record
            RecordComponent[] nestedComponents = nestedRecordClass.getRecordComponents();
            for (RecordComponent component : nestedComponents) {
                generateOptionalNestedFieldLensMethod(writer, recordName, nestedName, nestedWithersName, component);
            }
        });
        writer.writeBlankLine();
    }
    
    private void generateOptionalNestedFieldLensMethod(JavaCodeWriter writer, String recordName, String nestedName, 
                                                     String nestedWithersName, RecordComponent component) {
        String methodName = component.getName();
        String fieldType = getTypeString(component.getGenericType());
        Type type = component.getGenericType();
        
        if (isRecordType(type)) {
            String nestedLensClass = getTypeString(type) + "Lens";
            writer.writeMethod("public " + nestedLensClass + " " + methodName + "()", () -> {
                writer.writeLine("return this.<" + nestedLensClass + ", " + getTypeString(type) + ">createNestedLens(" + nestedName + "::" + methodName + ", " + nestedWithersName + "::with" + capitalize(methodName) + ", " + nestedLensClass + "::new);");
            });
        } else {
            String defaultValue = getDefaultValue(type);
            writer.writeMethod("public Lens<" + recordName + ", " + fieldType + "> " + methodName + "()", () -> {
                writer.writeLine("return createPropertyLens(" + nestedName + "::" + methodName + ", " + nestedWithersName + "::with" + capitalize(methodName) + ", " + defaultValue + ");");
            });
        }
        writer.writeBlankLine();
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
    
    private void generateRecursiveLensClass(JavaCodeWriter writer, Class<?> recordClass, Class<?> recursiveRecordClass) {
        String recordName = recordClass.getSimpleName();
        String recursiveTypeName = recursiveRecordClass.getSimpleName();
        String recursiveLensClassName = recursiveTypeName + "Lens";
        String recursiveWithersName = recursiveTypeName + "Withers";
        
        writer.writeClassDeclaration("public static class " + recursiveLensClassName + " extends AbstractDomainLens<" + recordName + ", Optional<" + recursiveTypeName + ">>", () -> {
            writer.writeMethod("private " + recursiveLensClassName + "(Lens<" + recordName + ", Optional<" + recursiveTypeName + ">> lens)", () -> {
                writer.writeLine("super(lens);");
            });
            writer.writeBlankLine();
            
            writer.writeMethod("public static " + recursiveLensClassName + " fromRequired(Function<" + recordName + ", " + recursiveTypeName + "> getter, BiFunction<" + recordName + ", " + recursiveTypeName + ", " + recordName + "> setter)", () -> {
                writer.writeLine("return new " + recursiveLensClassName + "(Lens.of(");
                writer.increaseIndent();
                writer.writeLine("entity -> Optional.of(getter.apply(entity)),");
                writer.writeLine("(entity, optValue) -> optValue.map(value -> setter.apply(entity, value)).orElse(entity)");
                writer.decreaseIndent();
                writer.writeLine("));");
            });
            writer.writeBlankLine();
            
            writer.writeMethod("public static " + recursiveLensClassName + " fromOptional(Function<" + recordName + ", Optional<" + recursiveTypeName + ">> getter, BiFunction<" + recordName + ", Optional<" + recursiveTypeName + ">, " + recordName + "> setter)", () -> {
                writer.writeLine("return new " + recursiveLensClassName + "(Lens.of(getter, setter));");
            });
            writer.writeBlankLine();
            
            // Generate lens methods for all fields of the recursive record
            RecordComponent[] recursiveComponents = recursiveRecordClass.getRecordComponents();
            for (RecordComponent component : recursiveComponents) {
                generateRecursiveFieldLensMethod(writer, recordName, recursiveTypeName, recursiveLensClassName, recursiveWithersName, component);
            }
        });
        writer.writeBlankLine();
    }
    
    private void generateRecursiveFieldLensMethod(JavaCodeWriter writer, String recordName, String recursiveTypeName, 
                                                String recursiveLensClassName, String recursiveWithersName, RecordComponent component) {
        String methodName = component.getName();
        String fieldType = getTypeString(component.getGenericType());
        Type type = component.getGenericType();
        
        if (isOptionalType(type) && getOptionalElementType(type).equals(component.getDeclaringRecord())) {
            // Self-referencing optional field (like Optional<RecursiveNested> child)
            writer.writeMethod("public " + recursiveLensClassName + " " + methodName + "()", () -> {
                writer.writeLine("return " + recursiveLensClassName + ".fromOptional(");
                writer.increaseIndent();
                writer.writeLine("entity -> lens.get(entity).flatMap(" + recursiveTypeName + "::" + methodName + "),");
                writer.writeLine("(entity, new" + capitalize(methodName) + ") -> lens.set(entity,");
                writer.increaseIndent();
                writer.writeLine("lens.get(entity).map(nested -> " + recursiveWithersName + ".with" + capitalize(methodName) + "(nested, new" + capitalize(methodName) + ")))");
                writer.decreaseIndent();
                writer.decreaseIndent();
                writer.writeLine(");");
            });
        } else {
            // Regular field (like String value)
            writer.writeMethod("public Lens<" + recordName + ", " + fieldType + "> " + methodName + "()", () -> {
                writer.writeLine("return this.lens.andThen(Lens.of(");
                writer.increaseIndent();
                String defaultValue = getDefaultValue(type);
                writer.writeLine("opt -> opt.map(" + recursiveTypeName + "::" + methodName + ").orElse(" + defaultValue + "),");
                writer.writeLine("(opt, new" + capitalize(methodName) + ") -> opt.map(nested -> " + recursiveWithersName + ".with" + capitalize(methodName) + "(nested, new" + capitalize(methodName) + "))");
                writer.decreaseIndent();
                writer.writeLine("));");
            });
        }
        writer.writeBlankLine();
    }
    
    // Utility methods
    
    private Set<String> getWithersImports(Class<?> recordClass) {
        Set<String> imports = new HashSet<>();
        for (RecordComponent component : recordClass.getRecordComponents()) {
            addImportsForType(imports, component.getGenericType());
        }
        return imports;
    }
    
    private Set<String> getLensImports(Class<?> recordClass) {
        Set<String> imports = new HashSet<>();
        imports.add("lib.AbstractDomainLens");
        imports.add("lib.Lens");
        imports.add("lib.Mutations");
        imports.add("java.util.function.BiFunction");
        imports.add("java.util.function.Function");
        imports.add("java.util.function.UnaryOperator");
        
        for (RecordComponent component : recordClass.getRecordComponents()) {
            Type type = component.getGenericType();
            addImportsForType(imports, type);
            addLensImportsForType(imports, type);
        }
        
        return imports;
    }
    
    private void addImportsForType(Set<String> imports, Type type) {
        if (type instanceof ParameterizedType paramType) {
            Class<?> rawType = (Class<?>) paramType.getRawType();
            if (rawType == Optional.class) {
                imports.add("java.util.Optional");
            } else if (rawType == List.class) {
                imports.add("java.util.List");
            } else if (rawType == Map.class) {
                imports.add("java.util.Map");
            }
            
            for (Type argType : paramType.getActualTypeArguments()) {
                addImportsForType(imports, argType);
            }
        }
    }
    
    private void addLensImportsForType(Set<String> imports, Type type) {
        if (type instanceof ParameterizedType paramType) {
            Class<?> rawType = (Class<?>) paramType.getRawType();
            if (rawType == List.class) {
                imports.add("lib.ListLensWrapper");
                imports.add("lib.ObjectListLensWrapper");
            } else if (rawType == Map.class) {
                imports.add("lib.MapLensWrapper");
                imports.add("lib.ObjectMapLensWrapper");
            } else if (rawType == Optional.class) {
                imports.add("lib.OptionalLensWrapper");
                imports.add("lib.ObjectOptionalLensWrapper");
            }
        }
    }
    
    /**
     * Recursively discover all nested record types starting from a main record
     */
    private Set<Class<?>> discoverNestedRecordTypes(Class<?> recordClass) {
        Set<Class<?>> allNestedTypes = new HashSet<>();
        Set<Class<?>> visited = new HashSet<>();
        discoverNestedRecordTypesRecursive(recordClass, allNestedTypes, visited);
        return allNestedTypes;
    }
    
    private void discoverNestedRecordTypesRecursive(Class<?> recordClass, Set<Class<?>> allNestedTypes, Set<Class<?>> visited) {
        if (visited.contains(recordClass)) {
            return; // Avoid infinite recursion
        }
        visited.add(recordClass);
        
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            Type type = component.getGenericType();
            Class<?> nestedRecordType = null;
            
            if (isRecordType(type)) {
                nestedRecordType = (Class<?>) type;
            } else if (isOptionalType(type) && isRecordType(getOptionalElementType(type))) {
                nestedRecordType = (Class<?>) getOptionalElementType(type);
            } else if (isListType(type) && isRecordType(getListElementType(type))) {
                nestedRecordType = (Class<?>) getListElementType(type);
            } else if (isMapType(type) && isRecordType(getMapValueType(type))) {
                nestedRecordType = (Class<?>) getMapValueType(type);
            }
            
            if (nestedRecordType != null && !allNestedTypes.contains(nestedRecordType)) {
                allNestedTypes.add(nestedRecordType);
                // Recursively discover nested types within this nested type
                discoverNestedRecordTypesRecursive(nestedRecordType, allNestedTypes, visited);
            }
        }
    }

    private Set<Class<?>> getNestedRecordTypes(RecordComponent[] components) {
        Set<Class<?>> nestedTypes = new HashSet<>();
        for (RecordComponent component : components) {
            Type type = component.getGenericType();
            if (isRecordType(type)) {
                nestedTypes.add((Class<?>) type);
            } else if (isOptionalType(type) && isRecordType(getOptionalElementType(type))) {
                nestedTypes.add((Class<?>) getOptionalElementType(type));
            } else if (isListType(type) && isRecordType(getListElementType(type))) {
                nestedTypes.add((Class<?>) getListElementType(type));
            } else if (isMapType(type) && isRecordType(getMapValueType(type))) {
                nestedTypes.add((Class<?>) getMapValueType(type));
            }
        }
        return nestedTypes;
    }
    
    private boolean hasRecursiveNested(RecordComponent[] components) {
        return Arrays.stream(components)
                .anyMatch(comp -> isRecursiveNestedType(comp.getGenericType()));
    }
    
    private boolean isRecursiveNestedType(Type type) {
        return type instanceof Class<?> clazz && 
               isRecursiveRecord(clazz);
    }
    
    private boolean isRecursiveNestedType(Class<?> clazz) {
        return isRecursiveRecord(clazz);
    }
    
    /**
     * Detect if a record is recursive (has a field of its own type or Optional of its own type)
     */
    private boolean isRecursiveRecord(Class<?> recordClass) {
        if (!recordClass.isRecord()) {
            return false;
        }
        
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            Type fieldType = component.getGenericType();
            
            // Direct self-reference
            if (fieldType.equals(recordClass)) {
                return true;
            }
            
            // Optional self-reference
            if (isOptionalType(fieldType)) {
                Type optionalElementType = getOptionalElementType(fieldType);
                if (optionalElementType.equals(recordClass)) {
                    return true;
                }
            }
            
            // Could add List<Self> or Map<?, Self> detection here if needed
        }
        
        return false;
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
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
    
    /**
     * Main method to run the generator
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java RecordCodeGenerator <target-package> <output-dir> <main-record-class>");
            System.exit(1);
        }
        
        String targetPackage = args[0];
        Path outputDir = Paths.get(args[1]);
        String mainRecordClassName = args[2];
        
        try {
            Files.createDirectories(outputDir);
            RecordCodeGenerator generator = new RecordCodeGenerator(targetPackage, outputDir);
            
            // Add package prefix if not provided
            if (!mainRecordClassName.contains(".")) {
                mainRecordClassName = targetPackage + "." + mainRecordClassName;
            }
            
            Class<?> mainRecordClass = Class.forName(mainRecordClassName);
            System.out.println("Generating code for main record: " + mainRecordClass.getSimpleName());
            generator.generateForMainRecord(mainRecordClass);
            
            System.out.println("Code generation completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Code generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
