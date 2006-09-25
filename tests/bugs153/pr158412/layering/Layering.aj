package layering;

//import architektur.SystemArchitektur;

public aspect Layering {
    declare warning : (layering.SystemArchitektur.inDAOLayer() ) : "Whatever";

}
