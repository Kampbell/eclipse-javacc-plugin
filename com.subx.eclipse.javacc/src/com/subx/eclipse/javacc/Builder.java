package com.subx.eclipse.javacc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

import COM.sun.labs.javacc.Main;

/**
 * @author Peter M. Murray
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Builder extends IncrementalProjectBuilder implements IResourceDeltaVisitor
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
	}

	private void incrementalBuild(IProgressMonitor monitor) throws CoreException
	{
		System.out.println("JAVACC: Incremental Build");
		getDelta(getProject()).accept(this);
	}

	public boolean visit(IResourceDelta delta) throws CoreException
	{
		IResource resource = delta.getResource();

		if ("jj".equals(resource.getFullPath().getFileExtension()))
			buildJJFile(resource);

		return true;
	}

	int count = 0;

	private boolean buildJJFile(IResource resource)
	{
		IContainer parent = resource.getParent();
		File directory;
		FileWriter writer;
		HashSet before;

		try
		{
			cleanupPreviousBuild(resource);
			before = getDirectoryFiles(resource);
			doBuild(resource);
			recordBuildChanges(resource, before);
			resource.getParent().refreshLocal(IResource.DEPTH_ONE, null);
			setResourceFlags(resource);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	private void cleanupPreviousBuild(IResource resource) throws IOException, CoreException
	{
		String previousFileList = resource.getPersistentProperty(kPreviousRunFiles);

		if (previousFileList != null)
		{
			String base = resource.getParent().getLocation().toString();
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

	private HashSet getDirectoryFiles(IResource resource) throws IOException
	{
		File[] files = new File(resource.getParent().getLocation().toString()).listFiles();
		HashSet set = new HashSet();

		for (int ct = 0; ct < files.length; ct++)
			set.add(files[ct]);

		return set;
	}

	private void doBuild(IResource resource) throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream errorStream, oldErrorStream = System.err;
		String[] args = new String[2];
		args[0] = "-OUTPUT_DIRECTORY=" + resource.getParent().getLocation();
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

	private void recordBuildChanges(IResource resource, HashSet before) throws IOException, CoreException
	{
		Iterator iterator;
		StringBuffer list = new StringBuffer();
		HashSet generated = getDirectoryFiles(resource);
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

	private void setResourceFlags(IResource resource) throws CoreException
	{
		IContainer parent = resource.getParent();
		String previousFileList = resource.getPersistentProperty(kPreviousRunFiles);

		if (previousFileList != null)
		{
			StringTokenizer names = new StringTokenizer(previousFileList, ",");
			while (names.hasMoreElements())
			{
				IResource derivedResource = parent.findMember(names.nextElement().toString());
				derivedResource.setReadOnly(true);
				derivedResource.setDerived(true);
			}
		}
	}

}
