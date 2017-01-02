package org.thoughtslive.jenkins.plugins.hubot.steps;

import java.io.Serializable;

import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundSetter;

import lombok.Getter;

/**
 * Base class for all hubot steps
 */
public abstract class BasicHubotStep extends AbstractStepImpl implements Serializable {

	private static final long serialVersionUID = 7268920801605705697L;

	@Getter
	@DataBoundSetter
	protected String room;

	@Getter
	@DataBoundSetter
	protected String message;

	@Getter
	@DataBoundSetter
	private boolean failOnError = true;

	@Getter
	@DataBoundSetter
	private String url;

}