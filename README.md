KameraKu
Aplikasi KameraKu adalah implementasi sederhana dari fungsionalitas kamera di Android menggunakan pustaka CameraX (Jetpack). Aplikasi ini bertujuan untuk mendemonstrasikan integrasi live preview, pengambilan gambar, dan manajemen penyimpanan modern.

Fitur Utama Aplikasi
- Live Preview: Menampilkan feed kamera secara real-time menggunakan PreviewView yang tertanam dalam Jetpack Compose.
- Pengambilan Foto: Mampu mengambil gambar dan menyimpannya dalam format JPEG.
- Penyimpanan Aman: Foto disimpan ke galeri publik melalui API MediaStore.
- Tampilan Hasil: Menampilkan thumbnail atau status dari foto yang terakhir berhasil diambil.
- Kontrol Tambahan:
    -  Peralihan Kamera: Fitur untuk mengganti kamera dari depan ke belakang dan sebaliknya.
    -  Kontrol Flash/Torch: Mengaktifkan atau menonaktifkan lampu kilat (torch) pada kamera belakang.

Runtime Permission
- Aplikasi ini wajib meminta izin android.permission.CAMERA saat runtime.
    - Permintaan Awal: Izin diminta segera setelah aplikasi pertama kali diluncurkan menggunakan side effect Compose (LaunchedEffect).
    - Penolakan: Jika pengguna menolak izin, live preview kamera tidak akan dimuat, dan aplikasi akan menampilkan pesan peringatan yang menunjukkan bahwa akses kamera diperlukan.
    - Implementasi: Izin ditangani secara reaktif di Compose menggunakan ActivityResultContracts.RequestPermission.

Mekanisme Penyimpanan (Scoped Storage)
- Foto disimpan secara publik di folder Pictures/KameraKu.
- Kebutuhan Izin: Aplikasi tidak memerlukan izin WRITE_EXTERNAL_STORAGE.
- Manfaat: Foto yang disimpan menggunakan metode ini akan segera terindeks oleh sistem, memastikannya muncul secara otomatis di aplikasi galeri standar (misalnya Google Photos).

Penanganan Rotasi Perangkat
- Konfigurasi: Activity di AndroidManifest.xml tidak dikunci menggunakan configChanges.
- Proses Rotasi: Ketika perangkat diputar, sistem Android menghancurkan dan membuat ulang MainActivity.
- CameraX Lifecycle: CameraX, yang terikat pada Lifecycle Activity, secara otomatis mendeteksi orientasi perangkat baru dan mengatur ulang Preview dan targetRotation pada ImageCapture. Hal ini memastikan bahwa hasil foto yang disimpan memiliki metadata EXIF yang benar, sehingga foto selalu tampil tegak di galeri, terlepas dari orientasi perangkat saat pengambilan.
