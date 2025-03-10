package in.oneton.idea.spring.assistant.plugin.suggestion.component;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.idea.maven.importing.MavenWorkspaceConfigurator;

public class MavenImporter implements MavenWorkspaceConfigurator {

  private static final Logger log = Logger.getInstance(MavenProcessorTask.class);

  public MavenImporter(String pluginGroupID, String pluginArtifactID) {
    super();
  }

//  @Override
//  public void preProcess(Module module, MavenProject mavenProject,
//      MavenProjectChanges mavenProjectChanges,
//      IdeModifiableModelsProvider ideModifiableModelsProvider) {
//
//  }
//
//  @Override
//  public void process(IdeModifiableModelsProvider ideModifiableModelsProvider, Module module,
//      MavenRootModelAdapter mavenRootModelAdapter, MavenProjectsTree mavenProjectsTree,
//      MavenProject mavenProject, MavenProjectChanges mavenProjectChanges,
//      Map<MavenProject, String> map, List<MavenProjectsProcessorTask> processorTasks) {
//    String skip = this.findConfigValue(mavenProject, "skip");
//    if (!"true".equals(skip)) {
//      processorTasks.add(new MavenProcessorTask(module));
//    } else {
//      debug(() -> log.debug(
//          "Skipping index check for project " + module.getProject().getName() + " & module "
//              + module.getName()));
//    }
//  }

  /**
   * Debug logging can be enabled by adding fully classified class name/package name with # prefix
   * For eg., to enable debug logging, go `Help > Debug log settings` & type `#in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionServiceImpl`
   *
   * @param doWhenDebug code to execute when debug is enabled
   */
  private void debug(Runnable doWhenDebug) {
    if (log.isDebugEnabled()) {
      doWhenDebug.run();
    }
  }

}
