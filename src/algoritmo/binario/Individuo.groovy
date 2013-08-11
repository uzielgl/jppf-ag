/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package algoritmo.binario

/**
 *
 * @author uzielgl
 */
class Individuo implements Cloneable {
    def valores = [] //Aqui estan las x: []
    def dimensiones = 10
    def String binario = "" //Cadena de binarios
    def aptitud = 0
   
    //Para representacion binaria
    def sup = 5.12
    def inf = -5.12
    def tamCad = 14
    
    def valorEsperado = 0
    
    def Random ran = new Random();
    
    public Object clone() {

        return super.clone();

    }
    

    public Individuo(){
        valores = []
        for ( i in 0..<dimensiones)
            valores << Math.round( (ran.nextFloat() * ( sup - inf )  + inf) * 1000 ) /1000  // /
        actualizarAptitud()
        crearRepresentacionBinaria()
    }
    
    /** Que actualizara y previo la calculara aptitud*/
    def actualizarAptitud(){
        aptitud = 10 * dimensiones + valores.collect{ it ** 2 - (10 * Math.cos(2* Math.PI * it))}.sum()
        //println Math.PI
    }
    
    def getAptitud(){
        return aptitud
    }
    
    def void setBinario(String binario){
        this.binario = binario
        crearRepresentacionReal()
    }
    def String getBinario(){
        return binario
    }
    /** Crea y actualiza la representacion binaria en base a this.valores*/
    def void crearRepresentacionBinaria(){
        def entero;
        def total_ceros
        def tmp_binario
        binario = ""
        
        for ( i in valores){
            entero = (int) ( ( i - inf ) * ( 2 ** tamCad ) ) / ( sup - inf )
            tmp_binario = Integer.toBinaryString(entero)
            //println// "de entero a binario: "+tmp_binario.length()
            if (tamCad > tmp_binario.length()){
                binario += "0" * ( tamCad - tmp_binario.length() ) + tmp_binario
            }
            else {
                binario += tmp_binario
            }
        }
    }
    /** Crea y actualiza la representacion real [12.5,232.3] en base a 
     * this.binario*/
    def String crearRepresentacionReal(){
        valores = []
        for ( i in (0..<tamCad*dimensiones).step(tamCad) ){//0 15 30 45  150
            def bits15 = binario[i..<i+tamCad]
            valores << Math.round(obtenerReal( bits15 ) * 1000) / 1000;
        }
    }
    /* Recibe un string de 15 caracteres y lo convierte a su representacion real*/
    def float obtenerReal( String binario ){
        def ent = Integer.parseInt(binario, 2);
        return inf + ( ( ent * ( sup - inf ) ) / ( 2 ** tamCad - 1 ) )
    }
    
    def mutar(probabilidad ){
        def nuevo_binario = "";
        for( i in binario){ 
            if( ran.nextFloat() <= probabilidad  ){
                nuevo_binario +=  i.equals( "1" ) ? "0" : "1"
            }
            else{
                nuevo_binario += i
            }
        }
        binario = nuevo_binario
        this.crearRepresentacionReal()
    }
    
    def ArrayList<Individuo> cruzar( Individuo otroIndividuo ){
        def limCad = dimensiones * tamCad
        def k = ran.nextInt( limCad ) - 1
        def a = binario
        def b = otroIndividuo.getBinario()
        //println "binario mama: "+a.length();
        //println "binario papa: "+b.length();
        def hijo1 = a[0..k] + b[k+1..<limCad]
        def hijo2 = b[0..k] + a[k+1..<limCad]
        
        def Individuo i1 = new Individuo()
        def Individuo i2 = new Individuo()
        
        i1.setBinario( hijo1 )
        i2.setBinario( hijo2 )
       
        return [i1, i2]
    }
    
    public String toString(){
        return valores.toString() + " f(x) = " + Math.round( aptitud * 1000 ) / 1000
    }
    
    
    
}
