package com.subx.eclipse.javacc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IJavaProject;

import COM.sun.labs.javacc.Main;

/**
 * @author Peter M. Murray
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Builder extends IncrementalProjectBuilder implements IResourceDeltaVisitor, IResourceVisitor
{
	private static final QualifiedName kPreviousRunFiles = new QualifiedName("com.subx.eclipse.javacc", "Builder.PreviousFiles");

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
	{
		if (kind == IncrementalProjectBuilder.FULL_BUILD)
		{
			fullBuild(monitor);
		}
		else
		{
			incrementalBuild(monitor);
		}

		return null;
	}

	private void fullBuild(IProgressMonitor montior) throws CoreException
	{
		System.out.println("JAVACC: Full Build");
		getProject().accept(this);
	}

	private void incrementalBuild(IProgressMonitor monitor) throws CoreException
	{
		System.out.println("JAVACC: Incremental Build");
		getDelta(getProject()).accept(this);
	}

	public boolean visit(IResourceDelta delta) throws CoreException
	{
		return visit(delta.getResource());
	}

	public boolean visit(IResource resource) throws CoreException
	{
		if ("jj".equals(resource.getFullPath().getFileExtension()))
			buildJJFile(resource);

		return true;
	}

	private boolean buildJJFile(IResource resource)
	{
		IContainer destination;
		File directory;
		FileWriter writer;
		HashSet before;
		

		try
		{
			destination = findOrCreateGeneratedSourceDirectory(resource);
			cleanupPreviousBuild(resource, destination);
			before = getDirectoryFiles(resource, destination);
			doBuild(resource, destination);
			recordBuildChanges(resource, destination, before);
			destination.refreshLocal(IResource.DEPTH_ONE, null);
			setResourceFlags(resource, destination);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	private IContainer findOrCreateGeneratedSourceDirectory(IResource resource) throws CoreException
	{
		IPath resourcePath = resource.getParent().getFullPath();
		IPath generatedSrcPath = getProject().getFullPath().append("_generated"); 
		IPath destinationPath = generatedSrcPath.removeFirstSegments(1).append(resourcePath.removeFirstSegments(2)).makeRelative();
		IContainer destination = (IContainer)(getProject().findMember(destinationPath));
		if(destination == null)
		{
			File destinationDirectory = new File(getProject().getLocation().toString() + "/" + destinationPath);
			destinationDirectory.mkdirs();

			try
			{
				IJavaProject javaProject = JavaCore.create(getProject());
				IClasspathEntry[] classpath = javaProject.getRawClasspath();
				boolean exists = false;
				for(int ct = 0; ct < classpath.length && !exists; ct++)
				{
					exists = (classpath[ct].getEntryKind() == IClasspathEntry.CPE_SOURCE &&
							  classpath[ct].getPath().equals(generatedSrcPath));
				}
				if(!exists)
				{
					IClasspathEntry[] newClasspath = new IClasspathEntry[classpath.length+1];
					System.arraycopy(classpath,0,newClasspath,0,classpath.length);
					newClasspath[classpath.length] = JavaCore.newSourceEntry(generatedSrcPath);
					javaProject.setRawClasspath(newClasspath, null);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			getProject().refreshLocal(IContainer.DEPTH_INFINITE, null);
			destination = (IContainer)(getProject().findMember(destinationPath));
			getProject().findMember("_generated").accept(kDerivedTagger);
		}
		
		return destination;
	}
	
	private static final DerivedResourceTagger kDerivedTagger = new DerivedResourceTagger();
	
	private static class DerivedResourceTagger implements IResourceVisitor
	{
		public boolean visit(IResource resource) throws CoreException
		{
			resource.setDerived(true);
			return true;
		}
	}
	
	private void cleanupPreviousBuild(IResource resource, IContainer destination) throws IOException, CoreException
	{
		String previousFileList = resource.getPersistentProperty(kPreviousRunFiles);

		if (previousFileList != null)
		{
			String base = destination.getLocation().toString();
			System.out.println("JAVACC: Deleting : " + previousFileList);
			StringTokenizer names = new StringTokenizer(previousFileList, ",");
			while (names.hasMoreElements())
			{
				File file = new File(base + "/" + names.nextElement());
				if (file.exists())
					file.delete();
			}
		}
	}

	private HashSet getDirectoryFiles(IResource resource, IContainer destination) throws IOException
	{
		File[] files = new File(destination.getLocation().toString()).listFiles();
		HashSet set = new HashSet();

		for (int ct = 0; ct < files.length; ct++)
			set.add(files[ct]);

		return set;
	}

	private void doBuild(IResource resource, IContainer destination) throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream errorStream, oldErrorStream = System.err;
		String[] args = new String[2];
		args[0] = "-OUTPUT_DIRECTORY=" + destination.getLocation();
		args[1] = resource.getLocation().toString();
		try
		{
			errorStream = new PrintStream(outputStream);
			System.setErr(errorStream);
			Main.mainProgram(args);
			errorStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.setErr(oldErrorStream);
		}

		try
		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			
			resource.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
			while ((line = reader.readLine()) != null)
			{
				System.out.println(line);
				int comma = line.indexOf(',');
				int lineNumber = Integer.parseInt(line.substring(12, comma));
				String message = line.substring(line.indexOf(':', comma) + 2);
				IMarker marker = resource.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.MESSAGE, message);
			}
			reader.close();
			inputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void recordBuildChanges(IResource resource, IContainer destination, HashSet before) throws IOException, CoreException
	{
		Iterator iterator;
		StringBuffer list = new StringBuffer();
		HashSet generated = getDirectoryFiles(resource, destination);
		generated.removeAll(before);

		iterator = generated.iterator();
		while (iterator.hasNext())
		{
			File file = (File) iterator.next();
			if (list.length() > 0)
				list.append(",");
			list.append(file.getName());
		}
		System.out.println("JAVACC: Recording Build: " + list.toString());
		resource.setPersistentProperty(kPreviousRunFiles, list.toString());
	}

	private void setResourceFlags(IResource resource, IContainer destination) throws CoreException
	{
		String previousFileList = resource.getPersistentProperty(kPreviousRunFiles);

		if (previousFileList != null)
		{
			StringTokenizer names = new StringTokenizer(previousFileList, ",");
			while (names.hasMoreElements())
			{
				IResource derivedResource = destination.findMember(names.nextElement().toString());
				derivedResource.setReadOnly(true);
				derivedResource.setDerived(true);
			}
		}
	}

}
