package com.example.plantillatrobamot;

import static java.lang.String.valueOf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
//INTEGRANTES DEL GRUPO
//SERGI OLIVER JUÁREZ
//ALEJANDRO MORENO
//ANGEL JIMÉNEZ SANCHIS
public class MainActivity extends AppCompatActivity {

    // Declaración Atributos:
    private int contadorPalabrasDiccionario = 0;
    private int contadorIntentos = 1;
    private final int lengthWord = 5;
    private final int maxTry = 6;

    private boolean victoria = false;
    private String palabraEnviada;

    // Para imprimir las soluciones Posibles en Pantalla:
    private TextView textSoluciones;
    private boolean existente = false;

    // Declaramos la palabra correcta para el juego:
    private String palabraSolucion;
    private String palabraSolucionAcentos;

    private UnsortedArrayMapping<String, UnsortedLinkedListSet> tecladoMapping = new UnsortedArrayMapping(27);

    // Declaramos e instanciamos un HashMapping para la creación de nuestro diccionario:
    private HashMap<String, String> diccionarioHash = new HashMap();

    //Declaramos e instanciamos un BSTMapping para guardar las restricciones
    private BSTMapping<String, UnsortedLinkedListSet> restriccionesMapping = new BSTMapping();

    // Declaración del Iterator para el UnsortedArrayMapping:
    private Iterator iteratorTeclado = tecladoMapping.IteratorUnsortedArrayMapping();

    //Declaración Iterator para el BSTMapping:
    private Button[] letrasButton;
    private Iterator iteratorHash;

    // Contenedor donde se mostraran la graella y letras
    private ConstraintLayout constraintLayout;

    // Indica la siguiente casilla en la que escribiremos:
    private int casillaActual = 0;

    // Indica el color de los bordes de las casillas:
    private GradientDrawable gd;

    // Definimos los colores:
    public static String grayColor = "#D9E1E8";
    public static String graySolutions = "#B2B2B2";
    public static String selectedYellow = "#F8ED62";

    // Servirá para la funcionalidad del botón Enviar:
    private int contadorEnviar = 0;

    private int widthDisplay;
    private int heightDisplay;

    // Arbol RojoNegro que guarda las soluciones posibles
    // Se copia el diccionario entero en el arbol
    // Luego se va actualizando el arbol segun las restricciones que vayamos teniendo
    // Asi con cada restriccion se va reduciendo el numero de soluciones posibles
    private TreeSet<String> solucionesPosibles;

    private Iterator itSoluciones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Object to store display information
        DisplayMetrics metrics = new DisplayMetrics();

        // Get display information
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        widthDisplay = metrics.widthPixels;
        heightDisplay = metrics.heightPixels;

        //Genereamos nuestro diccionario segun la longitud de letras
        crearDiccionario();

        //Inicializamos el arbolRojoNegro
        InicializarArbolRojoNegro();

