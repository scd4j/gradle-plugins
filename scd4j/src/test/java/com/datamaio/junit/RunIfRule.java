/*******************************************************************************
 * Copyright (c) 2013 Rüdiger Herrmann
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package com.datamaio.junit;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier; 
 
import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
 
public class RunIfRule implements MethodRule {
  
  public interface RunIfCondition {
    boolean condition();
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface RunIf {
    Class<? extends RunIfCondition> value();
  }
 
  public Statement apply( Statement base, FrameworkMethod method, Object target ) {
    Statement result = base;
    if( hasRunIfAnnotation( method ) ) {
      RunIfCondition condition = getRunIfContition( method , target);
      if( !condition.condition() ) {
        result = new RunIfIgnoreStatement( condition );
      }
    }
    return result;
  }
 
  private boolean hasRunIfAnnotation( FrameworkMethod method ) {
    return method.getAnnotation( RunIf.class ) != null;
  }
 
  private RunIfCondition getRunIfContition( FrameworkMethod method , Object instance) {
    RunIf annotation = method.getAnnotation( RunIf.class );
    return newCondition( annotation, instance );
  }
 
  private RunIfCondition newCondition( RunIf annotation, Object instance ) {
      final Class<? extends RunIfCondition> cond = annotation.value();
    try {        
        if (cond.isMemberClass()) {
            if (Modifier.isStatic(cond.getModifiers())) {
                return (RunIfCondition) cond.getDeclaredConstructor(new Class<?>[]{}).newInstance();
            } else if (instance != null && instance.getClass().isAssignableFrom(cond.getDeclaringClass())) {
                return (RunIfCondition) cond.getDeclaredConstructor(new Class<?>[]{instance.getClass()}).newInstance(instance);
            }
            throw new IllegalArgumentException("Conditional class: " + cond.getName() + " was an inner member class however it was not declared inside the test case using it. Either make this class a static class (by adding static keyword), standalone class (by declaring it in it's own file) or move it inside the test case using it");
        } else {
            return cond.newInstance();
        }
    } catch( RuntimeException re ) { 
      throw re;
    } catch( Exception e ) {
                
      throw new RuntimeException( e );
    }
  }
 
  private static class RunIfIgnoreStatement extends Statement {
    private RunIfCondition condition;
 
    RunIfIgnoreStatement( RunIfCondition condition ) {
      this.condition = condition;
    }
 
    @Override
    public void evaluate() {
      Assume.assumeTrue( "Ignored by " + condition.getClass().getSimpleName(), false );
    }
  }
  
}