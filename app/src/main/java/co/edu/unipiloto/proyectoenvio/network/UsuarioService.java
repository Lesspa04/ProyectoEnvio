package co.edu.unipiloto.proyectoenvio.network;

import co.edu.unipiloto.proyectoenvio.model.Usuario;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface UsuarioService {

    @GET("usuarios")
    Call<List<Usuario>> listar();

    @POST("usuarios")
    Call<Usuario> crear(@Body Usuario usuario);

    @GET("usuarios/{usuario}")
    Call<Usuario> obtener(@Path("usuario") String usuario);

    @GET("usuarios/login")
    Call<Usuario> login(
            @Query("usuario") String usuario,
            @Query("password") String password
    );

    @PUT("usuarios/{usuario}")
    Call<Usuario> actualizar(
            @Path("usuario") String usuarioS,
            @Body Usuario usuario
    );

    @Multipart
    @PUT("usuarios/{usuario}/foto")
    Call<Usuario> subirFoto(
            @Path("usuario") String usuario,
            @Part MultipartBody.Part file
    );

    @GET("usuarios/rol/{rol}/count")
    Call<Long> contarPorRol(@Path("rol") String rol);
}