        //Creamos toda la interficie grafica
        crearInterficie();

    }

    @Override
    protected void onStart() {
        super.onStart();
        hideSystemUI();
    }

    private void crearInterficie() {
        // Inicializamos el Mapping con su respectiva letra:
        inicializarMapping();

        // Creamos los botones de Enviar y Borrar con su funcionalidad:
        botonesIniciales();

        //creamos la graella "segun la longitud que nos indique el usuario"(aun no implementado)
        crearGraella();

        // Creamos el teclado:
        crearTeclat();
        imprimiLista();


    }

    public Button crearBotones(String nameButton, int x, int y) {

        int buttonHeight = heightDisplay / 16;
        int buttonWidth = widthDisplay / 3;

        Button b = new Button(this);
        b.setText(nameButton);

        constraintLayout = findViewById(R.id.layout);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        params.height = buttonHeight;
        params.width = buttonWidth;

        b.setLayoutParams(params);
        b.setY(y);
        b.setX(x);

        // Establecer el tamaño del texto para que se ajuste al tamaño del botón:
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 3);

        return b;
    }

    private void botonesIniciales() {

        // Creamos los botones de Enviar y Borrar:
        Button butEnviar = crearBotones("ENVIA", widthDisplay / 6, heightDisplay - heightDisplay / 5);
        Button butBorrar = crearBotones("ESBORRA", (widthDisplay / 6) + widthDisplay / 3, heightDisplay - heightDisplay / 5);

        // Se añaden los botones al Layout:
        constraintLayout.addView(butEnviar);
        constraintLayout.addView(butBorrar);

        // Crear el OnClickListener
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int duration = Toast.LENGTH_LONG;
                Context context = getApplicationContext();
                CharSequence text;
                Toast toast;
                if (view == butEnviar) {
                    //Si hemos escrito todas las csillas podemos enviar
                    if (contadorEnviar == lengthWord && comprobarPalabraValida()) {
                        contadorEnviar = 0;
                        restricciones();
                        //Si llegamos a la ultima fila no se muestra la siguiente casilla seleccionada
                        if (casillaActual <= (lengthWord * maxTry) - 1) {
                            // Marcamos la casillaActual:
                            casillaSeleccionada(findViewById(casillaActual));
                        }


                        if (contadorIntentos == maxTry) {
                            pantallaFinal();
                            text = "Final de joc";
                            toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }

                        contadorIntentos++;

                    } else {

                        // Si no hemos escrito todas las casillas mostramos error
                        //MENSAJE ERROR (al no tener la palabra completa)
                        text = "Paraula Incompleta!";
                        if (contadorEnviar == lengthWord && !comprobarPalabraValida()) {
                            text = "Paraula no existent!";
                        }
                        toast = Toast.makeText(context, text, duration);
                        toast.show();

                        // FALTA TRATAR EL ERROR SI LA PALABRA ENVIADA NO EXISTE
                        // EN EL DICCIONARIO QUE MUESTRE OTRO ERROR:
                    }

                } else if (view == butBorrar) {
                    if (contadorEnviar != 0) {
                        //Si llegamos a la ultima fila no se pone en gris la anterior
                        if (casillaActual <= (lengthWord * maxTry) - 1) {
                            // Volvemos gris la casilla:
                            findViewById(casillaActual).setBackground(gd);
                        }
                        //se resta el contador de casillas ocupadas:
                        contadorEnviar--;
                        //se resta la casilla en la que estamos:
                        casillaActual--;
                        // Se borra la letra de la casilla:
                        ((TextView) findViewById(casillaActual)).setText("");
                        // Marcamos la casillaActual:
                        casillaSeleccionada(findViewById(casillaActual));
                    }
                }
            }
        };

        // Asignar el OnClickListener a ambos botones
        butBorrar.setOnClickListener(onClickListener);
        butEnviar.setOnClickListener(onClickListener);

    }

    private void inicializarMapping() {
        UnsortedLinkedListSet listaÇ = new UnsortedLinkedListSet();

        char lletraTeclado = 'A';

        // Creamos el Mapping
        for (int i = 0; i <= ('Z' - 'A'); i++) {
            //Creamos una lista para cada una de las letras
            UnsortedLinkedListSet listaPosicionesLetras = new UnsortedLinkedListSet();


            //Recorremos letra a letra la palabraSolucion
            for (int j = 0; j < lengthWord; j++) {

                char letra = palabraSolucion.charAt(j);

                // Convertimos la letra teclado a minúscula para hacer su comparación correctamente:
                letra = (char) (letra - ('a' - 'A'));

                //Si la letra de la palabra coincide con la letra que se va a insertar en el diccionario
                //se agrega la posicion a la lista
                if (letra == lletraTeclado) {
                    listaPosicionesLetras.add(j);
                } else if (letra == 'Ç') {
                    listaÇ.add(j);
                }
            }
            //Asignamos el caracter y la lista de posiciones que aparece en la palabra solucion
            tecladoMapping.put(valueOf(lletraTeclado), listaPosicionesLetras);
            lletraTeclado++;
        }

        // Añadimos la 'Ç':
        tecladoMapping.put(valueOf('Ç'), listaÇ);

    }

    private void imprimiLista() {

        System.out.println("Palabra a Adivinar:" + palabraSolucion);

        Iterator iteratorTeclado = tecladoMapping.IteratorUnsortedArrayMapping();
        while (iteratorTeclado.hasNext()) {
            String letras = (String) iteratorTeclado.next();
            Iterator iteratortLista = (tecladoMapping.get(letras).IteratorUnsortedLinkedList());
        }
    }

    private void casillaSeleccionada(TextView tView) {

        // Definir les característiques del "pinzell"
        GradientDrawable colorBorde = new GradientDrawable();
        colorBorde.setCornerRadius((widthDisplay / 10) / 5);
        colorBorde.setStroke(heightDisplay / 200, Color.parseColor(selectedYellow));

        // Seleccionar la nueva casilla:
        tView.setBackground(colorBorde);

        // Guardamos el identificador de la casilla:
        casillaActual = tView.getId();
    }

    private void crearGraella() {

        int contador = 0;
        int tamañoCasilla = widthDisplay / 10;

        // Definir les característiques del "pinzell"
        gd = new GradientDrawable();
        gd.setCornerRadius(tamañoCasilla / 5);
        gd.setStroke(heightDisplay / 200, Color.parseColor(grayColor));

        // Establecemos el identificador de las casillas de la graella:
        int id = 0;

        // Obtenim el layout:
        int posX = (widthDisplay / lengthWord) + tamañoCasilla / 3;
        int posY = heightDisplay / maxTry;

        for (int i = 0; i < (lengthWord * maxTry); i++) {
            // Crear un TextView:
            TextView textViewCasillas = new TextView(this);
            textViewCasillas.setText("");
            textViewCasillas.setBackground(gd);
            textViewCasillas.setId(id);
            id++;

            textViewCasillas.setWidth(tamañoCasilla);
            textViewCasillas.setHeight(tamañoCasilla);

            // Posicionam el TextView
            textViewCasillas.setX(posX);
            textViewCasillas.setY(posY);
            textViewCasillas.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            textViewCasillas.setTextColor(Color.BLACK);
            textViewCasillas.setTextSize(tamañoCasilla / 5);

            // Incrementam la posició de les caselles:
            posX += tamañoCasilla + (widthDisplay / 100);
            contador++;
            if (contador == lengthWord) {
                posX = (widthDisplay / lengthWord) + tamañoCasilla / 3;
                posY += tamañoCasilla + (heightDisplay / 100);
                contador = 0;
            }

            // Afegir el TextView al layout
            constraintLayout.addView(textViewCasillas);
            casillaSeleccionada(findViewById(casillaActual));
        }
    }

    private void crearTeclat() {
        //posicion inicial del teclado
        int initPosX = (widthDisplay / 11);
        int initPosY = (heightDisplay / 2) + (widthDisplay / 4);
        //Contador de letras para saber cuando bajar de fila
        int contador = 0;

        // Establecemos el identificador de las casillas del teclado:
        int i = 0;

        //Creamos un boton para cada letra del mapping
        letrasButton = new Button[27];

        //Bucle que recorre el Mapping (hasta que no encuentre elementos)
        while (iteratorTeclado.hasNext()) {

            letrasButton[i] = new Button(this);

            //Asignamos el nombre del boton
            letrasButton[i].setText(iteratorTeclado.next().toString());

            // Ancho y largo del el botón:
            int buttonWidthHeight = widthDisplay / 10;

            //Tratamiento del Layout
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params.height = buttonWidthHeight;
            params.width = buttonWidthHeight;

            letrasButton[i].setLayoutParams(params);
            letrasButton[i].setX(initPosX);
            letrasButton[i].setY(initPosY);
            letrasButton[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonWidthHeight / 3);

            // Incrementam la posició de les caselles:
            initPosX += widthDisplay / 11;
            contador++;
            //Si contador es igual a 9 se baja una fila
            if (contador == 9) {
                initPosX = (widthDisplay / 11);
                contador = 0;
                initPosY += buttonWidthHeight;
            }
            // Afegir el botó al Layout:
            constraintLayout.addView(letrasButton[i]);

            letrasButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (contadorEnviar != lengthWord) {
                        // Obtener la letra del botón pulsado:
                        String letra = ((Button) view).getText().toString();

                        // Buscar la casilla correspondiente:
                        TextView casilla = findViewById(casillaActual);

                        // Asignar la letra a la casilla y deseleccionar la casilla actual:
                        casilla.setText(letra);

                        // Establecer el tamaño del texto para que se ajuste al tamaño del botón
                        casilla.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonWidthHeight / 2);

                        casilla.setBackground(gd);

                        // Actualizar la casilla actual:
                        casillaActual++;
                        if (contadorEnviar != (lengthWord - 1)) {
                            // Seleccionar la nueva casilla actual:
                            casillaSeleccionada(findViewById(casillaActual));
                        }
                        contadorEnviar++;
                    }
                }
            });

            // Incrementamos el i:
            i++;
        }
    }

    private void actualizarTeclado() {
        //Iterador que recorre el mapping
        Iterator itRestricciones = restriccionesMapping.IteratorBSTMapping();

        //Bucle que va mirando las restricciones una a una
        while (itRestricciones.hasNext()) {
            //Obtenemos la restriccion
            BSTMapping.Pair restriccion = (BSTMapping.Pair) itRestricciones.next();
            //Obtenemos la letra de la restriccion
            String letra = restriccion.getKey().toString();
            //Obtenemos la lista de restricciones de la letra
            UnsortedLinkedListSet listaRestriccion = (UnsortedLinkedListSet) restriccion.getValue();

            //Sabemos por ejemplo que si la letra es la "A" hay que cambiar el color de letrasButton[0]
            //Asi que a traves de letra lo convertimos a char y sacamos su valor numerico
            int posLetra = letra.charAt(0) - 'A';

            //Ahora cambiamos el color de la letra en el teclado segun la lista de restricciones
            if (listaRestriccion.isEmpty())
                letrasButton[posLetra].setTextColor(Color.RED);
            else
                letrasButton[posLetra].setTextColor(listaRestriccion.contains(-1) ? Color.YELLOW : Color.GREEN);

        }
    }


    private boolean comprobarPalabraValida() {

        palabraEnviada = "";

        //Recorre
        for (int i = (casillaActual - lengthWord); i < (casillaActual); i++) {
            palabraEnviada += ((TextView) findViewById(i)).getText();
        }
        //pasamos la palabra a minuscula
        palabraEnviada = palabraEnviada.toLowerCase();

        //Verificamos si la palabra existe en el diccionario
        return diccionarioHash.containsKey(palabraEnviada);
    }

    private void crearDiccionario() {
        InputStream is = getResources().openRawResource(R.raw.paraules);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));

        String lineaDicc;

        try {
            //Lee la linea, hasta encontrar el final del fichero.
            while ((lineaDicc = r.readLine()) != null) {
                String[] splitParts = lineaDicc.split(";");
                //Sepramos las palabras
                String parteAcentos = splitParts[0];
                String parteCorrecta = splitParts[1];

                // La palabra existe en el diccionario:
                if (parteCorrecta.length() == lengthWord) {
                    //Se añade al árbol:
                    diccionarioHash.put(parteCorrecta, parteAcentos);
                    contadorPalabrasDiccionario++;
                }
            }
            //Cerramos el fichero
            is.close();
            r.close();

        } catch (IOException e) {
            System.err.println("--- Error en la lectura de la palabra ---");
        }
    }

    private void restricciones() {
        //Para asignar color de las casillas
        int casillasRepaint = (casillaActual - lengthWord);

        // Primero comprobamos que la palabra no sea la solución:
        if (palabraEnviada.equals(palabraSolucion)) {
            //Recorremos letra a letra la palabraEnviada
            while (casillasRepaint < casillaActual) {
                pintarCasilla(casillasRepaint, Color.GREEN);
                casillasRepaint++;
            }
            victoria = true;
            pantallaFinal();
        } else {
            //Recorremos letra a letra la palabraEnviada
            for (int j = 0; j < palabraEnviada.length(); j++) {

                // Obtener la letra de la palabra enviada en la posición j
                String letraString = Character.toString(palabraEnviada.toUpperCase().charAt(j));
                Iterator iteratorLista = (tecladoMapping.get(letraString).IteratorUnsortedLinkedList());

                UnsortedLinkedListSet lista = restriccionesMapping.get(letraString);
                if (lista == null) {
                    lista = new UnsortedLinkedListSet();
                }

                int color;

                //Si no hay mas letras en la palabraSolucion significa que la letra no esta en la palabra
                //asi que es roja
                if (!iteratorLista.hasNext()) {
                    color = Color.RED;
                } else {
                    //Verde
                    if (tecladoMapping.get(letraString).contains(j)) {
                        color = Color.GREEN;
                        if (!lista.isEmpty() && lista.contains(-1)) {
                            lista.remove(-1);
                        }
                        lista.add(j);
                    }
                    //Amarillo
                    else {
                        color = Color.YELLOW;
                        if (lista.isEmpty())
                            lista.add(-1);

                    }
                }
                //añadimos la letra a las restricciones
                restriccionesMapping.put(letraString, lista);

                //Pintamos la casilla
                pintarCasilla(casillasRepaint, color);
                casillasRepaint++;
            }

            //Actualizar las soluciones posibles
            actualizarArbolRojoNegro();
        }
    }

    private void pintarCasilla(int casillaPos, int color) {
        GradientDrawable newGd = new GradientDrawable();
        TextView casilla = findViewById(casillaPos);

        newGd.setColor(color);
        casilla.setBackground(newGd);
    }

    /*
     * Método: InicializarArbolRojoNegro
     * Descripción: Inicializa el arbolRojoNegro con el diccionario
     * Entrada: -
     * Salida: -
     * Postcondiciones: El arbolRojoNegro queda inicializado
     */
    private void InicializarArbolRojoNegro() {

        //Numero random entre 0 y el numero de palabras del diccionario
        //(Nuestro diccionario hash empieza desde la posicion 1, por ello le sumamos 1)
        int n = (int) (Math.random() * (contadorPalabrasDiccionario - 1));
        int contador = 0;

        //Creamos el arbolRojoNegro
        solucionesPosibles = new TreeSet<>();

        Set<Map.Entry<String, String>> setIteratorhash = diccionarioHash.entrySet();
        iteratorHash = setIteratorhash.iterator();

        //Copiamos el diccionario en el arbolRojoNegro;
        while (iteratorHash.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iteratorHash.next();

            if (n == contador) {
                //cuando se encuentra la posicion
                //Numero random entre 0 y el numero de palabras del diccionario
                palabraSolucion = entry.getKey();
                palabraSolucionAcentos = entry.getValue();
            }
            // Añadimos las palabras del diccionarioHash:
            solucionesPosibles.add(entry.getKey());
            contador++;
        }
        imprimirSolucionesPosibles();
    }

    // Con el BTSMapping que contiene las restricciones hay que
    // ir eliminando las palabras que no cumplan las restricciones
    // Con el BTSMapping que contiene las restricciones hay que
    // ir eliminando las palabras que no cumplan las restricciones
    private void actualizarArbolRojoNegro() {

        //Creamos un arbol nuevo
        TreeSet<String> solucionesPosiblesAux = new TreeSet<>();
        //Boolean que indica si la palabra cumple las restricciones y se añade al arbol o no
        boolean cumpleRestricciones;


        itSoluciones = solucionesPosibles.iterator();

        //Recorremos arbol rojos y negros
        while (itSoluciones.hasNext()) {
            //reseteamos el boolean
            cumpleRestricciones = true;

            // Recorremos el arbol
            String palabra = (String) itSoluciones.next();
            palabra = palabra.toUpperCase();
            Iterator itRestricciones = restriccionesMapping.IteratorBSTMapping();

            //Miramos si la palabra cumple las restricciones
            while (itRestricciones.hasNext()) {
                BSTMapping.Pair restriccion = (BSTMapping.Pair) itRestricciones.next();
                UnsortedLinkedListSet listaRestriccion = (UnsortedLinkedListSet) restriccion.getValue();
                String llave = restriccion.getKey().toString();

                //Restriccion que la letra no puede estar en la palabra
                // comprobar si la letra es "ROJA"
                if (listaRestriccion.isEmpty()) {
                    if (palabra.contains(llave)) {
                        //No se añade al nuevo diccionario
                        cumpleRestricciones = false;
                        break;
                    }

                }
                //Restriccion que la letra esta en la palabra pero no sabemos donde
                //Comprobar si la letra es "AMARILLA"
                else {
                    if (listaRestriccion.contains(-1)) {
                        if (!palabra.contains(llave)) {
                            //No se añade al nuevo diccionario
                            cumpleRestricciones = false;
                            break;
                        }
                    } else {
                        //Hay que hacer un bucle recorriendo la palabra y comprobando si la letra esta en la posicion
                        //Si es verde mirar que se tiene la letra en la restriccion
                        Iterator itLista = listaRestriccion.IteratorUnsortedLinkedList();
                        while (itLista.hasNext()) {
                            int posicion = (int) itLista.next();
                            if (palabra.charAt(posicion) != llave.charAt(0)) {
                                //Se va a tomar por culo
                                cumpleRestricciones = false;
                                break;
                            }
                        }
                    }
                }
            }
            //Añadir la palabra si no tiene restricciones
            if (cumpleRestricciones) solucionesPosiblesAux.add(palabra);

        }
        solucionesPosibles = solucionesPosiblesAux;
        imprimirSolucionesPosibles();
        actualizarTeclado();
    }

    private void imprimirSolucionesPosibles() {

        // Verificar si ya existe un TextView en el constraintLayout
        if (!existente) {
            // No hay TextView existente, crear uno nuevo
            textSoluciones = new TextView(this);

            constraintLayout = findViewById(R.id.layout);
            constraintLayout.addView(textSoluciones);

            existente = true;
        }

        // Actualizar el contenido del TextView
        String solucionesP = "SOLUCIONES POSIBLES: " + solucionesPosibles.size();
        textSoluciones.setText(solucionesP);

        // Resto del código para configurar el TextView
        int tamaño = widthDisplay / 20;
        int posX = widthDisplay / 5;
        int posY = heightDisplay / 2 + tamaño*3 + tamaño/2;

        textSoluciones.setWidth(tamaño * 12);
        textSoluciones.setHeight(tamaño);

        // Posicionamiento del TextView
        textSoluciones.setX(posX);
        textSoluciones.setY(posY);
        textSoluciones.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textSoluciones.setTextColor(Color.parseColor(graySolutions));
        textSoluciones.setTextSize(tamaño / 4);
    }

    private void pantallaFinal() {
        Intent intent = new Intent(this, MainActivity2.class);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = agafaHTML();
                    intent.putExtra("DEFINICION", message);

                    //Si ha ganado solo se mostara la definicio, la palabra  y el titulo
                    if (victoria) {
                        //Mensaje si gana
                        intent.putExtra("TITULO", "ENHORABONA!!!");
                    } else {
                        //Mensaje si pierde
                        intent.putExtra("TITULO", "Oh no! Has perdut...");

                        itSoluciones = solucionesPosibles.iterator();
                        String solucion = " Posibles Solucions: ";
                        int contador = 0;
                        while (itSoluciones.hasNext()) {
                            solucion += itSoluciones.next().toString().toLowerCase();
                            solucion += itSoluciones.hasNext() ? ", " : ".";
                            contador++;
                            if(contador == 100){
                                solucion += "...";
                                break;
                            }
                        }

                        //Recorremos el mapping de restricciones y añadimos el texto correspondiente
                        //para cada resticcion
                        String textoRestricciones = "Restricciones: \n";
                        Iterator itRestricciones = restriccionesMapping.IteratorBSTMapping();
                        while (itRestricciones.hasNext()) {
                            BSTMapping.Pair restriccion = (BSTMapping.Pair) itRestricciones.next();
                            String letraString = (String) restriccion.getKey();
                            UnsortedLinkedListSet listaRestriccion = (UnsortedLinkedListSet) restriccion.getValue();

                            //Rojo
                            if (listaRestriccion.isEmpty()) {
                                textoRestricciones += "No ha de contenir la " + letraString + ". \n";
                            } else {
                                //Amarillo
                                if (listaRestriccion.contains(-1)) {
                                    textoRestricciones += "Ha de contenir la " + letraString + ". \n";
                                }
                                //Verde
                                else {
                                    Iterator itLista = listaRestriccion.IteratorUnsortedLinkedList();
                                    while (itLista.hasNext()) {
                                        textoRestricciones += "Ha de contenir la " + letraString + " a la posicio " + itLista.next() + ". \n";
                                    }
                                }
                            }

                        }
                        intent.putExtra("SOLUCIONES", solucion);
                        intent.putExtra("RESTRICCIONES", textoRestricciones);
                    }
                    intent.putExtra("PALABRATITULO",palabraSolucionAcentos);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public String agafaHTML() {
        String definicion = "";

        //La definicio de la paraula que volem consultar la trobarem a l’adreça:
        try {
            URL url = new URL("https://www.vilaweb.cat/paraulogic/?diec=" + palabraSolucionAcentos);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuffer stringBuffer = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
            reader.close();

            // Asignar el contenido HTML a la variable definicion
            String json = stringBuffer.toString();

            // Verificar si el JSON tiene definicion mirando si empieza por [
            if (json.startsWith("[")) {
                // El JSON es un arreglo, no hay definición disponible
                return "No hay definicion de la palabra " + palabraSolucionAcentos;
            }
            // Crear el objeto JSONObject y extraer la definición del JSON
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("d")) {
                definicion = jsonObject.getString("d");
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return definicion;
    }


    //NO TOCAR
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE  // no posar amb notch
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}