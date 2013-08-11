/*
 * JPPF.
 * Copyright (C) 2005-2013 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jppf.application.template;

import java.util.List;

import org.jppf.client.*;
import org.jppf.server.protocol.JPPFTask;
import algoritmo.binario.AGBinario2;
import java.util.ArrayList;
import org.jppf.utils.JPPFConfiguration;
import org.jppf.utils.StringUtils;
import org.jppf.utils.TypedProperties;
import org.slf4j.*;


/**
 * This is a template JPPF application runner.
 * It is fully commented and is designed to be used as a starting point
 * to write an application using JPPF.
 * @author Laurent Cohen
 */
public class TemplateApplicationRunner {
    
   /**
   * Logger for this class.
   */
  static Logger log = LoggerFactory.getLogger(TemplateApplicationRunner.class);
  
  /**
   * The JPPF client, handles all communications with the server.
   * It is recommended to only use one JPPF client per JVM, so it
   * should generally be created and used as a singleton.
   */
  private static JPPFClient jppfClient =  null;
  
  private static ArrayList resultados = new ArrayList();

  /**
   * The entry point for this application runner to be run from a Java command line.
   * @param args by default, we do not use the command line arguments,
   * however nothing prevents us from using them if need be.
   */
  public static void main(final String...args) {
    try {
      // create the JPPFClient. This constructor call causes JPPF to read the configuration file
      // and connect with one or multiple JPPF drivers.
      jppfClient = new JPPFClient();

      // create a runner instance.
      TemplateApplicationRunner runner = new TemplateApplicationRunner();
      
      TypedProperties props = JPPFConfiguration.getProperties();
      int poblacion = props.getInt("algoritmo.poblacion");
      int generaciones = props.getInt("algoritmo.generaciones");
      
      //Para medir el tiempo total que tarda y las iteraciones que realiza;
      long totalIterationTime = System.currentTimeMillis();
      int iter = 0;
      //Creamos las jobs y las executamos
      for ( float cruza = 0.0f; cruza <= 1.1f; cruza += 0.1f )
          for ( float mutacion = 0.0f; mutacion <= 1.1f; mutacion += 0.1f ){
              iter++;
              
              JPPFJob job = runner.createJob( poblacion, generaciones, cruza, mutacion);
              runner.executeBlockingJob(job);
              
          }

      output("Corridas: " + iter + " Tiempo: " + ( StringUtils.toStringDuration( System.currentTimeMillis() - totalIterationTime ) ) );
      output("Tiempo promedio: " + ( StringUtils.toStringDuration( (System.currentTimeMillis() - totalIterationTime) / iter) ) );
      
      //Buscamos la mejor solución
      double mejor_valor = Double.MAX_VALUE;
      ArrayList mejor_solucion = null;
      for( Object solucion: resultados ){
          ArrayList sol = (ArrayList) solucion;
          if( (double) sol.get(0) < mejor_valor ){
              mejor_valor = (double) sol.get(0);
              mejor_solucion = sol;
          }
      }
      output("mejor solución  " + mejor_solucion.get(5).toString() );
      
      // execute a non-blocking job
      //runner.executeNonBlockingJob(job);
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      if (jppfClient != null) jppfClient.close();
    }
  }

  /**
   * Create a JPPF job that can be submitted for execution.
   * @return an instance of the {@link org.jppf.client.JPPFJob JPPFJob} class.
   * @throws Exception if an error occurs while creating the job or adding tasks.
   */
  public JPPFJob createJob( int poblacion, int generaciones, float pCruza, float pMutacion  ) throws Exception {
    // create a JPPF job
    JPPFJob job = new JPPFJob();

    // give this job a readable unique id that we can use to monitor and manage it.
    job.setName("Template Job Id");

    // add a task to the job.
    job.addTask(new TemplateJPPFTask(poblacion, generaciones, pCruza, pMutacion) );
    // add more tasks here ...

    // there is no guarantee on the order of execution of the tasks,
    // however the results are guaranteed to be returned in the same order as the tasks.
    return job;
  }

