package co.edu.unipiloto.proyectoenvio.network;

import co.edu.unipiloto.proyectoenvio.model.CalificacionRequest;
import co.edu.unipiloto.proyectoenvio.model.Encomienda;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface EncomiendaService {

    @GET("encomiendas")
    Call<List<Encomienda>> listar();

    @POST("encomiendas")
    Call<Encomienda> crear(@Body Encomienda encomienda);

    @GET("encomiendas/{guia}")
    Call<Encomienda> obtenerPorGuia(@Path("guia") String guia);

    @GET("encomiendas/remitente/{usuario}")
    Call<List<Encomienda>> porRemitente(@Path("usuario") String usuario);

    @GET("encomiendas/estado/{estado}")
    Call<List<Encomienda>> porEstado(@Path("estado") String estado);

    @GET("encomiendas/recolector/{id}")
    Call<List<Encomienda>> porRecolector(@Path("id") String id);

    @GET("encomiendas/sin-recolector")
    Call<List<Map<String, String>>> sinRecolector();

    @PUT("encomiendas/{guia}/asignar/{id}")
    Call<Encomienda> asignar(
            @Path("guia") String guia,
            @Path("id") String id
    );

    @PUT("encomiendas/{guia}/estado/{nuevo}")
    Call<Encomienda> actualizarEstado(
            @Path("guia") String guia,
            @Path("nuevo") String nuevoEstado
    );

    @PUT("encomiendas/{guia}/calificacion")
    Call<Encomienda> guardarCalificacion(
            @Path("guia") String guia,
            @Body CalificacionRequest req
    );

    @GET("encomiendas/recolector/{id}/solicitadas")
    Call<List<Encomienda>> solicitadasDeRecolector(@Path("id") String id);

    @GET("encomiendas/recolector/{id}/entregar")
    Call<List<Encomienda>> entregarDeRecolector(@Path("id") String id);

    @GET("encomiendas/estadisticas/promedio")
    Call<Map<String, Double>> promedioGlobal();

    @GET("encomiendas/estadisticas/usuario/{usuario}")
    Call<Map<String, Double>> promedioPorUsuario(@Path("usuario") String usuario);

    @GET("encomiendas/estadisticas/recolector/{id}")
    Call<Map<String, Double>> promedioPorRecolector(@Path("id") String id);

    @GET("encomiendas/estadisticas/distribucion")
    Call<Map<Integer, Long>> distribucionGlobal();

    @GET("encomiendas/estadisticas/distribucion/{id}")
    Call<Map<Integer, Long>> distribucionPorRecolector(@Path("id") String id);
}
