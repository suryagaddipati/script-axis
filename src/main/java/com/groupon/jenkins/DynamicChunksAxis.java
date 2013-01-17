package com.groupon.jenkins;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import groovy.lang.GroovyShell;
import hudson.EnvVars;
import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.tasks.Shell;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jenkins.model.Jenkins;

public class DynamicChunksAxis extends Axis {

	
	private  String mapperCommand;
	private List<String> dynamicValues;

	public DynamicChunksAxis(String name,
			String mapperCommand) {
		super(name, Arrays.asList(new String [] {"default"}));
		dynamicValues = Arrays.asList(new String [] {"default"});
		this.mapperCommand = mapperCommand;

	}

	@Override
	public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
		OutputCapturingShell shell = new OutputCapturingShell(mapperCommand);	
		try {
			boolean out = shell.perform((AbstractBuild<?, ?>) context.getBuild(), context.getLauncher(), context.getListener());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		String[] chunks = shell.getOutput().split("\n");
		dynamicValues= Arrays.asList(Arrays.copyOfRange(chunks, 1, chunks.length));
		return getValues();
	}

	

	@Extension
	public static class DescriptorImpl extends AxisDescriptor {

		@Override
		public String getDisplayName() {
			return "DynamicChunksAxis";
		}

		@Override
		public Axis newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			
			return new DynamicChunksAxis(formData.getString("name"),
					formData.getString("mapperCommand")
					);

		}

	}
	
	@Override
	public List<String> getValues() {
		return dynamicValues;
	}

	public String getMapperCommand() {
		return mapperCommand;
	}

	public void setEnviromentVariables(String value, EnvVars envVar,
			PrintStream logger, Node builtOn) {
		envVar.put("CHUNKS_ALLOCATED", StringUtils.join(dynamicValues, ","));
		 
	}

	public void setMapperCommand(String mapperCommand) {
		this.mapperCommand = mapperCommand;
	}

}
