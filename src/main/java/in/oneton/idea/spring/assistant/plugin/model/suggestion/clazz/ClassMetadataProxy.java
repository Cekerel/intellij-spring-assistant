package in.oneton.idea.spring.assistant.plugin.model.suggestion.clazz;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.CachedValue;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.containers.ContainerUtil;
import in.oneton.idea.spring.assistant.plugin.completion.FileType;
import in.oneton.idea.spring.assistant.plugin.completion.SuggestionDocumentationHelper;
import in.oneton.idea.spring.assistant.plugin.model.suggestion.Suggestion;
import in.oneton.idea.spring.assistant.plugin.model.suggestion.SuggestionNode;
import in.oneton.idea.spring.assistant.plugin.model.suggestion.SuggestionNodeType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.psi.util.CachedValuesManager.getCachedValue;
import static in.oneton.idea.spring.assistant.plugin.model.suggestion.SuggestionNodeType.UNKNOWN_CLASS;
import static in.oneton.idea.spring.assistant.plugin.model.suggestion.clazz.ClassSuggestionNodeFactory.newClassMetadata;
import static in.oneton.idea.spring.assistant.plugin.util.PsiCustomUtil.computeDependencies;
import static in.oneton.idea.spring.assistant.plugin.util.PsiCustomUtil.toValidPsiClass;
import static in.oneton.idea.spring.assistant.plugin.util.PsiCustomUtil.typeToFqn;
import static java.util.Objects.requireNonNull;

public class ClassMetadataProxy implements MetadataProxy {

  private static final Logger log = Logger.getInstance(ClassMetadataProxy.class);

  private static ConcurrentMap<String, Key<CachedValue<ClassMetadata>>> fqnToKey =
      ContainerUtil.newConcurrentMap();

  @NotNull
  private final PsiClass targetClass;

  @NotNull
  private final PsiClassType type;

  ClassMetadataProxy(@NotNull PsiClassType type) {
    this.type = type;
    targetClass = requireNonNull(toValidPsiClass(type));
  }

  @Nullable
  @Override
  public SuggestionDocumentationHelper findDirectChild(Module module, String pathSegment) {
    return doWithTargetAndReturn(module, target -> target.findDirectChild(module, pathSegment),
        null);
  }

  @Nullable
  @Override
  public Collection<? extends SuggestionDocumentationHelper> findDirectChildrenForQueryPrefix(
      Module module, String querySegmentPrefix) {
    return doWithTargetAndReturn(module,
        target -> target.findDirectChildrenForQueryPrefix(module, querySegmentPrefix), null);
  }

  @Nullable
  @Override
  public List<SuggestionNode> findDeepestSuggestionNode(Module module,
      List<SuggestionNode> matchesRootTillParentNode, String[] pathSegments,
      int pathSegmentStartIndex) {
    return doWithTargetAndReturn(module, target -> target
        .findDeepestSuggestionNode(module, matchesRootTillParentNode, pathSegments,
            pathSegmentStartIndex), null);
  }

  @Nullable
  @Override
  public SortedSet<Suggestion> findKeySuggestionsForQueryPrefix(Module module, FileType fileType,
      List<SuggestionNode> matchesRootTillMe, int numOfAncestors, String[] querySegmentPrefixes,
      int querySegmentPrefixStartIndex) {
    return doWithTargetAndReturn(module, target -> target
        .findKeySuggestionsForQueryPrefix(module, fileType, matchesRootTillMe, numOfAncestors,
            querySegmentPrefixes, querySegmentPrefixStartIndex), null);
  }

  @Nullable
  @Override
  public SortedSet<Suggestion> findValueSuggestionsForPrefix(Module module, FileType fileType,
      List<SuggestionNode> matchesRootTillMe, String prefix) {
    return doWithTargetAndReturn(module,
        target -> target.findValueSuggestionsForPrefix(module, fileType, matchesRootTillMe, prefix),
        null);
  }

  @Nullable
  @Override
  public String getDocumentationForValue(Module module, String nodeNavigationPathDotDelimited,
      String value) {
    return doWithTargetAndReturn(module,
        target -> target.getDocumentationForValue(module, nodeNavigationPathDotDelimited, value),
        null);
  }

  @Override
  public boolean isLeaf(Module module) {
    return doWithTargetAndReturn(module, target -> target.isLeaf(module), true);
  }

  @NotNull
  @Override
  public SuggestionNodeType getSuggestionNodeType(Module module) {
    return doWithTargetAndReturn(module, ClassMetadata::getSuggestionNodeType, UNKNOWN_CLASS);
  }

  @Nullable
  @Override
  public PsiType getPsiType(Module module) {
    return doWithTargetAndReturn(module, target -> target.getPsiType(module), null);
  }

  @Override
  public boolean targetRepresentsArray() {
    return false;
  }

  @Override
  public boolean targetClassRepresentsIterable(Module module) {
    return doWithTargetAndReturn(module, IterableClassMetadata.class::isInstance, false);
  }

  <T> T doWithTargetAndReturn(Module module,
      TargetInvokerWithReturnValue<T> targetInvokerWithReturnValue, T defaultReturnValue) {
    ClassMetadata target = getTarget(module);
    if (target != null) {
      return targetInvokerWithReturnValue.invoke(target);
    }
    return defaultReturnValue;
  }

  //  private void doWithTarget(TargetInvoker targetInvoker) {
  //    ClassMetadata target = getTarget();
  //    if (target != null) {
  //      targetInvoker.invoke(target);
  //    }
  //  }

  private ClassMetadata getTarget(Module module) {
    String fqn = typeToFqn(module, type);
    if (fqn != null) {
      String userDataKeyRef = "spring_assistant_plugin_class_metadata:" + fqn;
      Key<CachedValue<ClassMetadata>> classMetadataKey =
          ConcurrencyUtil.cacheOrGet(fqnToKey, userDataKeyRef, Key.create(userDataKeyRef));
      return getCachedValue(targetClass, classMetadataKey, () -> {
        log.debug("Creating metadata instance for " + userDataKeyRef);
        Set<PsiClass> dependencies = computeDependencies(module, type);
        if (dependencies != null) {
          return create(newClassMetadata(type), dependencies);
        }
        return null;
      });
    }
    return null;
  }


  protected interface TargetInvokerWithReturnValue<T> {
    T invoke(ClassMetadata classMetadata);
  }


  private interface TargetInvoker {
    void invoke(ClassMetadata classMetadata);
  }

}