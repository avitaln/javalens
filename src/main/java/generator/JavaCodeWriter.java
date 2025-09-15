package generator;

import java.io.PrintWriter;
import java.util.Set;

/**
 * Well-refactored helper class for writing Java code.
 * Eliminates repetitive indentation and formatting code.
 */
public class JavaCodeWriter {
    
    private final PrintWriter writer;
    private int indentLevel = 0;
    private static final String INDENT = "    ";
    
    public interface CodeBlock {
        void write();
    }
    
    public JavaCodeWriter(PrintWriter writer) {
        this.writer = writer;
    }
    
    public void writePackage(String packageName) {
        writer.println("package " + packageName + ";");
        writeBlankLine();
    }
    
    public void writeImports(Set<String> imports) {
        imports.stream()
                .sorted()
                .forEach(imp -> writer.println("import " + imp + ";"));
        writeBlankLine();
    }
    
    public void writeClassDeclaration(String declaration, CodeBlock body) {
        writeLine(declaration + " {");
        writeBlankLine();
        increaseIndent();
        body.write();
        decreaseIndent();
        writeLine("}");
    }
    
    public void writeMethod(String signature, CodeBlock body) {
        writeLine(signature + " {");
        increaseIndent();
        body.write();
        decreaseIndent();
        writeLine("}");
    }
    
    public void writePrivateConstructor(String className) {
        writeLine("private " + className + "() {}");
    }
    
    public void writeLine(String line) {
        if (line.trim().isEmpty()) {
            writer.println();
        } else {
            writer.println(getCurrentIndent() + line);
        }
    }
    
    public void writeBlankLine() {
        writer.println();
    }
    
    public void increaseIndent() {
        indentLevel++;
    }
    
    public void decreaseIndent() {
        if (indentLevel > 0) {
            indentLevel--;
        }
    }
    
    private String getCurrentIndent() {
        return INDENT.repeat(indentLevel);
    }
}
