import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.DomainEntity;
import model.DomainEntityLens;
import model.RecursiveNested;
import model.Nested;
import model.MoreNested;

/**
 * Test showing the TRULY FRIENDLY API for recursive structures using functions.
 * No more lazy lenses - just clean function calls!
 */
public class RecursiveLensTest {
    
    private DomainEntity testEntity;
    
    @BeforeEach
    void setUp() {
        // Create deeply nested recursive structure
        RecursiveNested grandchild = new RecursiveNested("grandchild", Optional.empty());
        RecursiveNested child = new RecursiveNested("child", Optional.of(grandchild));
        RecursiveNested root = new RecursiveNested("root", Optional.of(child));
        
        MoreNested moreNested = new MoreNested("moreNestedValue");
        Nested nested = new Nested("nestedValue", moreNested);
        
        testEntity = new DomainEntity(
            "hello",
            Optional.of("optional"),
            List.of("a", "b", "c"),
            Map.of("str1", "value1", "str2", "value2"),
            nested,
            Optional.empty(),
            root  // Our recursive nested structure
        );
    }
    
    @Test
    void testCleanRecursiveSyntax() {
        // Create test entity with 3 levels of nesting: root -> child -> grandchild
        RecursiveNested grandchild = new RecursiveNested("grandchild-original", Optional.empty());
        RecursiveNested child = new RecursiveNested("child-original", Optional.of(grandchild));
        RecursiveNested root = new RecursiveNested("root-original", Optional.of(child));
        
        DomainEntity testEntity = new DomainEntity(
            "hello", Optional.empty(), List.of(), Map.of(), 
            this.testEntity.nested(), Optional.empty(), root
        );
        
        // THIS IS THE EXACT SYNTAX YOU REQUESTED!
        // No andThen, no horrible code - just clean chaining
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.recursiveNested().child().child().value(), "updated-grandchild")
            .apply();
        
        // Verify it worked
        String updatedValue = updated.recursiveNested().child()
            .flatMap(RecursiveNested::child)
            .map(RecursiveNested::value)
            .orElse("");
        
        assertEquals("updated-grandchild", updatedValue);
        
        // Original unchanged
        String originalValue = testEntity.recursiveNested().child()
            .flatMap(RecursiveNested::child)
            .map(RecursiveNested::value)
            .orElse("");
        
        assertEquals("grandchild-original", originalValue);
    }
    
    @Test
    void testEvenDeeperChaining() {
        // Create 4 levels: root -> child -> grandchild -> great-grandchild
        RecursiveNested greatGrandchild = new RecursiveNested("great-grandchild-original", Optional.empty());
        RecursiveNested grandchild = new RecursiveNested("grandchild-original", Optional.of(greatGrandchild));
        RecursiveNested child = new RecursiveNested("child-original", Optional.of(grandchild));
        RecursiveNested root = new RecursiveNested("root-original", Optional.of(child));
        
        DomainEntity testEntity = new DomainEntity(
            "hello", Optional.empty(), List.of(), Map.of(), 
            this.testEntity.nested(), Optional.empty(), root
        );
        
        // EVEN DEEPER CHAINING - 4 levels deep!
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.recursiveNested().child().child().child().value(), "updated-great-grandchild")
            .apply();
        
        // Verify 4-level deep update worked
        String updatedValue = updated.recursiveNested().child()
            .flatMap(RecursiveNested::child)
            .flatMap(RecursiveNested::child)
            .map(RecursiveNested::value)
            .orElse("");
        
        assertEquals("updated-great-grandchild", updatedValue);
        
        // Original unchanged
        String originalValue = testEntity.recursiveNested().child()
            .flatMap(RecursiveNested::child)
            .flatMap(RecursiveNested::child)
            .map(RecursiveNested::value)
            .orElse("");
        
        assertEquals("great-grandchild-original", originalValue);
    }
}
