package generator;

import java.nio.file.Paths;

/**
 * Runner class to generate code for the main DomainEntity record
 */
public class GenerateModelCode {
    
    public static void main(String[] args) {
        try {
            RecordCodeGenerator generator = new RecordCodeGenerator(
                "model", 
                Paths.get("target/generated-sources/model")
            );
            
            // Generate for the main DomainEntity record only
            // This will automatically discover and generate all nested records
            Class<?> domainEntityClass = Class.forName("model.DomainEntity");
            generator.generateForMainRecord(domainEntityClass);
            
            System.out.println("Code generation completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Code generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
