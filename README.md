# KameraKu

Aplikasi Android sederhana untuk mengambil foto, menyimpan ke galeri, dan menampilkan thumbnail hasil foto terakhir. 

## Fitur Utama

1.  **Live Preview**: Menampilkan pratinjau kamera secara real-time.
2.  **Ambil Foto**: Menyimpan foto ke penyimpanan eksternal (MediaStore) dengan format JPEG.
3.  **Galeri Thumbnail**: Menampilkan thumbnail dari foto yang baru saja diambil.
4.  **Switch Kamera**: Mendukung peralihan antara kamera depan dan belakang.
5.  **Flash/Torch**: Kontrol lampu kilat untuk kamera belakang.

## Alur Izin 
Menggunakan izin `android.permission.CAMERA`.
- Saat aplikasi pertama kali dibuka, sistem akan meminta izin pengguna untuk mengakses kamera.
- Jika izin ditolak, aplikasi akan menampilkan pesan bahwa izin diperlukan dan tidak akan memuat pratinjau kamera.
- Izin ditangani menggunakan API `ActivityResultContracts.RequestPermission` di Jetpack Compose.

## Penyimpanan (MediaStore)

Aplikasi menyimpan foto secara publik di folder `Pictures/KameraKu` menggunakan **MediaStore API**, yang merupakan cara standar dan aman untuk menyimpan media di Android 10+ (API 29+).
- Tidak memerlukan izin `WRITE_EXTERNAL_STORAGE` pada Android 10 ke atas.
- Foto yang diambil akan otomatis muncul di aplikasi Galeri/Google Photos pengguna.

## Penanganan Rotasi

Aplikasi menangani rotasi layar dengan cara **memuat ulang Activity**.
- `AndroidManifest.xml` dikonfigurasi standar (tanpa `configChanges="orientation"` yang memblokir rotasi).
- Saat HP diputar, Activity dihancurkan dan dibuat ulang. CameraX secara otomatis mendeteksi orientasi layar baru dan menyesuaikan pratinjau serta rotasi hasil foto agar tetap tegak (tidak miring).


