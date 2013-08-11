/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algoritmo.binario;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import algoritmo.binario.*;

/**
 *
 * @author uzielgl
 */
public class AGBinario2 {

    static int tamPob;
    static int generaciones;
    static float pCruza;
    static float pMutacion;
    static List mejoresResultados = new ArrayList();

    
    static public double algoritmo(int poblacion, int generaciones, float pCruza, float pMutacion){
        Poblacion p = new Poblacion(poblacion);
        p.evaluacion(p.getPoblacion());
        int evaluaciones = poblacion;
        int gen = 0;
        //System.out.println("Gen: "+gen);
        //System.out.println(p.obtenerMejorIndividuo());
        Individuo mejorIndividuo = new Individuo();
        while(evaluaciones < generaciones *  poblacion){
            gen++;
            p.seleccionarPadres();
            p.cruzarPadres(pCruza);
            p.mutacionHijos(pMutacion);
            p.evaluacion(p.getHijos());
            evaluaciones += poblacion;
            p.reemplazo();
            mejorIndividuo = p.obtenerMejorIndividuo();

        }
        mejoresResultados.add(mejorIndividuo);
        
        return (double) mejorIndividuo.getAptitud();
        
    }
}
