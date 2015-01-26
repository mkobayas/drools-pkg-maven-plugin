/*
 * Copyright 2015 Masazumi Kobayashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mk300.maven.plugin.drools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.drools.core.util.DroolsStreamUtils;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;

/**
 * Generate Drools pkg file plugin.
 * 
 * @goal drools-pkg
 * @requiresDependencyResolution compile+runtime
 * @author mkobayas@redhat.com
 */
public class PkgBuilderMojo extends AbstractMojo {

	private static String pomTemplete = "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd'>"
			+ "<modelVersion>4.0.0</modelVersion>"
			+ "<groupId>tmp</groupId>"
			+ "<artifactId>tmp</artifactId>"
			+ "<version>1</version>"
			+ "</project>";
	
	/**
	 * The directory for rules base dir.
	 * 
	 * @parameter expression="${drools-pkg.ruleBaseDir}" default-value="${project.basedir}/src/main/resources"
	 */
	private String ruleBaseDir;

    /**
     * The directory for the generated pkg.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File targetDir;
    
	/**
	 * File name of Drools pkg file(output).
	 * 
	 * @parameter expression="${drools-pkg.pkgFileName}" default-value="${project.artifactId}.pkg"
	 */
	private String pkgFileName;
	
	/**
     * Encoding of rule file(DRL).
     *
     * @parameter expression="${drools-pkg.encoding}" default-value="${project.build.sourceEncoding}"
     */
    private String encoding;
    
    /**
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;
    
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		getLog().info("ruleBaseDir=" + ruleBaseDir);

		ClassLoader cl = createProjectClassLoader();
		ClassLoader orgCl = Thread.currentThread().getContextClassLoader();
		
		FileOutputStream fos = null;
		try {
			Thread.currentThread().setContextClassLoader(cl);
			
			File outputFile = new File(targetDir, pkgFileName);
			Collection<KiePackage> pkgs = createKiePackage();
			
			logKBase(pkgs);
			
			fos = new FileOutputStream(outputFile);
			DroolsStreamUtils.streamOut(fos, pkgs);
			fos.flush();
			
			getLog().info("Output to " + outputFile);
		} catch (IOException e) {
			throw new MojoExecutionException("hogeee", e);
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
			Thread.currentThread().setContextClassLoader(orgCl);
		}
	
	}

	
	private Collection<KiePackage> createKiePackage() throws MojoExecutionException, IOException {
		
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kfs = kieServices.newKieFileSystem();

		File ruleBaseDirFile = new File(ruleBaseDir);
		writeAllRule(kfs, ruleBaseDirFile);

		kfs.writePomXML(pomTemplete);

		KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
		if (kieBuilder.getResults().getMessages(Level.WARNING).size() > 0) {
			getLog().warn("kieBuilder warn!: " + kieBuilder.getResults().getMessages(Level.WARNING));
		}
		if (kieBuilder.getResults().getMessages(Level.ERROR).size() > 0) {
			getLog().error("kieBuilder error!: " + kieBuilder.getResults().getMessages(Level.ERROR));
			throw new MojoExecutionException("ERROR");
		}

		ReleaseId releaseId = kieServices.newReleaseId("tmp", "tmp", "1");
		KieContainer kieContainer = kieServices.newKieContainer(releaseId);

		kieServices.getRepository().removeKieModule(releaseId);
		
		return kieContainer.getKieBase().getKiePackages();
	}
	
	
	private void writeAllRule(KieFileSystem kfs, File file) throws IOException {
		if (!file.exists() || file.isHidden()) {
			return;
		}

		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				writeAllRule(kfs, subFile);
			}
			return;
		}

		if (file.canRead()) {
			KieServices kieServices = KieServices.Factory.get();
			String fullPath = file.getAbsolutePath();

			if (File.separator.equals("\\")) {
				// for Windows
				fullPath = fullPath.replaceAll("\\\\", "/");
			}

			Resource resource = kieServices.getResources().newFileSystemResource(fullPath, encoding);
			
			if(isTextRule(resource)) {
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(resource.getReader());
				for (String line; (line = br.readLine()) != null;) {
					sb.append(line).append(System.lineSeparator());
			    }
				br.close();

				kfs.write("src/main/resources/" + fullPath, sb.toString());
				getLog().info("Add rule file=" + fullPath + "(" + encoding + ")");
			} else {
				kfs.write("src/main/resources/" + fullPath, resource);
				getLog().info("Add rule file=" + fullPath + "(binary)");
			}
		}
	}
	
	private void logKBase(Collection<KiePackage> packages) {
		getLog().info("Builded packages=[");
		if(packages == null || packages.isEmpty()) {
			getLog().info("+pkg = <empty>");
			return;
		}
		
		for (KiePackage pkg : packages) {
			getLog().info(" +pkg = " + pkg);
			
			Collection<Rule> rules = pkg.getRules();
			if(rules.isEmpty()) {
				getLog().info(" --rule = <empty>");
				continue;
			}
			for (Rule rule : rules) {
				getLog().info(" --rule = " + rule.getName());
			}
		}
		getLog().info("]");
	}
	
	
	private boolean isTextRule(Resource resource) {
		
		ResourceType type = resource.getResourceType();
		
		if(ResourceType.DRL.equals(type)) return true;
		if(ResourceType.BPMN2.equals(type)) return true;
		if(ResourceType.DRF.equals(type)) return true;
		if(ResourceType.DSL.equals(type)) return true;
		if(ResourceType.DSLR.equals(type)) return true;
		
		return false;
	}
	
	
	@SuppressWarnings("unchecked")
	private ClassLoader createProjectClassLoader() throws MojoExecutionException {
	    List<String> projectClasspathElements = null;
	    try {
	        projectClasspathElements = this.mavenProject.getCompileClasspathElements();
	    } catch (DependencyResolutionRequiredException e) {
	        new MojoExecutionException("Dependency resolution failed", e);
	    }

	    
	    List<URL> projectClasspathList = new ArrayList<URL>();
	    for (String element : projectClasspathElements) {
	    	File elementFile = new File(element);
	    	URL url;
	    	try {
	    		url = elementFile.toURI().toURL();
	    		projectClasspathList.add(url);
	    	} catch (MalformedURLException e) {
	            throw new MojoExecutionException(element + " is an invalid classpath element", e);
	        }
	    }
	    ClassLoader pluginClassLoader = getClass().getClassLoader();
	    ClassLoader mavenClassLoader = pluginClassLoader.getParent();
	    ClassLoader projectClassLoader =
	        new URLClassLoader(projectClasspathList.toArray(new URL[projectClasspathList.size()]), mavenClassLoader);
	    return projectClassLoader;
	}
}
