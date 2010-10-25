package com.aptana.git.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

import com.aptana.git.core.model.ChangedFile;
import com.aptana.git.core.model.GitRepository;
import com.aptana.git.ui.internal.actions.ChangedFileAction;

abstract class StagingAction extends ChangedFileAction
{

	@Override
	public void run()
	{
		Map<GitRepository, List<ChangedFile>> repoToChangedFiles = new HashMap<GitRepository, List<ChangedFile>>();
		IResource[] resources = getSelectedResources();
		for (IResource resource : resources)
		{
			if (resource instanceof IContainer)
			{
				IContainer container = (IContainer) resource;
				GitRepository repo = getGitRepositoryManager().getAttached(resource.getProject());
				List<ChangedFile> files = repo.getChangedFilesForContainer(container);
				// FIXME just filter and add them all at the same time!
				for (ChangedFile file : files)
				{
					if (!changedFileIsValid(file))
						continue;

					List<ChangedFile> changedFiles = repoToChangedFiles.get(repo);
					if (changedFiles == null)
					{
						changedFiles = new ArrayList<ChangedFile>();
						repoToChangedFiles.put(repo, changedFiles);
					}
					changedFiles.add(file);
				}
			}
			else
			{
				ChangedFile correspondingChangedFile = getChangedFile(resource);
				if (!changedFileIsValid(correspondingChangedFile))
					continue;
				GitRepository repo = getGitRepositoryManager().getAttached(resource.getProject());
				List<ChangedFile> changedFiles = repoToChangedFiles.get(repo);
				if (changedFiles == null)
				{
					changedFiles = new ArrayList<ChangedFile>();
					repoToChangedFiles.put(repo, changedFiles);
				}
				changedFiles.add(correspondingChangedFile);
			}
		}

		for (Map.Entry<GitRepository, List<ChangedFile>> entry : repoToChangedFiles.entrySet())
		{
			doOperation(entry.getKey(), entry.getValue());
		}
	}

	protected abstract void doOperation(GitRepository repo, List<ChangedFile> changedFiles);

	@Override
	public boolean isEnabled()
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
		{
			return false;
		}
		for (IResource resource : resources)
		{
			if (!isEnabledForResource(resource))
				return false;
		}
		return true;
	}

	private boolean isEnabledForResource(IResource resource)
	{
		if (resource instanceof IContainer)
		{
			IContainer container = (IContainer) resource;
			GitRepository repo = getGitRepositoryManager().getAttached(resource.getProject());
			List<ChangedFile> files = repo.getChangedFilesForContainer(container);
			for (ChangedFile file : files)
			{
				if (changedFileIsValid(file))
					return true;
			}
		}
		return changedFileIsValid(getChangedFile(resource));
	}

	protected abstract boolean changedFileIsValid(ChangedFile correspondingChangedFile);
}
