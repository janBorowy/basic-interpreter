import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.example.InterpretCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class InterpreterAcceptanceTest {

    private final static String TEST_PROGRAMS_DIR = "test_programs";
    private final static String DEMO_PROGRAMS_DIR = "demo_programs";

    private CommandLine commandLine;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    public InterpreterAcceptanceTest() {
    }

    @BeforeEach
    public void prepareCommandLineApp() {
        var app = new InterpretCommand();
        this.commandLine = new CommandLine(app);
    }

    @BeforeEach
    public void prepareStreams() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private String getFileParameter(String scriptName) {
        return "-f%s/%s.stud".formatted(TEST_PROGRAMS_DIR, scriptName);
    }

    private String getDemoFileParameter(String scriptName) {
        return "-f%s/%s.stud".formatted(DEMO_PROGRAMS_DIR, scriptName);
    }

    @Test
    void add() {
        commandLine.execute(getFileParameter("add"), "-m=add", "-p=1,1");
        assertEquals("Program returned: integer(2)\n", out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void wrongParam() {
        commandLine.execute(getFileParameter("add"), "-m=add", "-p=1.0,1");
        assertEquals("", out.toString());
        assertEquals("Semantic error at line 0, col 0: Expected int value, but was given float(1)\n", err.toString());
    }

    @Test
    void relationalOperations() {
        commandLine.execute(getFileParameter("relationalOperations"), "-m=main");
        assertEquals("""
                one equals one
                one is greater than zero
                two is greater or equal to one
                three is greater or equal to three
                four is lower or equal to five

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void wrongStructure() {
        commandLine.execute(getFileParameter("wrongStructure"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Semantic error at line 28, col 21: Value(Circle(radius: float(0.5))) does not match variable type(MeasurementUnit)\n",
                err.toString());
    }

    @Test
    void userTypeDoesNotExist() {
        commandLine.execute(getFileParameter("userTypeDoesNotExist"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Semantic error at line 2, col 16: Function \"Square\" does not exist\n", err.toString());
    }

    @Test
    void structureBelongsToMultipleVariants() {
        commandLine.execute(getFileParameter("structureBelongsToMultipleVariants"), "-m=main");
        assertEquals("""
                Jacek is an author and is known for historyjka
                Luki is a musician and plays klarnet

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void defaultBranch() {
        commandLine.execute(getFileParameter("defaultBranch"), "-m=main");
        assertEquals("""
                Jacek is an author and is known for historyjka
                Luki is a musician and plays klarnet
                Some person

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void clashingVariantStructureNames() {
        commandLine.execute(getFileParameter("clashingVariantStructureNames"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Syntax error at line 13, col 2: Multiple functions with same id found: Rectangle\n", err.toString());
    }

    @Test
    void clashingFunctionStructureNames() {
        commandLine.execute(getFileParameter("clashingFunctionStructureNames"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Syntax error at line 5, col 15: Multiple functions with same id found: main\n", err.toString());
    }

    @Test
    void branchVariableWithClashingName() {
        commandLine.execute(getFileParameter("branchVariableWithClashingName"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Semantic error at line 26, col 5: a was already initialized\n", err.toString());
    }

    @Test
    void branchContainsStructureThatDoesNotBelongToVariant() {
        commandLine.execute(getFileParameter("branchContainsStructureThatDoesNotBelongToVariant"), "-m=main");
        // I don't think this should throw an error, it's not fatal.
        assertEquals("""
                Jacek is an author and is known for historyjka
                Luki is a musician and plays klarnet

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void invalidStructureArguments() {
        commandLine.execute(getFileParameter("invalidStructureArguments"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Semantic error at line 0, col 0: Expected float value, but was given string(abc)\n", err.toString());
    }

    @Test
    void returnInLoop() {
        commandLine.execute(getFileParameter("returnInLoop"), "-m=main");
        assertEquals("""
                Should be 4: 4
                Should be 5: 5

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void returnTest() {
        commandLine.execute(getFileParameter("return"), "-m=main");
        assertEquals("""
                Hello world!

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void doubleNestedFunctionReturn() {
        commandLine.execute(getFileParameter("doubleNestedFunctionReturn"), "-m=main");
        assertEquals("""
                should print this

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void divideByZero() {
        commandLine.execute(getFileParameter("divideByZero"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Semantic error at line 2, col 11: Division by zero is forbidden\n", err.toString());
    }

    @Test
    void divideByZeroFloat() {
        commandLine.execute(getFileParameter("divideByZeroFloat"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("Semantic error at line 2, col 12: Division by zero is forbidden\n", err.toString());
    }

    @Test
    void printStructure() {
        commandLine.execute(getFileParameter("printStructure"), "-m=main");
        assertEquals("""
                Author(knownFor: string(historyjka), name: string(Jacek))
                Musician(name: string(Luki), instrument: string(klarnet))

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void voidReturnInExpression() {
        commandLine.execute(getFileParameter("voidReturnInExpression"), "-m=main");
        assertEquals("""
                Hello World!

                """, out.toString());
        assertEquals("Semantic error at line 6, col 17: Function returns void, but value is expected\n", err.toString());
    }

    @Test
    void structureInitializedWithVarVariable() {
        commandLine.execute(getFileParameter("structureInitializedWithVarVariable"), "-m=main");
        assertEquals("""
                Author(knownFor: string(historyjka), name: string(Jacek))
                Author(knownFor: string(historyjka), name: string(Jacek))

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void stringToIntFloatCast() {
        commandLine.execute(getFileParameter("stringToIntFloatCast"), "-m=main");
        assertEquals("""
                6
                3

                """, out.toString());
        assertEquals("Semantic error at line 9, col 26: Number format error for input string: \"5.5\"\n", err.toString());
    }

    @Test
    void sumAndIODemo() {
        commandLine.execute(getDemoFileParameter("sumAndIO"), "-m=main");
        assertEquals("4\n\n", out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void conditionalsDemo() {
        commandLine.execute(getDemoFileParameter("conditionals"), "-m=main");
        assertEquals("""
                a variable's value is even

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void mutabilityDemo() {
        commandLine.execute(getDemoFileParameter("mutability"), "-m=main");
        assertEquals("", out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void whileLoopDemo() {
        commandLine.execute(getDemoFileParameter("whileLoop"), "-m=main");
        assertEquals("""
                0
                1
                2
                3
                4
                5
                6
                7
                8
                9

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void structuresDemo() {
        commandLine.execute(getDemoFileParameter("structures"), "-m=main");
        assertEquals("1.5\n", out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void variantDemo() {
        commandLine.execute(getDemoFileParameter("variant"), "-m=main");
        assertEquals("""
                Article with headline - Czy jutro jest niedziela handlowa?
                Book with title - Kroniki Jakuba Wędrowycza

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void functionsDemo() {
        commandLine.execute(getDemoFileParameter("functions"), "-m=main");
        assertEquals("""
                Float point: 3.5
                Int point: 6

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void variableHidingDemo() {
        commandLine.execute(getDemoFileParameter("variableHiding"), "-m=main");
        assertEquals("""
                3
                2

                """, out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void recursionDemo() {
        commandLine.execute(getDemoFileParameter("recursion"), "-m=getNthFibonacciNumber", "-p=6");
        assertEquals("Program returned: integer(8)\n", out.toString());
        assertEquals("", err.toString());
    }
}
