package org.springframework.security.demo;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.AnnotationConfiguration.ParserTask;
import org.eclipse.jetty.annotations.AnnotationConfiguration.TimeStatistic;
import org.eclipse.jetty.annotations.AnnotationConfiguration.ContainerClassNameResolver;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.AnnotationParser.Handler;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.statistic.CounterStatistic;
import org.eclipse.jetty.webapp.WebAppContext;

@Deprecated
public class SecurityAnnotationConfiguration 
 extends AnnotationConfiguration {

  @Override
  public void parseContainerPath(final WebAppContext context, final AnnotationParser parser) 
   throws Exception {
    final Set<Handler> handlers = new HashSet<Handler>();
    handlers.addAll(_discoverableAnnotationHandlers);
    handlers.addAll(_containerInitializerAnnotationHandlers);
    if (_classInheritanceHandler != null)
      handlers.add(_classInheritanceHandler);

    _containerPathStats = new CounterStatistic();
    for (Resource r : getAllResources(getClass().getClassLoader())) 
      if (_parserTasks != null) {
        ParserTask task 
          = new ParserTask(parser, handlers, r,
                           new ContainerClassNameResolver(context));
        _parserTasks.add(task);
        _containerPathStats.increment();
      }
  }

  // I know this description is disgusting, so it should be rewritten later.
  private List<Resource> getAllResources(ClassLoader loader)
   throws IOException {
    if (loader instanceof URLClassLoader) {
      List<Resource> resources = new ArrayList<Resource>(); 
      for (URL url : ((URLClassLoader) loader).getURLs())
        resources.add(Resource.newResource(url));
      return resources;
    } 
    else return Collections.emptyList();
  } 
}
