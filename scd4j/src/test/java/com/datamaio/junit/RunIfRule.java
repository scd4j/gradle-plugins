/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 scd4j scd4j.tools@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

/**
 * <p>
 * This class was originally written by Rüdiger Herrmann and Matt Morrissette
 * and published on github (https://gist.github.com/rherrmann/7447571) with the
 * name ConditionalIgnoreRule. The class was renamed and the logical was
 * refactored. Now we test if the JUnit test will run. On the original version
 * the test would not run if the condition was true.
 * <p>
 */
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