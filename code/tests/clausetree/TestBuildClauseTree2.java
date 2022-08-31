package clausetree;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.*;
import edu.stanford.nlp.util.CoreMap;
import parsing.DocumentationParser;
import parsing.NLPSentenceParser;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import clausetree.ClauseTreeNode;

public class TestBuildClauseTree2 {
    @Rule
    public TestName name = new TestName();
    
    @BeforeClass
    public static void oneTimeSetUp() {
    	Utils.encloseClauseWithParentheses = true;
    }
    
    
    @Ignore
    @Test 
    public void test1() throws Exception {
    	String text = "if the port parameter is outside the specified range of valid port values, which is between 0 and 65535, inclusive.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    
    
    @Test 
    public void test2() throws Exception {
    	String text = "if there is an error in the underlying protocol, such as a TCP error.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( there be a error in the underlie protocol ).", clause);
    }
    
    @Test 
    public void test3() throws Exception {
    	String text = "if an error occurs enabling or disabling the <link> SO_REUSEADDR </link> socket option, or the socket is closed.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a error occur enable the SO_REUSEADDR socket option ) or ( a error occur disable the SO_REUSEADDR socket option ) or ( the socket be close ).", clause);
    }
    
    @Test 
    public void test4() throws Exception {
    	String text = "if the<code>  beginIndex</code>  is negative, or <code>  endIndex</code> is larger than the length of this sequence, or<code>  beginIndex</code>  is larger than <code>  endIndex</code> .";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the beginIndex be negative ) or ( endIndex be larger than the length of this sequence ) or ( beginIndex be larger than endIndex ).", clause);
    }
    
    @Ignore
    @Test 
    public void test5() throws Exception {
    	String text = "if <code>  index</code> is negative or larger then the length of this sequence, or if <code>  codePointOffset</code>  is positive and the subsequence starting with <code>  index</code>  has fewer than<code>  codePointOffset</code>  code points, or if <code>  codePointOffset</code>  is negative and the subsequence before <code>  index</code>  has fewer than the absolute value of<code>  codePointOffset</code>  code points.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    
    @Ignore
    @Test 
    public void test6() throws Exception {
    	String text = "if the specified offset is less than the first text boundary or greater than the last text boundary.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    
    @Ignore
    @Test 
    public void test7() throws Exception {
    	String text = "on encoding errors, or if this parameter object has not been initialized.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    
    @Test 
    public void test8() throws Exception {
    	//should be an I/O
    	String text = "if this input stream has been closed by invoking its <link> close()</link>  method, or an I/O error occurs.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( this input stream have be close by invoke its close() method ) or ( a I/O error occur ).", clause);
    }
    
    @Test 
    public void test9() throws Exception {
    	//should be an I/O
    	String text = "if an I/O error occurs when creating the input stream, the socket is closed, the socket is not connected, or the socket input has been shutdown using <link> shutdownInput()</link>";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a I/O error occur create the input stream ) or ( the socket be close ) or ( the socket be not connect ) or ( the socket input have be shutdown use shutdownInput() ).", clause);
    }
    
    @Test 
    public void test10() throws Exception {
    	//semantic seems to not correct
    	String text = "if an I/O error occurs when creating the output stream or if the socket is not connected.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a I/O error occur create the output stream ) or ( the socket be not connect ).", clause);
    }
    
    @Test 
    public void test11() throws Exception {
    	String text = "if an error occurs enabling or disabling the <link> SO_REUSEADDR </link> socket option, or the socket is closed.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a error occur enable the SO_REUSEADDR socket option ) or ( a error occur disable the SO_REUSEADDR socket option ) or ( the socket be close ).", clause);
    }
    
    @Test 
    public void test12() throws Exception {
    	String text = "if the engine is not initialized properly, if this signature algorithm is unable to process the input data provided, or if <code>  len</code>  is less than the actual signature length.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the engine be not initialize properly ) or ( this signature algorithm be unable to process the input datum provide ) or ( len be less than the actual signature length ).", clause);
    }
    
    @Test 
    public void test13() throws Exception {
    	String text = "If the new strength value is not one of PRIMARY, SECONDARY, TERTIARY or IDENTICAL.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the new strength value be not PRIMARY ) and ( the new strength value be not SECONDARY ) and ( the new strength value be not TERTIARY ) and ( the new strength value be not IDENTICAL ).", clause);
    }
    
    @Test 
    public void test14() throws Exception {
    	//null be not
    	String text = "if the coordinates are not in bounds, or if <code>obj</code> is not <code>null</code> or not large enough to hold the pixel data";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the coordinate be not in bound ) or ( obj be not null ) or ( obj be not not large enough to hold the pixel datum ).", clause);
    }
    
    @Test 
    public void test15() throws Exception {
    	//together
    	String text = "if beginIndex is less then 0, endIndex is greater than the length of the string, or beginIndex and endIndex together don't define a non-empty subrange of the string.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( beginIndex be less then 0 ) or ( endIndex be greater than the length of the string ) or ( beginIndex and endIndex together do not define a non-empty subrange of the string ).", clause);
    }
    
    @Ignore
    @Test 
    public void test16() throws Exception {
    	//line 144 (test 15) vs 146: 2 conflicts tree
    	String text = "if beginIndex is less then 0, endIndex is greater than the length of the string, or beginIndex and endIndex together don't define a non-empty subrange of the string and the attributes parameter is not an empty Map.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( beginindex be less then 0 ) or ( endindex be greater than the length of the string ) or ( beginindex and endindex together do not define a non-empty subrange of the string ) and ( the attribute parameter be not a empty map ).", clause);
    }
    
    @Test 
    public void test17() throws Exception {
    	//tag problem
    	String text = "if the mode argument is not equal to one of <tt>\"r\"</tt>, <tt>\"rw\"</tt>, <tt>\"rws\"</tt>, or <tt>\"rwd\"</tt>.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the mode argument be not equal to r ) and ( the mode argument be not equal to rw ) and ( the mode argument be not equal to rws ) and ( the mode argument be not equal to rwd ).", clause);
    }
    
    @Ignore
    @Test 
    public void test18() throws Exception {
    	String text = "if the mode is <tt>\"r\"</tt> but the given file object does not denote an existing regular file, or if the mode begins with <tt>\"rw\"</tt> but the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( some other error occur while open the file ) or ( some other error occur while create the file ).", clause);
    }
    
    @Test 
    public void test181() throws Exception {
    	String text = "some error occurs while opening or creating the file";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( some error occur while open the file ) or ( some error occur while create the file ).", clause);
    }

    @Ignore
    @Test 
    public void test19() throws Exception {
    	//148
    	String text = "if a security manager exists and its<code>  checkRead</code>  method denies read access to the file or the mode is \"rw\" and the security manager's<code>  checkWrite</code>  method denies write access to the file";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    
    @Test 
    public void test20() throws Exception {
    	//149
    	String text = "if <code>  pos</code>  is less than<code>  0</code>, pos is too large  or if an I/O error occurs.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( pos be less than 0 ) or ( pos be too large ) or ( a I/O error occur ).", clause);
    }
    
    @Test 
    public void test201() throws Exception {
    	//149
    	String text = "if and only if an I/O error occurs.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a I/O error occur ).", clause);
    }
    
    @Ignore
    @Test 
    public void test21() throws Exception {
    	String text = "if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    
    @Test 
    public void test22() throws Exception {
    	String text = "if <code>listenerType</code> doesn't specify a class or interface that implements <code>java.util.EventListener</code>";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( listenerType do not specify a class that implement EventListener ) or ( listenerType do not specify interface that implement EventListener ).", clause);
    }

    @Test 
    public void test23() throws Exception {
    	String text = "if <code>listenerType</code> doesn't specify a class or interface that implements <code>java.util.EventListener</code>";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( listenerType do not specify a class that implement EventListener ) or ( listenerType do not specify interface that implement EventListener ).", clause);
    }

    @Ignore
    @Test 
    public void test24() throws Exception {
    	String text = "If character encoding needs to be consulted, but named character encoding is not supported";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    

    @Ignore
    @Test 
    public void test25() throws Exception {
    	String text = "if the pipe is <a href=#BROKEN> broken</a>,<link> connect()  unconnected</link> , closed, or if an I/O error occurs.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    

    @Ignore
    @Test 
    public void test26() throws Exception {
    	String text = "if the number of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type by a method invocation conversion; if this constructor pertains to an enum type.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }

    @Ignore
    @Test 
    public void test27() throws Exception {
    	String text = "if the specified permission is not permitted, based on the current security policy and the context encapsulated by this object.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }

    @Ignore
    @Test 
    public void test28() throws Exception {
    	String text = "if the number and types of bands in the <code>SampleModel</code> of the <code>Raster</code> do not match the number and types required by the <code>ColorModel</code> to represent its color and alpha components.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }    

    @Ignore
    @Test 
    public void test29() throws Exception {
    	String text = "if <code>src</code> is longer than <code>dst</code> or if for any element <code>i</code> of <code>src</code>, <code>src[i]-offset</code> is either less than zero or greater than or equal to the length of the lookup table for any band.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }    

    @Ignore
    @Test 
    public void test30() throws Exception {
    	String text = "  if <code>src</code> is longer than <code>dst</code> or if for any element <code>i</code> of <code>src</code>,<code>  (src[i]&0xff)-offset</code>  is either less than zero or greater than or equal to the length of the lookup table for any band.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }    

    @Ignore
    @Test 
    public void test31() throws Exception {
    	String text = "if the month, day, dayOfWeek, time more, or time parameters are out of range for the start or end rule, or if a time mode value is invalid.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }    

    @Ignore
    @Test 
    public void test32() throws Exception {
    	String text = "if the file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading.";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }    

    @Ignore
    @Test 
    public void test33() throws Exception {
    	String text = "if <code>bundleName</code>, <code>locale</code>, <code>format</code>, or <code>loader</code> is <code>null</code>, or if <code>null</code> is returned by<link> toBundleName()  toBundleName</link>";
		ArrayList<String> paraNames = new ArrayList<>();
		
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }

    @Test 
    public void test34() throws Exception {
    	String text = "if <code>baseName</code>, <code>locale</code>, <code>format</code>, <code>loader</code>, or <code>bundle</code> is <code>null</code>";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( baseName be null ) or ( locale be null ) or ( format be null ) or ( loader be null ) or ( bundle be null ).", clause);
    }    

    @Ignore
    @Test 
    public void test35() throws Exception {
    	String text = "if the pipe is <a href=PipedOutputStream.html#BROKEN> <code>broken</code></a>,<link> connect()  unconnected</link> , closed or an I/O error occurs.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }       

    @Ignore
    @Test 
    public void test36() throws Exception {
    	String text = "if the pipe is <a href=PipedOutputStream.html#BROKEN> <code>broken</code></a>,<link> connect()  unconnected</link> , closed or an I/O error occurs.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }       

    @Ignore
    @Test 
    public void test37() throws Exception {
    	String text = "if the pipe is <a href=PipedOutputStream.html#BROKEN> <code>broken</code></a>,<link> connect()  unconnected</link> , closed or an I/O error occurs.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }       

    @Ignore
    @Test 
    public void test38() throws Exception {
    	String text = "if<code>  m</code> is not positive, or the length of <code>  ks</code> is neither 1 nor 3, or values in <code>  ks</code> are not between <code>  m</code> -1 and 1 (inclusive) and in descending order.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }

    @Test 
    public void test39() throws Exception {
    	String text = "if the specified security context is not an instance of <code>AccessControlContext</code> (e.g., is <code>null</code>), or is denied access to the resource specified by the given permission.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( the specify security context be not a instance of AccessControlContext ) or ( the specify security context be deny access to the resource specify by the give permission ).", clause);
    }

    @Ignore
    @Test 
    public void test40() throws Exception {
    	String text = "if the calling thread does not have permission to open a socket connection to the specified <code>host</code> and <code>port</code>.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( the call thread do not have permission to open a socket connection to the specify host ) and ( the call thread do not have permission to open a socket connection to the specify port ).", clause);
    }

    @Test 
    public void test41() throws Exception {
    	String text = "if the specified security context is not an instance of <code>AccessControlContext</code> (e.g., is <code>null</code>), or does not have permission to open a socket connection to the specified <code>host</code> and <code>port</code>.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( the specify security context be not a instance of AccessControlContext ) or ( the specify security context do not have permission to open a socket connection to the specify host ) and ( the specify security context do not have permission to open a socket connection to port ).", clause);
    }

    @Ignore
    @Test 
    public void test42() throws Exception {
    	String text = "if the Type/format combination is unrecognized, if the algorithm, key format, or encoded key bytes are unrecognized/invalid, of if the resolution of the key fails for any reason";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }        

    @Ignore
    @Test 
    public void test43() throws Exception {
    	//only first sentence
    	String text = "  if there is an I/O or format problem with the keystore data. If the error is due to an incorrect<code>  ProtectionParameter</code>  (e.g. wrong password) the <link> getCause  cause</link>  of the<code>  IOException</code>  should be an<code>  UnrecoverableKeyException</code>";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }        

    @Test 
    public void test44() throws Exception {
    	String text = "  if<code>  rule</code>  is not either<link> WIND_EVEN_ODD</link>  or<link> WIND_NON_ZERO</link>";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( rule be not WIND_EVEN_ODD ) or ( rule be not WIND_NON_ZERO ).", clause);
    }     

    @Ignore
    @Test 
    public void test45() throws Exception {
    	String text = "if <code>  dstIndex</code> is negative or not less than <code>  dst.length</code> , or if<code>  dst</code>  at <code>  dstIndex</code>  doesn't have enough array element(s) to store the resulting <code>  char</code> value(s). (If <code>  dstIndex</code>  is equal to<code>  dst.length-1</code>  and the specified<code>  codePoint</code>  is a supplementary character, the high-surrogate value is not stored in<code>  dst[dstIndex]</code> .)";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }        

    @Ignore
    @Test 
    public void test47() throws Exception {
    	String text = "  if <code>  index</code> is negative or larger then the length of the char sequence, or if <code>  codePointOffset</code>  is positive and the subsequence starting with <code>  index</code>  has fewer than<code>  codePointOffset</code>  code points, or if<code>  codePointOffset</code>  is negative and the subsequence before <code>  index</code>  has fewer than the absolute value of <code>  codePointOffset</code>  code points.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }        

    @Ignore
    @Test 
    public void test48() throws Exception {
    	String text = "   if <code>  start</code>  or <code>  count</code>  is negative, or if <code>  start + count</code>  is larger than the length of the given array, or if <code>  index</code>  is less than <code>  start</code>  or larger then <code>  start + count</code> , or if <code>  codePointOffset</code>  is positive and the text range starting with <code>  index</code>  and ending with <code>  start + count - 1</code> has fewer than <code>  codePointOffset</code>  code points, or if <code>  codePointOffset</code>  is negative and the text range starting with <code>  start</code>  and ending with <code>  index - 1</code> has fewer than the absolute value of<code>  codePointOffset</code>  code points.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }        

    @Ignore
    @Test 
    public void test49() throws Exception {
    	String text = "if <code>src</code> is longer than <code>dst</code> or if for any element <code>i</code> of <code>src</code>,<code>  (src[i]&0xffff)-offset</code>  is either less than zero or greater than or equal to the length of the lookup table for any band.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }   
    

    @Test 
    public void test50() throws Exception {
    	String text = "if error occurs during group creation, if security manager is not set, or if the group has already been created and deactivated.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( error occur during group creation ) or ( security manager be not set ) or ( the group have already be create ) and ( the group have already be deactivate ).", clause);
    }   
    

    @Ignore
    @Test 
    public void test51() throws Exception {
    	String text = "if <code>obj</code> is not large enough to hold a pixel value for this <code>ColorModel</code> or the <code>components</code> array is not large enough to hold all of the color and alpha components starting at <code>offset</code>";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }   

    @Test 
    public void test52() throws Exception {
    	String text = "   if<code>  beginIndex</code>  is negative or larger than the length of this <code>  String</code>  object.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( beginIndex be negative ) or ( beginIndex be larger than the length of this String object ).", clause);
    }   

    @Ignore
    @Test 
    public void test53() throws Exception {
    	String text = "   if the capabilities supplied could not be supported or met; this may happen, for example, if there is not enough accelerated memory currently available, or if page flipping is specified but not possible.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }   

    @Ignore
    @Test 
    public void test54() throws Exception {
    	String text = "   if the capabilities supplied could not be supported or met; this may happen, for example, if there is not enough accelerated memory currently available, or if page flipping is specified but not possible.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( ", clause);
    }
    
    @Test 
    public void test55() throws Exception {
    	String text = "   if <code>element</code> or <code>localName</code> is <code>null</code>";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( element be null ) or ( localName be null ).", clause);
    }
    
    @Test 
    public void test56() throws Exception {
    	String text = "if <code>url</code> does not conform to the syntax for an RMI connector, or if its protocol is not recognized by this implementation. Only \"rmi\" and \"iiop\" are recognized when <var>rmiServerImpl</var> is null.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( url do not conform to the syntax for a RMI connector ) or ( its protocol be not recognize by this implementation ). ( Only rmi be recognize rmiServerImpl be null ) and ( iiop be recognize rmiServerImpl be null ).", clause);
    }
    
    @Test 
    public void test57() throws Exception {
    	String text = "if there is not exactly one child <code>SOAPElement</code> of the <code> <code>SOAPBody</code>.";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
    	Assert.assertEquals("( there be not exactly one child SOAPElement of the SOAPBody ).", clause);
    }
        
    @Ignore
    @Test
    public void test() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = ".";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("(  ).", clause);
    }

	public static void main(String[] args) throws IOException {
//		String text = "<code> <code>index</code> < 0 </code>. "
//				+ "'n' is negative. "
//				+ "if <code>number</code> is null or not an instance of <code>Number</code>.";
		String text = "if <code>x</code> is not a valid horizontal scrollbar policy, as listed above. ";
		HashMap<String, String> codeStr = new HashMap<>(), strCode = new HashMap<>();
		ArrayList<String> paraNames = new ArrayList<>();
		paraNames.add("limit");
		paraNames.add("count");
		paraNames.add("off");
		paraNames.add("len");
		paraNames.add("n");
		text = DocumentationParser.normalize(text, codeStr, strCode, paraNames );
		List<CoreMap> sentences = NLPSentenceParser.parse(text);
		for (CoreMap sentence : sentences) {
			Tree tree = sentence.get(TreeAnnotation.class);
			ClauseTreeNode clauseTree = NLPSentenceParser.buildClauseTree(tree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
			String flattenedSequence = clauseTree.flatten();
			if (!flattenedSequence.equals("null")) {
				System.out.println(sentence);
				System.out.println(tree);
				NLPSentenceParser.print(tree);
				clauseTree.print();
				System.out.println(flattenedSequence);
			}
		}
		Scanner scan=new Scanner(System.in);
		while (true) {
			text = scan.nextLine();
			sentences = NLPSentenceParser.parse(text);
			for (CoreMap sentence : sentences) {
				System.out.println(sentence);
				Tree tree = sentence.get(TreeAnnotation.class);
				System.out.println(tree);
				NLPSentenceParser.print(tree);
				ClauseTreeNode clauseTree = NLPSentenceParser.buildClauseTree(tree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
				clauseTree.print();
				String flattenedSequence = clauseTree.flatten();
				System.out.println(flattenedSequence);
			}
			if (text.isEmpty())
				break;
		}
		scan.close();
	}

	private String transform(String text, ArrayList<String> paraNames) {
		HashMap<String, String> codeStr = new HashMap<>(), strCode = new HashMap<>();
		text = DocumentationParser.normalize(text, codeStr, strCode, paraNames );
		StringBuilder sb = new StringBuilder();
		List<CoreMap> sentences = NLPSentenceParser.parse(text);
		for (CoreMap sentence : sentences) {
			Tree tree = sentence.get(TreeAnnotation.class);
			ClauseTreeNode clauseTree = NLPSentenceParser.buildClauseTree(tree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
			String flattenedSequence = clauseTree.flatten();
			if (!flattenedSequence.equals("null")) {
				System.out.println(sentence);
				System.out.println(tree);
				NLPSentenceParser.print(tree);
				clauseTree.print();
				System.out.println(flattenedSequence);
				sb.append(flattenedSequence + ". ");
			}
		}
		return sb.toString().trim();
	}

}