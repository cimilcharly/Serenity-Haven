package com.example.oldagehome.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Random;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String SENDER_EMAIL = "arunjohny2412@gmail.com";
    private static final String SENDER_PASSWORD = "kkwq uoot ausj yrvf";

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        String medicineName = intent.getStringExtra("medicineName");
        String residentId = intent.getStringExtra("residentId");
        String userEmail = intent.getStringExtra("userEmail");

        String title = intent.getStringExtra("title");
        if (title == null)
            title = "Medicine Reminder";

        String message = intent.getStringExtra("message");
        if (message == null)
            message = "Time to take your medicine: " + (medicineName != null ? medicineName : "");

        int hour = intent.getIntExtra("hour", -1);
        int minute = intent.getIntExtra("minute", -1);

        if (hour != -1 && minute != -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            Intent newIntent = new Intent(context, AlarmReceiver.class);
            newIntent.putExtra("medicineName", medicineName);
            newIntent.putExtra("residentId", residentId);
            newIntent.putExtra("hour", hour);
            newIntent.putExtra("minute", minute);
            newIntent.putExtra("title", title);
            newIntent.putExtra("message", message);
            newIntent.putExtra("userEmail", userEmail);

            int requestCode = (residentId + (medicineName != null ? medicineName : "DoctorVisit") + hour + minute)
                    .hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, newIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        pendingIntent);
            }
        }

        showNotification(context, title, message, residentId, medicineName);

        // Chain operations: Email -> Medicine Update -> Finish
        if (userEmail != null && !userEmail.isEmpty()) {
            final String finalTitle = title;
            final String finalMessage = message;
            new Thread(() -> {
                EmailSender.sendEmail(SENDER_EMAIL, SENDER_PASSWORD, userEmail, finalTitle, finalMessage);
                // Proceed to next step on main thread or just call logic
                // Since this is background thread, we can continue here but
                // `updateMedicineQuantity` uses Firestore which is async on main/background.
                // We'll call updateMedicineQuantity from here.
                if (residentId != null && medicineName != null) {
                    updateMedicineQuantity(residentId, medicineName, pendingResult);
                } else {
                    pendingResult.finish();
                }
            }).start();
        } else {
            if (residentId != null && medicineName != null) {
                updateMedicineQuantity(residentId, medicineName, pendingResult);
            } else {
                pendingResult.finish();
            }
        }
    }

    private void updateMedicineQuantity(String residentId, String medicineName, PendingResult pendingResult) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
                .getInstance();
        db.collection("users").document(residentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.example.oldagehome.models.ResidentModel resident = documentSnapshot
                                .toObject(com.example.oldagehome.models.ResidentModel.class);
                        if (resident != null && resident.getMedicines() != null) {
                            java.util.List<com.example.oldagehome.models.MedicineModel> medicines = resident
                                    .getMedicines();
                            boolean updated = false;
                            for (com.example.oldagehome.models.MedicineModel med : medicines) {
                                if (med.getMedicineName().equals(medicineName)) {
                                    if (med.getTotalQuantity() > 0) {
                                        med.setTotalQuantity(med.getTotalQuantity() - 1);
                                        updated = true;
                                    }
                                    break;
                                }
                            }

                            if (updated) {
                                db.collection("users").document(residentId)
                                        .update("medicines", medicines)
                                        .addOnCompleteListener(task -> pendingResult.finish());
                            } else {
                                pendingResult.finish();
                            }
                        } else {
                            pendingResult.finish();
                        }
                    } else {
                        pendingResult.finish();
                    }
                })
                .addOnFailureListener(e -> pendingResult.finish());
    }

    private void showNotification(Context context, String title, String message, String residentId,
            String medicineName) {
        String channelId = "medicine_notifications";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        Class<?> targetClass = (medicineName != null) ? com.example.oldagehome.activities.MedicineActivity.class
                : com.example.oldagehome.activities.ResidentDashboardActivity.class;
        Intent intent = new Intent(context, targetClass);
        intent.putExtra("residentId", residentId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }
}
