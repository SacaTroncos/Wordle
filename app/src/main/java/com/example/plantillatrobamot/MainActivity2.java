package com.example.plantillatrobamot;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Iterator;

public class MainActivity2 extends AppCompatActivity {

    private static final String EXTRA_MESSAGE = "android.intent.extra.alarm.MESSAGE";
    private TextView textView;
    private TextView titulo;
    private TextView palabrasSolucion;

    private TextView restricciones;

    private TextView palabraTitulo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate ( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textView = findViewById(R.id.myTextView);
        Intent intent = getIntent();
        String definicion = intent.getStringExtra("DEFINICION");
        textView.setText(Html.fromHtml(definicion));

        titulo = findViewById(R.id.TITULO);
        String titul = intent.getStringExtra("TITULO");
        titulo.setText(titul);

        palabraTitulo = findViewById(R.id.PALABRATITULO);
        String pala = intent.getStringExtra("PALABRATITULO");
        pala = pala.toUpperCase();
        palabraTitulo.setText(pala);

        palabrasSolucion = findViewById(R.id.PALABRAS);
        String palabras = intent.getStringExtra("SOLUCIONES");
        palabrasSolucion.setText(palabras);

        restricciones = findViewById(R.id.RESTRICCIONES);
        String restic = intent.getStringExtra("RESTRICCIONES");
        restricciones.setText(restic);

    }
}