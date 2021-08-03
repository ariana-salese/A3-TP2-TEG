package edu.fiuba.algo3.interfaz;


import edu.fiuba.algo3.App;
import edu.fiuba.algo3.interfaz.fases.Fase;
import edu.fiuba.algo3.interfaz.fases.colocacion.Inicial;
import edu.fiuba.algo3.modelo.Juego;
import edu.fiuba.algo3.modelo.Jugador;
import edu.fiuba.algo3.modelo.Pais;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.ArrayList;

public class ControladorPantallaJuego {

    private Juego juego;
    private Scene scene;
    private Fase fase;
    private enum EjeCambioEscala {
        HORIZONTAL, VERTICAL, NINGUNO;
    }

    public ControladorPantallaJuego(Scene scene, Juego juego) throws IOException {
        this.scene = scene;
        this.juego = juego;

        //FIXME - Estas 3 lineas se repiten en varios lados, hacer una funcion
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("VistaPantallaJuego.fxml"));
        fxmlLoader.setController(this);
        scene.setRoot(fxmlLoader.load());
        this.fase = new Inicial(juego, scene);
        scene.lookup("#slider").setManaged(false);

        mostrarTabJugadores();

        inicializarAjusteEscala();
    }

    private void mostrarTabJugadores(){
        HBox caja = (HBox) scene.lookup("#tabJugadores");
        ArrayList<Node> sobrantes = new ArrayList<>();

        for( int i=0 ; i<6; i++ ){
            String color = VistaJugador.getColorJugador(i+1);
            if( color=="" ) {
                sobrantes.add( caja.getChildren().get(i) );
                continue;
            }
            HBox tab = (HBox) caja.getChildren().get(i);
            tab.setStyle(String.format("-fx-background-color: %s;",color));
            tab.setVisible(true);

            ( (Label) tab.getChildren().get(0) ).setText(String.valueOf(i+1));
        }
        caja.getChildren().removeAll(sobrantes);

    }

    private void inicializarAjusteEscala() {
        ChangeListener<Number> alturaListener = (observable, oldValue, newValue) -> {
            ajustarEscala(EjeCambioEscala.VERTICAL, newValue);
        };
        ChangeListener<Number> anchoListener= (observable, oldValue, newValue) -> {
            ajustarEscala(EjeCambioEscala.HORIZONTAL, newValue);
        };
        GridPane grilla = (GridPane) scene.lookup("#grilla");
        grilla.heightProperty().addListener(alturaListener);
        grilla.widthProperty().addListener(anchoListener);
        ajustarEscala(EjeCambioEscala.NINGUNO, 0);
    }

    @FXML
    public void tocarPais(MouseEvent mouseEvent) {

        Node node = (Node) mouseEvent.getSource();
        Pais pais = (Pais) node.getUserData();
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            fase = fase.tocoPais(node);
        } else {
            System.out.printf("País %s (%d fichas)%n",
                node.getId(), pais.cantidadFichas());
        }
    }

    @FXML
    public void tocoBoton(ActionEvent actionEvent) {
        fase = fase.tocoBoton((Button) actionEvent.getSource());
        verificarGanador();
    }

    @FXML
    public void mostrarNombrePais(MouseEvent mouseEvent) {
        Group nodoPais = (Group) mouseEvent.getSource();
        String nombrePais = nodoPais.getId();
        nombrePais = nombrePais.replace("_", " ");

        Label labelNombrePais = (Label) scene.lookup("#nombrePais");
        Label labelNombreContinente = (Label) scene.lookup("#nombreContinente");

        labelNombrePais.setText(nombrePais);
        labelNombreContinente.setText(((Pais) nodoPais.getUserData()).continente());
    }

    private void ajustarEscala(EjeCambioEscala cambio, Number nuevoValor) {

        Group mapa = (Group) scene.lookup("#_root");
        GridPane grilla = (GridPane) scene.lookup("#grilla");
        ColumnConstraints col1 = grilla.getColumnConstraints().get(0);
        RowConstraints fila2 = grilla.getRowConstraints().get(1);
        Bounds boundsMapa = mapa.getLayoutBounds();

        Insets padding = grilla.getPadding();
        Insets margen = GridPane.getMargin(mapa);
        double dx = padding.getRight() + padding.getRight() + margen.getRight() + margen.getLeft();
        double dy = padding.getTop() + padding.getBottom() + margen.getTop() + margen.getBottom();

        double x = cambio == EjeCambioEscala.HORIZONTAL ? nuevoValor.doubleValue() : grilla.getWidth();
        double y = cambio == EjeCambioEscala.VERTICAL ? nuevoValor.doubleValue() : grilla.getHeight();
        x = x * col1.getPercentWidth() / 100;
        y = y * fila2.getPercentHeight() / 100;

        double relacionAspectoMapa = boundsMapa.getHeight()/boundsMapa.getWidth();
        double relacionAspectoGrilla = y/x;

        double factor = 0;
        if (relacionAspectoGrilla > relacionAspectoMapa) {
            factor = (x - dx)/ boundsMapa.getWidth();
        } else {
            factor = (y - dy) / boundsMapa.getHeight();
        }

        mapa.setScaleX(factor);
        mapa.setScaleY(factor);
    }

    private void verificarGanador() {

        ArrayList<Jugador> jugadores = juego.getJugadores();
        ArrayList<Jugador> ganadores = new ArrayList<>();

        for (Jugador jug: jugadores) {
            if(jug.gane()) ganadores.add(jug);
        }

        if(ganadores.size() == 0) return;

        String textoGanador = "Ganador: ";

        if(ganadores.size() > 1) textoGanador = "Gandores: ";

        for(Jugador jug : ganadores) {
            textoGanador = textoGanador.concat("\n" + jug.numero());
        }

        scene.setRoot(new Label(textoGanador));
    }
}
