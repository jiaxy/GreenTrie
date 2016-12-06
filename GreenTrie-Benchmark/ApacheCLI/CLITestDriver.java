

import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLITestDriver {
    protected CommandLineParser parser;

    protected Options options;
    
    public void CLITestDriver(){
    	setUp();
    }
    
    public void setUp()
    {
        options = new Options()
            .addOption("a", "enable-a", false, "turn [a] on or off")
            .addOption("b", "bfile", true, "set the value of [b]")
            .addOption("c", "copt", false, "turn [c] on or off");
    }
    
    
	public static void main( String[] args ) throws Exception{
		CLITestDriver testdriver = new CLITestDriver();
		String[] opts = new String[] { "-a",
                "-b", "toast",
                "foo", "bar" };
		testdriver.testSimpleShort(opts);
		
	}
	

	    public void testSimpleShort(String[] opts) throws Exception
	    {
	        CommandLine cl = parser.parse(options, opts);

	        assert(cl.hasOption("a"));
	        assert(cl.hasOption("b"));
	        assert( cl.getOptionValue("b").equals("toast"));
	        assert(cl.getArgList().size() == 2);
	    }

		
	 

}
