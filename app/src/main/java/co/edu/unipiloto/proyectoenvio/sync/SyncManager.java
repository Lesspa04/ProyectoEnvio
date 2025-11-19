package co.edu.unipiloto.proyectoenvio.sync;

import android.content.Context;
import android.util.Log;

import java.util.List;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;
import co.edu.unipiloto.proyectoenvio.model.Usuario;
import co.edu.unipiloto.proyectoenvio.model.Encomienda;
import co.edu.unipiloto.proyectoenvio.network.RetrofitClient;
import co.edu.unipiloto.proyectoenvio.network.UsuarioService;
import co.edu.unipiloto.proyectoenvio.network.EncomiendaService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncManager {

    private final DatabaseHelper db;
    private final UsuarioService usuarioService;
    private final EncomiendaService encomiendaService;

    public SyncManager(Context context) {
        this.db = new DatabaseHelper(context);
        this.usuarioService = RetrofitClient.getClient().create(UsuarioService.class);
        this.encomiendaService = RetrofitClient.getClient().create(EncomiendaService.class);
    }

    // --------------------------------------------------------------------
    // 1️⃣ Sincronizar usuarios desde backend a SQLite
    // --------------------------------------------------------------------
    public void syncUsuariosFromBackend() {
        usuarioService.listar().enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Usuario u : response.body()) {
                        db.insertOrUpdateUsuarioFromBackend(u);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Log.e("Sync", "Error sincronizando usuarios", t);
            }
        });
    }

    // --------------------------------------------------------------------
    // 2️⃣ Sincronizar encomiendas desde backend a SQLite
    // --------------------------------------------------------------------
    public void syncEncomiendasFromBackend() {
        encomiendaService.listar().enqueue(new Callback<List<Encomienda>>() {
            @Override
            public void onResponse(Call<List<Encomienda>> call, Response<List<Encomienda>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Encomienda e : response.body()) {
                        db.insertOrUpdateEncomiendaFromBackend(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Encomienda>> call, Throwable t) {
                Log.e("Sync", "Error sincronizando encomiendas", t);
            }
        });
    }

    // --------------------------------------------------------------------
    // 3️⃣ Método general para sincronizar TODO al iniciar la app
    // --------------------------------------------------------------------
    public void fullSync() {
        syncUsuariosFromBackend();
        syncEncomiendasFromBackend();
    }
}

