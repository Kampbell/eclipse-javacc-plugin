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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.javacc.parser.Main;

/**
 * @author Peter M. Murray
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class Builder extends IncrementalProjectBuilder implements IResourceDeltaVisitor, IResourceVisitor
{
	private static final QualifiedName kPreviousRunFiles = new QualifiedName("com.subx.eclipse.javacc", "Builder.PreviousFiles");

	String buildRoot;

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
	{
		buildRoot = JavaCore.create(getProject()).getOutputLocation().toString();

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
		// All components of the project will be traversed. But he actual visit
		// will check that the resource is a component on the classpath.
		//
		getProject().accept(this);
	}

	private void incrementalBuild(IProgressMonitor monitor) throws CoreException
	{
		IResourceDelta delta = getDelta(getProject());
		if (delta != null)
			delta.accept(this);
	}

	public boolean visit(IResourceDelta delta) throws CoreException
	{
		return visit(delta.getResource());
	}

	public boolean visit(IResource resource) throws CoreException
	{
		IPath path = resource.getFullPath();

		if (!path.toString().startsWith(buildRoot) && "jj".equals(path.getFileExtension()))
		{
			// Only build if the resource is included as a source.
			//
			if (JavaCore.create(getProject()).isOnClasspath(resource))
				buildJJFile(resource);
		}
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
		IProject project = this.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		int ncp = classpath.length;

		IPath resourcePath = resource.getParent().getFullPath();
		IPath destinationPath = null;
		for (int ct = 0; ct < ncp; ct++)
		{
			IClasspathEntry cpe = classpath[ct];

			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE)
			{
				IPath path = cpe.getPath();
				if (path.isPrefixOf(resourcePath))
				{
					// Make the destination path relative to the class path
					// (i.e. preserve
					// package structure but strip everything before that).
					//
					destinationPath = resourcePath.removeFirstSegments(path.segmentCount());
					break;
				}
			}
		}

		if (destinationPath == null)
			//
			// The resourcePath was not a child to any of the classpaths. This
			// should
			// not really happen now.
			//
			destinationPath = resourcePath.removeFirstSegments(2);

		IPath generatedSrcPath = project.getFullPath().append("_generated");
		IPath projRelative = generatedSrcPath.removeFirstSegments(1); // Strip
																	  // project
		destinationPath = projRelative.append(destinationPath).makeRelative();

		IContainer destination = (IContainer) project.findMember(destinationPath);
		if (destination == null)
		{
			File destinationDirectory = new File(project.getLocation().toString() + "/" + destinationPath);
			destinationDirectory.mkdirs();

			try
			{
				boolean exists = false;
				for (int ct = 0; ct < ncp && !exists; ct++)
				{
					IClasspathEntry cpe = classpath[ct];
					exists = (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE && cpe.getPath().equals(generatedSrcPath));
				}
				if (!exists)
				{
					IClasspathEntry[] newClasspath = new IClasspathEntry[ncp + 1];
					System.arraycopy(classpath, 0, newClasspath, 0, ncp);
					newClasspath[ncp] = JavaCore.newSourceEntry(generatedSrcPath);
					javaProject.setRawClasspath(newClasspath, null);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			project.refreshLocal(IContainer.DEPTH_INFINITE, null);
			destination = (IContainer) project.findMember(destinationPath);
			project.findMember("_generated").accept(kDerivedTagger);
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
		PrintStream errorStream, oldErrorStream = System.err, oldOutputStream = System.out;
		String[] args = new String[2];
		args[0] = "-OUTPUT_DIRECTORY=" + destination.getLocation();
		args[1] = resource.getLocation().toString();
		try
		{
			errorStream = new PrintStream(outputStream);
			System.setErr(errorStream);
			System.setOut(errorStream);
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
			System.setOut(oldOutputStream);
		}

		try
		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;

			resource.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
			while ((line = reader.readLine()) != null)
			{
				//System.out.println("LINE:" + line);
				if (line.startsWith("Error:") || line.startsWith("org.javacc.parser.ParseException") || line.startsWith("Warning:"))
				{
					if (line.startsWith("Warning:"))
					{
						line += " " + reader.readLine().trim();
						line += " " + reader.readLine().trim();
						line += " " + reader.readLine().trim();
					}
					int comma = 0;

					while ((comma = line.indexOf(',', comma + 1)) > 0)
					{
						int space = line.lastIndexOf(' ', comma) + 1;
						int lineNumber = Integer.parseInt(line.substring(space, comma));
						IMarker marker = resource.createMarker(IMarker.PROBLEM);
						marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
						if (line.startsWith("Warning"))
							marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
						else
							marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
						marker.setAttribute(IMarker.MESSAGE, line);
						marker.setAttribute(IMarker.LOCATION, line);
					}
				}
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