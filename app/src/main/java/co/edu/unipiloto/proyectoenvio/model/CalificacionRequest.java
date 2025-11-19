package co.edu.unipiloto.proyectoenvio.model;
public class CalificacionRequest {
    private Integer calificacion;
    private String comentario;

    public CalificacionRequest(Integer calificacion, String comentario) {
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
