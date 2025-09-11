import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LensCodeGenerator {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: LensCodeGenerator <fullyQualifiedRecordClass> <outputDir>");
            System.exit(1);
        }
        String fqcn = args[0];
        String outDir = args[1];
        Class<?> root = Class.forName(fqcn);
        if (!root.isRecord()) {
            throw new IllegalArgumentException("Provided class is not a record: " + fqcn);
        }
        String code = generateForRoot(root);
        String outName = simpleName(root) + "LensGenerated.java";
        Path outPath = Path.of(outDir, outName);
        try {
            Files.writeString(outPath, code, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        System.out.println("Wrote " + outPath);
    }

    private static String generateForRoot(Class<?> root) {
        StringBuilder sb = new StringBuilder();
        String rootName = simpleName(root);
        String className = rootName + "LensGenerated";
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Map;\n\n");
        sb.append("public final class ").append(className).append(" {\n\n");

        // Root-level simple lenses and nodes
        List<RecordComponent> components = Arrays.asList(root.getRecordComponents());
        // Pre-generate constructor arg list builder for setters
        for (RecordComponent rc : components) {
            Type t = rc.getGenericType();
            String field = rc.getName();
            if (isListOfRecord(t)) {
                // list of records -> Node
                String nodeClass = upper(field) + "Node";
                sb.append("    public static final ").append(nodeClass).append(" ").append(field)
                  .append(" = new ").append(nodeClass).append("();\n\n");
            } else if (isMapWithRecordValue(t)) {
                String nodeClass = upper(field) + "Node";
                sb.append("    public static final ").append(nodeClass).append(" ").append(field)
                  .append(" = new ").append(nodeClass).append("();\n\n");
            } else if (isRecordType(rc.getType())) {
                String nodeClass = upper(field) + "Node";
                sb.append("    public static final ").append(nodeClass).append(" ").append(field)
                  .append(" = new ").append(nodeClass).append("();\n\n");
            } else {
                // simple lens (includes List<String> etc.)
                sb.append("    public static final Lens<").append(rootName).append(", ")
                  .append(typeString(t)).append("> ").append(field).append(" = Lens.of(\n");
                sb.append("            ").append(rootName).append("::").append(field).append(",\n");
                sb.append("            (it, newValue) -> new ").append(rootName).append("(")
                  .append(constructorArgsReplacing(components, field, "newValue", "it")).append(")\n");
                sb.append("    );\n\n");
            }
        }

        // Generate record/list/map nodes
        for (RecordComponent rc : components) {
            String field = rc.getName();
            Type t = rc.getGenericType();
            if (isRecordType(rc.getType())) {
                emitRecordNode(sb, root, components, field, rc.getType());
            } else if (isListOfRecord(t)) {
                Class<?> elem = (Class<?>) ((ParameterizedType) t).getActualTypeArguments()[0];
                emitListNode(sb, root, components, field, elem);
            } else if (isMapWithRecordValue(t)) {
                ParameterizedType pt = (ParameterizedType) t;
                Class<?> key = (Class<?>) pt.getActualTypeArguments()[0];
                Class<?> val = (Class<?>) pt.getActualTypeArguments()[1];
                emitMapNode(sb, root, components, field, key, val);
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static void emitRecordNode(StringBuilder sb, Class<?> root, List<RecordComponent> rootComps, String field, Class<?> recType) {
        String rootName = simpleName(root);
        String nodeClass = upper(field) + "Node";
        sb.append("    public static final class ").append(nodeClass).append(" {\n");
        sb.append("        private final Lens<").append(rootName).append(", ").append(simpleName(recType)).append("> lens = Lens.of(\n");
        sb.append("                ").append(rootName).append("::").append(field).append(",\n");
        sb.append("                (it, newValue) -> new ").append(rootName).append("(")
          .append(constructorArgsReplacing(rootComps, field, "newValue", "it")).append(")\n");
        sb.append("        );\n\n");
        sb.append("        public ").append(simpleName(recType)).append(" get(").append(rootName).append(" it) { return lens.get(it); }\n\n");
        sb.append("        public ").append(rootName).append(" set(").append(rootName).append(" it, ")
          .append(simpleName(recType)).append(" newValue) { return lens.set(it, newValue); }\n\n");

        // sub-record components as lenses
        for (RecordComponent sub : recType.getRecordComponents()) {
            String sf = sub.getName();
            Type st = sub.getGenericType();
            if (isRecordType(sub.getType())) {
                // inline lenses for nested record fields as simple inline "of"
                // but here sub is record -> generate composed lenses to its simple fields
                // We'll generate direct field-level lenses for its components (not deeper recursing here)
            }
            sb.append("        public final Lens").append("<").append(rootName).append(", ")
              .append(typeString(st)).append("> ").append(sf).append(" = lens.andThen(\n");
            sb.append("                Lens.of(\n");
            sb.append("                        ").append(simpleName(recType)).append("::").append(sf).append(",\n");
            sb.append("                        (addr, newValue) -> new ").append(simpleName(recType)).append("(")
              .append(constructorArgsReplacing(Arrays.asList(recType.getRecordComponents()), sf, "newValue", "addr")).append(")\n");
            sb.append("                )\n");
            sb.append("        );\n\n");
        }
        sb.append("    }\n\n");
    }

    private static void emitListNode(StringBuilder sb, Class<?> root, List<RecordComponent> rootComps, String field, Class<?> elemType) {
        String rootName = simpleName(root);
        String nodeClass = upper(field) + "Node";
        sb.append("    public static final class ").append(nodeClass).append(" {\n");
        sb.append("        private final Lens<").append(rootName).append(", java.util.List<")
          .append(simpleName(elemType)).append(">> lens = Lens.of(\n");
        sb.append("                ").append(rootName).append("::").append(field).append(",\n");
        sb.append("                (it, newValue) -> new ").append(rootName).append("(")
          .append(constructorArgsReplacing(rootComps, field, "newValue", "it")).append(")\n");
        sb.append("        );\n\n");
        sb.append("        public java.util.List").append("<").append(simpleName(elemType)).append("> get(").append(rootName).append(" it) { return lens.get(it); }\n\n");
        sb.append("        public ").append(rootName).append(" set(").append(rootName).append(" it, java.util.List").append("<")
          .append(simpleName(elemType)).append("> newValue) { return lens.set(it, newValue); }\n\n");
        sb.append("        public ElementNode at(int index) { return new ElementNode(index); }\n");
        sb.append("        public ElementNode get(int index) { return at(index); }\n\n");
        sb.append("        public Lens<").append(rootName).append(", ").append(simpleName(elemType)).append("> lensAt(int index) { return lens.andThen(ListLens.index(index)); }\n\n");
        sb.append("        public final class ElementNode {\n");
        sb.append("            private final Lens<").append(rootName).append(", ").append(simpleName(elemType)).append("> lens;\n");
        if (elemType.isRecord()) {
            RecordComponent[] ecs = elemType.getRecordComponents();
            for (RecordComponent ec : ecs) {
                String sf = ec.getName();
                Type st = ec.getGenericType();
                sb.append("            public final Lens<").append(rootName).append(", ")
                  .append(typeString(st)).append("> ").append(sf).append(";\n");
            }
            sb.append("            public ElementNode(int index) { this.lens = lensAt(index);\n");
            for (RecordComponent ec : ecs) {
                String sf = ec.getName();
                Type st = ec.getGenericType();
                sb.append("                this.").append(sf).append(" = this.lens.andThen(\n");
                sb.append("                        Lens.of(\n");
                sb.append("                                ").append(simpleName(elemType)).append("::").append(sf).append(",\n");
                sb.append("                                (it2, newValue) -> new ").append(simpleName(elemType)).append("(")
                  .append(constructorArgsReplacing(Arrays.asList(elemType.getRecordComponents()), sf, "newValue", "it2")).append(")\n");
                sb.append("                        )\n");
                sb.append("                );\n");
            }
            sb.append("            }\n\n");
        } else {
            sb.append("            public ElementNode(int index) { this.lens = lensAt(index); }\n\n");
        }
        sb.append("        }\n");
        sb.append("    }\n\n");
    }

    private static void emitMapNode(StringBuilder sb, Class<?> root, List<RecordComponent> rootComps, String field, Class<?> keyType, Class<?> valType) {
        String rootName = simpleName(root);
        String nodeClass = upper(field) + "Node";
        sb.append("    public static final class ").append(nodeClass).append(" {\n");
        sb.append("        private final Lens<").append(rootName).append(", java.util.Map<")
          .append(simpleName(keyType)).append(", ").append(simpleName(valType)).append(">> lens = Lens.of(\n");
        sb.append("                ").append(rootName).append("::").append(field).append(",\n");
        sb.append("                (it, newValue) -> new ").append(rootName).append("(")
          .append(constructorArgsReplacing(rootComps, field, "newValue", "it")).append(")\n");
        sb.append("        );\n\n");
        sb.append("        public java.util.Map").append("<").append(simpleName(keyType)).append(", ").append(simpleName(valType)).append("> get(")
          .append(rootName).append(" it) { return lens.get(it); }\n\n");
        sb.append("        public ").append(rootName).append(" set(").append(rootName).append(" it, java.util.Map").append("<")
          .append(simpleName(keyType)).append(", ").append(simpleName(valType)).append("> newValue) { return lens.set(it, newValue); }\n\n");
        sb.append("        public ElementNode at(").append(simpleName(keyType)).append(" key) { return new ElementNode(key); }\n");
        sb.append("        public ElementNode get(").append(simpleName(keyType)).append(" key) { return at(key); }\n\n");
        sb.append("        public Lens<").append(rootName).append(", ").append(simpleName(valType)).append("> lensAt(")
          .append(simpleName(keyType)).append(" key) { return lens.andThen(MapLens.key(key)); }\n\n");
        sb.append("        public final class ElementNode {\n");
        sb.append("            private final Lens<").append(rootName).append(", ").append(simpleName(valType)).append("> lens;\n");
        if (valType.isRecord()) {
            RecordComponent[] ecs = valType.getRecordComponents();
            for (RecordComponent ec : ecs) {
                String sf = ec.getName();
                Type st = ec.getGenericType();
                sb.append("            public final Lens<").append(rootName).append(", ")
                  .append(typeString(st)).append("> ").append(sf).append(";\n");
            }
            sb.append("            public ElementNode(").append(simpleName(keyType)).append(" key) { this.lens = lensAt(key);\n");
            for (RecordComponent ec : ecs) {
                String sf = ec.getName();
                sb.append("                this.").append(sf).append(" = this.lens.andThen(\n");
                sb.append("                        Lens.of(\n");
                sb.append("                                ").append(simpleName(valType)).append("::").append(sf).append(",\n");
                sb.append("                                (it2, newValue) -> new ").append(simpleName(valType)).append("(")
                  .append(constructorArgsReplacing(Arrays.asList(valType.getRecordComponents()), sf, "newValue", "it2")).append(")\n");
                sb.append("                        )\n");
                sb.append("                );\n");
            }
            sb.append("            }\n\n");
        } else {
            sb.append("            public ElementNode(").append(simpleName(keyType)).append(" key) { this.lens = lensAt(key); }\n\n");
        }
        sb.append("        }\n");
        sb.append("    }\n\n");
    }

    private static boolean isRecordType(Class<?> c) { return c.isRecord(); }

    private static boolean isListOfRecord(Type t) {
        if (!(t instanceof ParameterizedType pt)) return false;
        if (!(pt.getRawType() instanceof Class<?> raw)) return false;
        if (!List.class.isAssignableFrom(raw)) return false;
        Type arg = pt.getActualTypeArguments()[0];
        return (arg instanceof Class<?> c) && c.isRecord();
    }

    private static boolean isMapWithRecordValue(Type t) {
        if (!(t instanceof ParameterizedType pt)) return false;
        if (!(pt.getRawType() instanceof Class<?> raw)) return false;
        if (!Map.class.isAssignableFrom(raw)) return false;
        Type val = pt.getActualTypeArguments()[1];
        return (val instanceof Class<?> c) && c.isRecord();
    }

    private static String typeString(Type t) {
        if (t instanceof Class<?> c) {
            return simpleName(c);
        }
        if (t instanceof ParameterizedType pt) {
            String raw = simpleName((Class<?>) pt.getRawType());
            StringBuilder sb = new StringBuilder(raw).append("<");
            Type[] args = pt.getActualTypeArguments();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(typeString(args[i]));
            }
            sb.append(">");
            return sb.toString();
        }
        return t.getTypeName();
    }

    private static String constructorArgsReplacing(List<RecordComponent> components, String replaceField, String replacement, String ctx) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < components.size(); i++) {
            RecordComponent rc = components.get(i);
            if (i > 0) sb.append(", ");
            if (rc.getName().equals(replaceField)) {
                sb.append(replacement);
            } else {
                sb.append(ctx).append(".").append(rc.getName()).append("()");
            }
        }
        return sb.toString();
    }

    private static String simpleName(Class<?> c) { return c.getSimpleName(); }
    private static String upper(String s) { return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1); }
}


