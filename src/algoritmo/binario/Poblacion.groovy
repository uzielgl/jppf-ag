/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package algoritmo.binario

/**
 *
 * @author uzielgl
 */
class Poblacion {
    def ArrayList<Individuo> poblacion = []
    def ArrayList<Individuo> padres = []
    def ArrayList<Individuo> hijos = []
    def Random ran = new Random();
    
    public Poblacion( tam ){
        for ( i in (0..<tam )){
            poblacion << new Individuo()
        }
    }
    
    public seleccionarPadres(){
        def aptitudesNegativas = poblacion.collect{ it.getAptitud() * -1 }
        def minimo = aptitudesNegativas.min()
        def valorASumar = Math.abs( minimo ) * 2
        
        def aptitudesParaMinimizar = aptitudesNegativas.collect{ it + valorASumar }
        
        //Para los valores esperados
        def promedioAptitudes = aptitudesParaMinimizar.sum() / poblacion.size() ///
        
        def valoresEsperados = aptitudesParaMinimizar.collect{ it / promedioAptitudes }
        
        for ( i in 0..<poblacion.size() )
            poblacion[i].valorEsperado = valoresEsperados[i]
        
        //Individuos 
        def individuos1 = poblacion.findAll{ it.valorEsperado >= 1 }
        def tmp_individuos = individuos1.clone()
        
        for (int i=0; i<tmp_individuos.size(); i++){
             if( (int) tmp_individuos[i].valorEsperado >= 2){
                 int padresRepetidos = (int) tmp_individuos[i].valorEsperado - 1
                 for (int j=1; j<padresRepetidos; j++){
                     if( individuos1.size() == poblacion.size() ) break
                     individuos1 << tmp_individuos[i].clone()
                 }
             }
        }

        def individuos2 = poblacion.clone();
        for ( i in individuos2)
            i.valorEsperado = i.valorEsperado - (int) i.valorEsperado
        individuos2.sort{ it.valorEsperado }
        
        for ( def j = individuos2.size()-1 ; j > 0 ; j--){
            if( individuos1.size() == poblacion.size() ) break
            individuos1 << individuos2[j].clone()
        }
        
        padres = individuos1.clone()
        //println padres.size()
    }
    
    public cruzarPadres(probabilidad){
        def ArrayList<Individuo> nuevosHijos = []
        def Individuo mama;
        def Individuo papa;
        for (int i=0; i<padres.size(); i+=2 ){
            mama = padres[i].clone();
            if (i != padres.size()) { 
                papa = padres[i+1].clone()
            }
            else{
                papa = padres[ran.nexInt(padres.size())].clone()
            }
            if (ran.nextFloat() <= probabilidad){
                ArrayList<Individuo> hijosCruza = mama.cruzar(papa)
                nuevosHijos << hijosCruza[0].clone()
                nuevosHijos << hijosCruza[1].clone()
            }
            else{
                nuevosHijos << mama.clone();
                nuevosHijos << papa.clone();
            }
        }
        hijos = nuevosHijos.clone()
    }
    
    
    public mutacionHijos(probabilidad){
        for(ind in hijos){
            if (ran.nextFloat() <= probabilidad){
                ind.mutar(probabilidad)
            }
        }
    }
    
    public reemplazo(){
        def ArrayList<Individuo> nuevaPoblacion = []
        poblacion.sort{ it.aptitud }
        Individuo mejorIndividuo = poblacion.get(0).clone()
        nuevaPoblacion << mejorIndividuo.clone()
        for (ind in hijos){
            if (nuevaPoblacion.size() < poblacion.size()){
                nuevaPoblacion << ind.clone()
            }
        }
        poblacion = nuevaPoblacion.clone()
    }
    
    public void evaluacion(poblacion){
        for (ind in poblacion){
            ind.actualizarAptitud();
        }
    }
    
    public Individuo obtenerMejorIndividuo(){
        poblacion.sort{ it.aptitud }
        Individuo mejorIndividuo = poblacion.get(0);
        return mejorIndividuo
    }
    
    
 /*   public static void main( String[] args ){
        Poblacion p = new Poblacion(100)
        p.seleccionarPadres()
        p.cruzarPadres()
        p.mutacionHijos()
    }*/
    
}



