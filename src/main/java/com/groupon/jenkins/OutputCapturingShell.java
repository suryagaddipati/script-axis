package com.groupon.jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Messages;
import hudson.tasks.Shell;
import hudson.util.FormValidation;

public class OutputCapturingShell extends Shell {

	private String output;
	public OutputCapturingShell(String command) {
		super(command);
	}
	 public boolean perform(AbstractBuild<?,?> build, Launcher launcher, TaskListener listener) throws InterruptedException {
	        FilePath ws = build.getWorkspace();
	        FilePath script=null;
	        try {
	            try {
	                script = createScriptFile(ws);
	            } catch (IOException e) {
	                Util.displayIOException(e,listener);
	                e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
	                return false;
	            }

	            int r;
	            try {
	                EnvVars envVars = build.getEnvironment(listener);
	                // on Windows environment variables are converted to all upper case,
	                // but no such conversions are done on Unix, so to make this cross-platform,
	                // convert variables to all upper cases.
	                for(Map.Entry<String,String> e : build.getBuildVariables().entrySet())
	                    envVars.put(e.getKey(),e.getValue());

	                ProcStarter launch = launcher.launch();
	                
	               ByteArrayOutputStream out = new ByteArrayOutputStream() ;
					r = launch.cmds(buildCommandLine(script)).envs(envVars).pwd(ws).stdout(out).join();
					
					
					
				
					output = out.toString();
	            } catch (IOException e) {
	                Util.displayIOException(e,listener);
	                e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
	                r = -1;
	            }
	            return r==0;
	        } finally {
	            try {
	                if(script!=null)
	                script.delete();
	            } catch (IOException e) {
	                Util.displayIOException(e,listener);
	                e.printStackTrace( listener.fatalError(Messages.CommandInterpreter_UnableToDelete(script)) );
	            }
	        }
	    }
	public String getOutput() {
		return output;
	}
	
	   @Extension
	    public static class DescriptorImpl extends Shell.DescriptorImpl { 
		   public String getDisplayName() {
	            return "No Output Shell";
	        }
	    }
	

}
