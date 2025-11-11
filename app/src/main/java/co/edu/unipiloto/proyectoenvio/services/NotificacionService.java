package co.edu.unipiloto.proyectoenvio.services;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import co.edu.unipiloto.proyectoenvio.MisRecoleccionesActivity;
import co.edu.unipiloto.proyectoenvio.R;

public class NotificacionService extends IntentService {

    private static final String CHANNEL_ID = "ENCOMIENDAS_CHANNEL";
    private static final int NOTIFICATION_ID = 1001;

    public static final String EXTRA_MENSAJE = "mensaje";

    public NotificacionService() {
        super("NotificacionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String mensaje = intent.getStringExtra(EXTRA_MENSAJE);
            mostrarNotificacion(mensaje);
        }
    }

    private void mostrarNotificacion(String mensaje) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de Encomiendas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Avisos del estado de tus envíos");
            notificationManager.createNotificationChannel(channel);
        }

        // Intent para abrir la app al tocar la notificación
        Intent intent = new Intent(this, MisRecoleccionesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Actualización de Encomienda")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
