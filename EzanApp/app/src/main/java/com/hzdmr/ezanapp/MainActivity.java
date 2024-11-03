package com.hzdmr.ezanapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.media.MediaPlayer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Sayaç haneleri
    public TextView Sayac1, Sayac2, Sayac3, Sayac4, Sayac5, Sayac6, Sayac7, Sayac8;

    // Üst kısım TextView'ları
    public TextView Tarih, gun, Vakit, Saat, kerat, sehir, pil;

    // Namaz vakitleri
    public TextView imsakvakit, gunesvakit, oglevakit, ikindivakit, aksamvakit, yatsivakit;

    public CountDownTimer countDownTimer;

    public MediaPlayer o;
    public MediaPlayer z;

    @SuppressLint("SimpleDateFormat")
    public SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    public SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        o = MediaPlayer.create(this, R.raw.o);
        z = MediaPlayer.create(this, R.raw.z);

        ImageButton imageButton = findViewById(R.id.settings);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ayarlar aktivitesini başlat
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });;


        // Status bar ve navigation bar gizlemek için
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            // Daha eski versiyonlar için
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Sayaç hanelerini tanımla
        Sayac1 = findViewById(R.id.Sayac1);
        Sayac2 = findViewById(R.id.Sayac2);
        Sayac3 = findViewById(R.id.Sayac3);
        Sayac4 = findViewById(R.id.Sayac4);
        Sayac5 = findViewById(R.id.Sayac5);
        Sayac6 = findViewById(R.id.Sayac6);
        Sayac7 = findViewById(R.id.Sayac7);
        Sayac8 = findViewById(R.id.Sayac8);

        // Üst kısım TextView'ları
        Tarih = findViewById(R.id.Tarih);
        gun = findViewById(R.id.gun);
        Vakit = findViewById(R.id.Vakit);
        Saat = findViewById(R.id.Saat);
        kerat = findViewById(R.id.kerat);
        pil = findViewById(R.id.pil);


        // Namaz vakitleri
        imsakvakit = findViewById(R.id.imsakvakit);
        gunesvakit = findViewById(R.id.gunesvakit);
        oglevakit = findViewById(R.id.oglevakit);
        ikindivakit = findViewById(R.id.ikindivakit);
        aksamvakit = findViewById(R.id.aksamvakit);
        yatsivakit = findViewById(R.id.yatsivakit);

        // Tarih, gün, saat ve pil yüzdesini güncelle
        updateDateTimeBattery();

        // Namaz vakitlerini çek ve sayacı başlat
        fetchNamazVakitleri();
    }
    private void playoSound() {
        if (o != null && !o.isPlaying()) {
            o.start();
            o.setOnCompletionListener(mp -> o.reset());
        }
    }

    private void playzSound() {
        if (z != null && !z.isPlaying()) {
            z.start();
            z.setOnCompletionListener(mp -> z.reset());
        }
    }
    public void seso() {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();

        try {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            Date gunesTime = fullDateFormat.parse(todayDate + " " + gunesvakit.getText());
            Date ogleTime = fullDateFormat.parse(todayDate + " " + oglevakit.getText());
            Date ikindiTime = fullDateFormat.parse(todayDate + " " + ikindivakit.getText());
            Date aksamTime = fullDateFormat.parse(todayDate + " " + aksamvakit.getText());
            Date yatsiTime = fullDateFormat.parse(todayDate + " " + yatsivakit.getText());

            // Güneş vakti için 43 dk - 42 dk 45 sn arası
            Date sabahBasla = new Date(gunesTime.getTime() - (43 * 60 * 1000));
            Date sabahBitir = new Date(gunesTime.getTime() - (42 * 60 * 1000) - (45 * 1000));

            // Diğer vakitler için 10 dk - 9 dk 45 sn arası
            Date ogleBasla = new Date(ogleTime.getTime() - (10 * 60 * 1000));
            Date ogleBitir = new Date(ogleTime.getTime() - (9 * 60 * 1000) - (45 * 1000));

            Date ikindiBasla = new Date(ikindiTime.getTime() - (10 * 60 * 1000));
            Date ikindiBitir = new Date(ikindiTime.getTime() - (9 * 60 * 1000) - (45 * 1000));

            Date aksamBasla = new Date(aksamTime.getTime() - (10 * 60 * 1000));
            Date aksamBitir = new Date(aksamTime.getTime() - (9 * 60 * 1000) - (45 * 1000));

            Date yatsiBasla = new Date(yatsiTime.getTime() - (10 * 60 * 1000));
            Date yatsiBitir = new Date(yatsiTime.getTime() - (9 * 60 * 1000) - (45 * 1000));

            if ((currentTime.after(sabahBasla) && currentTime.before(sabahBitir)) ||
                    (currentTime.after(ogleBasla) && currentTime.before(ogleBitir)) ||
                    (currentTime.after(ikindiBasla) && currentTime.before(ikindiBitir)) ||
                    (currentTime.after(aksamBasla) && currentTime.before(aksamBitir)) ||
                    (currentTime.after(yatsiBasla) && currentTime.before(yatsiBitir))) {
                playoSound();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void sesz() {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();

        try {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            Date gunesTime = fullDateFormat.parse(todayDate + " " + gunesvakit.getText());
            Date ogleTime = fullDateFormat.parse(todayDate + " " + oglevakit.getText());
            Date ikindiTime = fullDateFormat.parse(todayDate + " " + ikindivakit.getText());
            Date aksamTime = fullDateFormat.parse(todayDate + " " + aksamvakit.getText());
            Date yatsiTime = fullDateFormat.parse(todayDate + " " + yatsivakit.getText());

            // Güneş vakti için 43 dk - 42 dk 45 sn arası
            Date sabahBasla = new Date(gunesTime.getTime() - (33 * 60 * 1000));
            Date sabahBitir = new Date(gunesTime.getTime() - (32 * 60 * 1000) - (45 * 1000));

            // Diğer vakitler için 10 dk - 9 dk 45 sn arası
            Date ogleBasla = new Date(ogleTime.getTime() - (15 * 1000));
            Date ogleBitir = new Date(ogleTime.getTime());

            Date ikindiBasla = new Date(ikindiTime.getTime() - (15 * 1000));
            Date ikindiBitir = new Date(ikindiTime.getTime());

            Date aksamBasla = new Date(aksamTime.getTime() - (15 * 1000));
            Date aksamBitir = new Date(aksamTime.getTime());

            Date yatsiBasla = new Date(yatsiTime.getTime() - (15 * 1000));
            Date yatsiBitir = new Date(yatsiTime.getTime());

            if ((currentTime.after(sabahBasla) && currentTime.before(sabahBitir)) ||
                    (currentTime.after(ogleBasla) && currentTime.before(ogleBitir)) ||
                    (currentTime.after(ikindiBasla) && currentTime.before(ikindiBitir)) ||
                    (currentTime.after(aksamBasla) && currentTime.before(aksamBitir)) ||
                    (currentTime.after(yatsiBasla) && currentTime.before(yatsiBitir))) {
                playzSound();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void updateDateTimeBattery() {
        // Tarihi güncelle
        Calendar calendar = Calendar.getInstance();
        String date = new SimpleDateFormat("dd MMMM", new Locale("tr")).format(calendar.getTime());
        Tarih.setText(date);

        // Günü güncelle
        String dayOfWeek = new SimpleDateFormat("EEEE", new Locale("tr")).format(calendar.getTime());
        gun.setText(dayOfWeek);

        // Saati güncelle
        String time = new SimpleDateFormat("HH:mm").format(calendar.getTime());

        // Saat ve pil yüzdesini ayarla
        Saat.setText(time);
    }
    public void pil() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level = 0;
        int scale = 100;
        if (batteryStatus != null) {
            level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
        int batteryPct = (int) (level * 100 / (float) scale);
        if (batteryPct <= 100 && batteryPct >= 61) {
            pil.setTextColor(Color.parseColor("#003200"));
        } else if (batteryPct <= 60 && batteryPct >= 36) {
            pil.setTextColor(Color.parseColor("#FF9B00"));
        } else if (batteryPct <= 35 && batteryPct >= 1) {
            pil.setTextColor(Color.parseColor("#FF0000"));
        }
        pil.setText("%" + batteryPct);
    }

    public void keratvakti() {
        runOnUiThread(() -> {
            Sayac1.setTextColor(Color.parseColor("#FF5900"));
            Sayac2.setTextColor(Color.parseColor("#FF5900"));
            Sayac3.setTextColor(Color.parseColor("#FF5900"));
            Sayac4.setTextColor(Color.parseColor("#FF5900"));
            Sayac5.setTextColor(Color.parseColor("#FF5900"));
            Sayac6.setTextColor(Color.parseColor("#FF5900"));
            Sayac7.setTextColor(Color.parseColor("#FF5900"));
            Sayac8.setTextColor(Color.parseColor("#FF5900"));
            kerat.setTextColor(Color.parseColor("#FF5900"));
            kerat.setText("Kerahat");
        });
    }

    public void nonkeratvakti() {
        runOnUiThread(() -> {
            Sayac1.setTextColor(Color.parseColor("#00FF00"));
            Sayac2.setTextColor(Color.parseColor("#00FF00"));
            Sayac3.setTextColor(Color.parseColor("#00FF00"));
            Sayac4.setTextColor(Color.parseColor("#00FF00"));
            Sayac5.setTextColor(Color.parseColor("#00FF00"));
            Sayac6.setTextColor(Color.parseColor("#00FF00"));
            Sayac7.setTextColor(Color.parseColor("#00FF00"));
            Sayac8.setTextColor(Color.parseColor("#00FF00"));
            kerat.setText("");
        });
    }

    public void kala() {
        Sayac1.setTextColor(Color.parseColor("#FF0000"));
        Sayac2.setTextColor(Color.parseColor("#FF0000"));
        Sayac3.setTextColor(Color.parseColor("#FF0000"));
        Sayac4.setTextColor(Color.parseColor("#FF0000"));
        Sayac5.setTextColor(Color.parseColor("#FF0000"));
        Sayac6.setTextColor(Color.parseColor("#FF0000"));
        Sayac7.setTextColor(Color.parseColor("#FF0000"));
        Sayac8.setTextColor(Color.parseColor("#FF0000"));
    }
    public void gunesvakti() {
        Sayac1.setTextColor(Color.parseColor("#353535"));
        Sayac2.setTextColor(Color.parseColor("#353535"));
        Sayac3.setTextColor(Color.parseColor("#353535"));
        Sayac4.setTextColor(Color.parseColor("#353535"));
        Sayac5.setTextColor(Color.parseColor("#353535"));
        Sayac6.setTextColor(Color.parseColor("#353535"));
        Sayac7.setTextColor(Color.parseColor("#353535"));
        Sayac8.setTextColor(Color.parseColor("#353535"));
    }

    public void fetchNamazVakitleri() {
        new Thread(() -> {
            try {
                // Jsoup ile namaz vakitlerini çek
                Document doc = Jsoup.connect("https://namazvakitleri.diyanet.gov.tr/tr-TR").get();

                // Namaz vakitlerini al
                Elements imsakElem = doc.select("#today-pray-times-row > div:nth-child(1) > div > div.tpt-time");
                Elements gunesElem = doc.select("#today-pray-times-row > div:nth-child(2) > div > div.tpt-time");
                Elements ogleElem = doc.select("#today-pray-times-row > div:nth-child(3) > div > div.tpt-time");
                Elements ikindiElem = doc.select("#today-pray-times-row > div:nth-child(4) > div > div.tpt-time");
                Elements aksamElem = doc.select("#today-pray-times-row > div:nth-child(5) > div > div.tpt-time");
                Elements yatsiElem = doc.select("#today-pray-times-row > div:nth-child(6) > div > div.tpt-time");

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Veri Çekildi!", Toast.LENGTH_LONG).show());

                // String olarak al
                String imsak = imsakElem.text();
                String gunes = gunesElem.text();
                String ogle = ogleElem.text();
                String ikindi = ikindiElem.text();
                String aksam = aksamElem.text();
                String yatsi = yatsiElem.text();

                // UI elemanlarını güncelle
                runOnUiThread(() -> {
                    imsakvakit.setText(imsak);
                    gunesvakit.setText(gunes);
                    oglevakit.setText(ogle);
                    ikindivakit.setText(ikindi);
                    aksamvakit.setText(aksam);
                    yatsivakit.setText(yatsi);
                });

                // Sıradaki namaz vaktini belirle
                Calendar calendar = Calendar.getInstance();
                String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                Date currentTime = calendar.getTime();

                Date imsakTime = fullDateFormat.parse(todayDate + " " + imsak);
                Date gunesTime = fullDateFormat.parse(todayDate + " " + gunes);
                Date ogleTime = fullDateFormat.parse(todayDate + " " + ogle);
                Date ikindiTime = fullDateFormat.parse(todayDate + " " + ikindi);
                Date aksamTime = fullDateFormat.parse(todayDate + " " + aksam);
                Date yatsiTime = fullDateFormat.parse(todayDate + " " + yatsi);

                Date nextPrayerTime;
                String nextPrayerName;

                Date kerat1 = new Date(gunesTime.getTime() + (45 * 60 * 1000));
                Date kerat2 = new Date(ogleTime.getTime() - (45 * 60 * 1000));
                Date kerat3 = new Date(aksamTime.getTime() - (45 * 60 * 1000));



                if (currentTime.before(imsakTime)) {
                    nextPrayerTime = imsakTime;
                    nextPrayerName = "İmsak";
                    nonkeratvakti();
                    imsakvakit.setTextColor(Color.GREEN);
                    gunesvakit.setTextColor(Color.parseColor("#353535"));
                    oglevakit.setTextColor(Color.parseColor("#353535"));
                    ikindivakit.setTextColor(Color.parseColor("#353535"));
                    aksamvakit.setTextColor(Color.parseColor("#353535"));
                    yatsivakit.setTextColor(Color.parseColor("#353535"));
                } else if (currentTime.before(gunesTime)) {
                    nextPrayerTime = gunesTime;
                    nextPrayerName = "Güneş";
                    nonkeratvakti();
                    imsakvakit.setTextColor(Color.parseColor("#353535"));
                    gunesvakit.setTextColor(Color.GREEN);
                    oglevakit.setTextColor(Color.parseColor("#353535"));
                    ikindivakit.setTextColor(Color.parseColor("#353535"));
                    aksamvakit.setTextColor(Color.parseColor("#353535"));
                    yatsivakit.setTextColor(Color.parseColor("#353535"));
                } else if (currentTime.before(ogleTime)) {
                    nextPrayerTime = ogleTime;
                    nextPrayerName = "Öğle";
                    nonkeratvakti();
                    imsakvakit.setTextColor(Color.parseColor("#353535"));
                    gunesvakit.setTextColor(Color.parseColor("#353535"));
                    oglevakit.setTextColor(Color.GREEN);
                    ikindivakit.setTextColor(Color.parseColor("#353535"));
                    aksamvakit.setTextColor(Color.parseColor("#353535"));
                    yatsivakit.setTextColor(Color.parseColor("#353535"));
                    if (currentTime.after(kerat1) && currentTime.after(kerat2)) {
                        runOnUiThread(this::keratvakti);
                    } else if (currentTime.before(kerat1) && currentTime.after(kerat2)) {
                        runOnUiThread(this::nonkeratvakti);
                    } else if (currentTime.before(kerat1) && currentTime.before(kerat2)) {
                        runOnUiThread(this::keratvakti);
                    }
                } else if (currentTime.before(ikindiTime)) {
                    nextPrayerTime = ikindiTime;
                    nextPrayerName = "İkindi";
                    nonkeratvakti();
                    imsakvakit.setTextColor(Color.parseColor("#353535"));
                    gunesvakit.setTextColor(Color.parseColor("#353535"));
                    oglevakit.setTextColor(Color.parseColor("#353535"));
                    ikindivakit.setTextColor(Color.GREEN);
                    aksamvakit.setTextColor(Color.parseColor("#353535"));
                    yatsivakit.setTextColor(Color.parseColor("#353535"));
                } else if (currentTime.before(aksamTime)) {
                    nextPrayerTime = aksamTime;
                    nextPrayerName = "Akşam";
                    imsakvakit.setTextColor(Color.parseColor("#353535"));
                    gunesvakit.setTextColor(Color.parseColor("#353535"));
                    oglevakit.setTextColor(Color.parseColor("#353535"));
                    ikindivakit.setTextColor(Color.parseColor("#353535"));
                    aksamvakit.setTextColor(Color.GREEN);
                    yatsivakit.setTextColor(Color.parseColor("#353535"));
                    if (currentTime.after(kerat3)) {
                        keratvakti();
                    } else if (currentTime.before(kerat3)) {
                        nonkeratvakti();
                    }
                } else if (currentTime.before(yatsiTime)) {
                    nextPrayerTime = yatsiTime;
                    nextPrayerName = "Yatsı";
                    imsakvakit.setTextColor(Color.parseColor("#353535"));
                    gunesvakit.setTextColor(Color.parseColor("#353535"));
                    oglevakit.setTextColor(Color.parseColor("#353535"));
                    ikindivakit.setTextColor(Color.parseColor("#353535"));
                    aksamvakit.setTextColor(Color.parseColor("#353535"));
                    yatsivakit.setTextColor(Color.GREEN);
                } else {
                    // Yarınki İmsak vaktini al
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    String tomorrowDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                    nextPrayerTime = fullDateFormat.parse(tomorrowDate + " " + imsak);
                    nextPrayerName = "İmsak";
                    imsakvakit.setTextColor(Color.GREEN);
                    gunesvakit.setTextColor(Color.parseColor("#353535"));
                    oglevakit.setTextColor(Color.parseColor("#353535"));
                    ikindivakit.setTextColor(Color.parseColor("#353535"));
                    aksamvakit.setTextColor(Color.parseColor("#353535"));
                    yatsivakit.setTextColor(Color.parseColor("#353535"));
                }

                long timeUntilNextPrayer = nextPrayerTime.getTime() - currentTime.getTime();

                // UI elemanlarını güncelle
                runOnUiThread(() -> {
                    Vakit.setText(nextPrayerName);
                    startCountdown(timeUntilNextPrayer);
                });

            } catch (IOException | ParseException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Veri çekme hatası!", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
    public boolean keratcheker = false;
    private void checkKerahatTimes() {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();

        try {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            Date gunesTime = fullDateFormat.parse(todayDate + " " + gunesvakit.getText());
            Date ogleTime = fullDateFormat.parse(todayDate + " " + oglevakit.getText());
            Date aksamTime = fullDateFormat.parse(todayDate + " " + aksamvakit.getText());

            Date kerat1 = new Date(gunesTime.getTime() + (45 * 60 * 1000));
            Date kerat2 = new Date(ogleTime.getTime() - (45 * 60 * 1000));
            Date kerat3 = new Date(aksamTime.getTime() - (45 * 60 * 1000));

            if ((currentTime.after(gunesTime) && currentTime.before(kerat1)) ||
                    (currentTime.after(kerat2) && currentTime.before(ogleTime)) ||
                    (currentTime.after(kerat3) && currentTime.before(aksamTime))) {
                keratvakti();
                keratcheker = true;
            } else  {
                nonkeratvakti();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void checkPrayerTimes() {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();

        try {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            Date gunesTime = fullDateFormat.parse(todayDate + " " + gunesvakit.getText());
            Date ogleTime = fullDateFormat.parse(todayDate + " " + oglevakit.getText());
            Date ikindiTime = fullDateFormat.parse(todayDate + " " + ikindivakit.getText());
            Date aksamTime = fullDateFormat.parse(todayDate + " " + aksamvakit.getText());
            Date yatsiTime = fullDateFormat.parse(todayDate + " " + yatsivakit.getText());

            Date sabahkala = new Date(gunesTime.getTime() - (40 * 60 * 1000));
            Date sabahkalabitir  = new Date(gunesTime.getTime() - (30 * 60 * 1000));
            Date oglekala = new Date(ogleTime.getTime() - (10 * 60 * 1000));
            Date ikindikala = new Date(ikindiTime.getTime() - (10 * 60 * 1000));
            Date aksamkala = new Date(aksamTime.getTime() - (10 * 60 * 1000));
            Date yatsikala = new Date(yatsiTime.getTime() - (10 * 60 * 1000));

            if ((currentTime.after(sabahkala) && currentTime.before(sabahkalabitir) ||
                    currentTime.after(oglekala) && currentTime.before(ogleTime) ||
                    (currentTime.after(ikindikala) && currentTime.before(ikindiTime)) ||
                    (currentTime.after(aksamkala) && currentTime.before(aksamTime)) ||
                    (currentTime.after(yatsikala) && currentTime.before(yatsiTime)))) {
                kala();
            } else if (currentTime.after(gunesTime) && currentTime.before(sabahkalabitir)) {
                gunesvakti();
            } else if (!keratcheker) {
                nonkeratvakti();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void startCountdown(long timeUntilNextPrayer) {
        // Önceki sayaç varsa durdur
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeUntilNextPrayer, 1000) {
            public void onTick(long millisUntilFinished) {
                // Tarih ve saati güncelle
                updateDateTimeBattery();
                pil();
                seso();
                sesz();


                // Kerahat vakitlerini kontrol et
                checkKerahatTimes();
                checkPrayerTimes();

                long totalSeconds = millisUntilFinished / 1000;
                int hours = (int) (totalSeconds / 3600);
                int minutes = (int) ((totalSeconds % 3600) / 60);
                int seconds = (int) (totalSeconds % 60);

                // Haneleri ayır
                int h1 = hours / 10;
                int h2 = hours % 10;
                int m1 = minutes / 10;
                int m2 = minutes % 10;
                int s1 = seconds / 10;
                int s2 = seconds % 10;

                // UI elemanlarını güncelle
                runOnUiThread(() -> {
                    Sayac1.setText(String.valueOf(h1));
                    Sayac2.setText(String.valueOf(h2));
                    Sayac3.setText(":");
                    Sayac4.setText(String.valueOf(m1));
                    Sayac5.setText(String.valueOf(m2));
                    Sayac6.setText(":");
                    Sayac7.setText(String.valueOf(s1));
                    Sayac8.setText(String.valueOf(s2));
                });
            }

            public void onFinish() {
                fetchNamazVakitleri(); // Sayaç bittiğinde namaz vakitlerini tekrar çek
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Sayaç varsa durdur
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
