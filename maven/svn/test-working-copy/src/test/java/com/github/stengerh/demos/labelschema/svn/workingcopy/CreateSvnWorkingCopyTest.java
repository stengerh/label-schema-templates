package com.github.stengerh.demos.labelschema.svn.workingcopy;

import org.junit.Test;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc2.compat.SvnCodec;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNFileFilter;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnImport;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateSvnWorkingCopyTest {
  @Test
  public void createWorkingCopy() throws SVNException, IOException {
    Path parentPath = Paths.get("..").toAbsolutePath();

    Path repositoryPath = Paths.get("target", "svn", "repository").toAbsolutePath();
    File repositoryDir = repositoryPath.toFile();

    Path workingCopyPath = Paths.get("target", "svn", "working-copy").toAbsolutePath();
    File workingCopyDir = workingCopyPath.toFile();

    deleteFileTree(repositoryPath, true);
    deleteFileTree(workingCopyPath, false);

    SVNURL repositoryUrl = SVNRepositoryFactory.createLocalRepository(repositoryDir, false, true);
    SVNURL trunkUrl = repositoryUrl.appendPath("trunk", false);
    
    SVNClientManager clientManager = SVNClientManager.newInstance();
    try {
      createProjectLayout(clientManager, repositoryUrl);
      importFiltered(clientManager, trunkUrl, parentPath.toFile(), this::includeFile);
      checkout(clientManager, trunkUrl, workingCopyDir);
      fixScmInPomXml(clientManager, trunkUrl, workingCopyDir);
    } finally {
      clientManager.dispose();
    }
  }

  private void fixScmInPomXml(SVNClientManager clientManager, SVNURL trunkUrl, File workingCopyDir) {
    // TODO
  }

  private void createProjectLayout(SVNClientManager clientManager, SVNURL repositoryUrl) throws SVNException {
    clientManager.getCommitClient().doMkDir(
      new SVNURL[]{
        repositoryUrl.appendPath("trunk", false),
        repositoryUrl.appendPath("tags", false),
        repositoryUrl.appendPath("branches", false),
      },
      "Create project layout"
    );
  }

  private void checkout(SVNClientManager clientManager, SVNURL trunkUrl, File workingCopyDir) throws SVNException {
    clientManager.getUpdateClient().doCheckout(
      trunkUrl,
      workingCopyDir,
      SVNRevision.HEAD, SVNRevision.HEAD,
      SVNDepth.INFINITY, false
    );
  }

  private boolean includeFile(File file) {
    return !(
      file.getName().equals("target")
        || file.getName().equals("test-working-copy")
        || file.getName().endsWith(".iml")
    );
  }

  private void importFiltered(SVNClientManager clientManager, SVNURL targetUrl, File source, ISVNFileFilter fileFilter) throws SVNException {
    SvnOperationFactory operationFactory = new SvnOperationFactory();
    try {
      SvnImport svnImport = operationFactory.createImport();
      svnImport.setCommitHandler(SvnCodec.commitHandler(clientManager.getCommitClient().getCommitHandler()));
      svnImport.setCommitMessage("Import source");
      svnImport.setRevisionProperties(null);
      svnImport.addTarget(SvnTarget.fromURL(targetUrl));
      svnImport.setSource(source);
      svnImport.setDepth(SVNDepth.INFINITY);
      svnImport.setUseGlobalIgnores(true);
      svnImport.setApplyAutoProperties(true);
      svnImport.setFileFilter(fileFilter);

      svnImport.run();
    } finally {
      operationFactory.dispose();
    }
  }

  private void deleteFileTree(Path path, boolean force) throws IOException {
    if (Files.exists(path)) {
      Files.walkFileTree(path, new DeletingFileVisitor(force));
    }
  }

}
