package com.groupon.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixRun;
import hudson.model.TaskListener;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;

import java.io.IOException;
import java.util.Map;

@Extension
public class ChunkContributor extends EnvironmentContributor {

	@Override
	public void buildEnvironmentFor(Run r, EnvVars envs, TaskListener listener)
			throws IOException, InterruptedException {
		 if (r instanceof MatrixRun)   setEnvironment((MatrixRun)r, envs,listener);

	}
	public void setEnvironment(MatrixRun run, EnvVars envs, TaskListener listener) {
        AxisList axes = run.getParent().getParent().getAxes();
        for (Map.Entry<String, String> e : run.getParent().getCombination().entrySet()) {
            @SuppressWarnings("unused")
			Axis a = axes.find(e.getKey());
          if (a != null && (a instanceof DynamicChunksAxis))
                ((DynamicChunksAxis) a).setEnviromentVariables(e.getValue(), envs, listener.getLogger(), run.getBuiltOn());
        }
    }
}