  /**
   * Execute a job in blocking mode. The application will be blocked until the job
   * execution is complete.
   * @param job the JPPF job to execute.
   * @throws Exception if an error occurs while executing the job.
   */
  public void executeBlockingJob(final JPPFJob job) throws Exception {
    // set the job in blocking mode.
    job.setBlocking(true);

    // Submit the job and wait until the results are returned.
    // The results are returned as a list of JPPFTask instances,
    // in the same order as the one in which the tasks where initially added the job.
    List<JPPFTask> results = jppfClient.submit(job);

    // process the results
    processExecutionResults(results);
  }

  /**
   * Execute a job in non-blocking mode. The application has the responsibility
   * for handling the notification of job completion and collecting the results.
   * @param job the JPPF job to execute.
   * @throws Exception if an error occurs while executing the job.
   */
  public void executeNonBlockingJob(final JPPFJob job) throws Exception {
    // set the job in non-blocking (or asynchronous) mode.
    job.setBlocking(false);

    // this call returns immediately. We will use the collector at a later time
    // to obtain the execution results asynchronously
    JPPFResultCollector collector = submitNonBlockingJob(job);

    // the non-blocking job execution is asynchronous, we can do anything else in the meantime
    System.out.println("Doing something while the job is executing ...");
    // ...

    // We are now ready to get the results of the job execution.
    // We use JPPFResultCollector.waitForResults() for this. This method returns immediately with
    // the results if the job has completed, otherwise it waits until the job execution is complete.
    List<JPPFTask> results = collector.waitForResults();

    // process the results
    processExecutionResults(results);
  }

  /**
   * Execute a job in non-blocking mode. The application has the responsibility
   * for handling the notification of job completion and collecting the results.
   * @param job the JPPF job to execute.
   * @return a JPPFResultCollector used to obtain the execution results at a later time.
   * @throws Exception if an error occurs while executing the job.
   */
  public JPPFResultCollector submitNonBlockingJob(final JPPFJob job) throws Exception {
    // set the job in non-blocking (or asynchronous) mode.
    job.setBlocking(false);

    // We need to be notified of when the job execution has completed.
    // To this effect, we define an instance of the TaskResultListener interface,
    // which we will register with the job.
    // Here, we use an instance of JPPFResultCollector, conveniently provided by the JPPF API.
    // JPPFResultCollector implements TaskResultListener and has a constructor that takes
    // the number of tasks in the job as a parameter.
    JPPFResultCollector collector = new JPPFResultCollector(job);
    job.setResultListener(collector);

    // Submit the job. This call returns immediately without waiting for the execution of
    // the job to complete. As a consequence, the object returned for a non-blocking job is
    // always null. Note that we are calling the exact same method as in the blocking case.
    System.out.println("probando");
    jppfClient.submit(job);

    // finally return the result collector, so it can be used to collect the exeuction results
    // at a time of our chosing. The collector can also be obtained at any time by calling 
    // (JPPFResultCollector) job.getResultListener()
    return collector;
  }

  /**
   * Process the execution results of each submitted task. 
   * @param results the tasks results after execution on the grid.
   */
  public void processExecutionResults(final List<JPPFTask> results) {
    // process the results
    for (JPPFTask task: results) {
      // if the task execution resulted in an exception
      if (task.getException() != null) {
        // process the exception here ...
          System.out.println( "Exception: " + task.getException() ) ;
      }
      else {
        // process the result here ...
          ArrayList res = (ArrayList) task.getResult();
          resultados.add( res );
          System.out.println( res.get(5) ) ;
      }
    }
  }

  /**
   * Print a message to the console and/or log file.
   * @param message - the message to print.
   */
  private static void output(final String message)
  {
    System.out.println(message);
    log.info(message);
  }
  
  
}
