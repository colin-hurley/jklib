package net.jkhub.jklib.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * TODO: This plugin doesn't have any goals, yet, but the maven-plugin packaging
 * requires at least one goal or it fails the build. This placeholder goal makes
 * it so the build will succeed while the plugin doesn't have any real goals.
 * After the first real goal is added to the plugin, this placeholder goal can
 * be removed
 */
@Mojo(name = "placeholder")
public class PlaceholderMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		throw new MojoExecutionException("This is just a placeholder, not a real goal");
	}

}
