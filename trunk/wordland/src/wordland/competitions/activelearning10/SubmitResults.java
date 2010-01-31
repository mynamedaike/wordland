package wordland.competitions.activelearning10;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

/**
 * class to submit the results to this competition through the web
 * implemented as a singleton design pattern
 * uses apache httpclient 3.1
 */
public final class SubmitResults {
	private static SubmitResults instance = null;
	private HttpClient client=new HttpClient();
	private SubmitResults () {
		
	}
	public static SubmitResults getInstance() {
		if (instance == null) {
			instance = new SubmitResults();
		}
		return instance;
	}
	public boolean login(String username, String password) {
		PostMethod post= new PostMethod("http://www.causality.inf.ethz.ch/activelearning.php");
		boolean ret = false;
		if (username==null) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(Params.passwfile));
				username=in.readLine();
				password=in.readLine();
				in.close();
			}
			catch (IOException e) {
				return false;
			}
		}
		NameValuePair [] data = {
			new NameValuePair("text",username),
			new NameValuePair("password", password)
		};
		post.setRequestBody(data);
		try {
			int statuscode = client.executeMethod(post);
			if (statuscode != HttpStatus.SC_OK) {
				 System.err.println("Method failed: " + post.getStatusLine());
			}
			//String resp=post.getResponseBodyAsString();
			//System.out.println(resp);
			Cookie [] cookies = client.getState().getCookies();
			for (Cookie c : cookies) {
				System.out.println(c.getName()+"  =  "+c.getValue());
			}
			ret=true;
		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			post.releaseConnection();
		}
		return ret;
	}
	public boolean submitResults(String dataname,String expname,int [] sample,double [] predict) {
		boolean ret=false;
		try {
			int i;
			FileOutputStream file=new FileOutputStream("sent.zip");
			ZipOutputStream out=new ZipOutputStream(new BufferedOutputStream(file));
			ZipEntry entry=new ZipEntry(dataname+".sample");
			out.putNextEntry(entry);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintWriter bosw=new PrintWriter(bos);
			for (i=0;i<sample.length;i++) {
				bosw.println(sample[i]);
			}
			bosw.close();
			out.write(bos.toByteArray());
			bos.close();
			
			entry=new ZipEntry(dataname+".predict");
			out.putNextEntry(entry);
			bos=new ByteArrayOutputStream();
			bosw=new PrintWriter(bos);
			for (i=0;i<predict.length;i++) {
				bosw.println(predict[i]);
			}
			bosw.close();
			out.write(bos.toByteArray());
			bos.close();
			out.close();
			
			MultipartPostMethod post= new MultipartPostMethod("http://www.causality.inf.ethz.ch/ajax/workbench.php");
			post.addParameter("action", "newExperiment");
			post.addParameter("id_user", "240");
			post.addParameter("model", dataname);
			post.addParameter("experiment", expname);
			post.addParameter("FILE1", new File("sent.zip"));
			
			int statuscode = client.executeMethod(post);
			if (statuscode != HttpStatus.SC_OK) {
				 System.err.println("Method failed: " + post.getStatusLine());
			}
			
			String postret=post.getResponseBodyAsString();
			if (postret!=null) {
				if (Integer.parseInt(postret)==1) {
					ret=true;
				}
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return ret;
	}
}
