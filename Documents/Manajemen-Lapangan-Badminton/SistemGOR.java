import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class SistemGOR {
    private static final int JAM_BUKA = 7;
    private static final int JAM_TUTUP = 22;
    private static final String FILE_MEMBER = "data_member.txt";

    private final Scanner scanner = new Scanner(System.in);
    private final ArrayList<Lapangan> daftarLapangan = new ArrayList<>();
    private final ArrayList<Member> databaseMember = new ArrayList<>();
    private final ArrayList<Barang> daftarBarang = new ArrayList<>();
    private final ArrayList<Transaksi> riwayatTransaksi = new ArrayList<>();
    private final Set<String> memberCheckedInHariIni = new HashSet<>();
    private final DataMemberStore memberStore = new DataMemberStore(FILE_MEMBER);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private String hariIni = "";
    private String tanggalCheckInAktif = "";

    private static class PelangganAktif {
        private final String nama;
        private final int nomorLapangan;

        private PelangganAktif(String nama, int nomorLapangan) {
            this.nama = nama;
            this.nomorLapangan = nomorLapangan;
        }

        private String label() {
            return nama + " (Lapangan " + nomorLapangan + ")";
        }
    }

    public void run() {
        inisialisasiData();
        boolean isRunning = true;

        while (isRunning) {
            clearScreen();
            tampilkanHeader();

            System.out.print("Pilih menu (1-6): ");
            int pilihan = inputInt();

            switch (pilihan) {
                case 1:
                    menuCekLapangan();
                    break;
                case 2:
                    menuTransaksiBiasa();
                    break;
                case 3:
                    menuMember();
                    break;
                case 4:
                    menuToko();
                    break;
                case 5:
                    menuLaporanKeuangan();
                    break;
                case 6:
                    System.out.println("\nTerima kasih telah menggunakan Sistem Manajemen GOR.");
                    System.out.println("Data Member telah tersimpan otomatis.");
                    System.out.println("Exiting program...\n");
                    isRunning = false;
                    break;
                default:
                    System.out.println("\nPilihan tidak valid. Silakan pilih 1-6.");
            }

            if (isRunning) {
                pause();
            }
        }

        memberStore.save(databaseMember);
    }

    private void inisialisasiData() {
        clearScreen();
        System.out.println("=====================================================");
        System.out.println("      SISTEM MANAJEMEN GOR SILMA BADMINTON");
        System.out.println("          Lokasi: Bekasi, West Java");
        System.out.println("=====================================================");
        System.out.println("--- INISIALISASI SISTEM ---");

        databaseMember.addAll(memberStore.load());
        tanggalCheckInAktif = tanggalHariIni();

        for (int i = 1; i <= 8; i++) {
            daftarLapangan.add(new Lapangan(i));
        }
        System.out.println("[INFO] 8 Lapangan Siap Digunakan.");

        daftarBarang.add(new Barang("Shuttlecock", 10000, 50));
        daftarBarang.add(new Barang("Air Mineral", 5000, 100));
        daftarBarang.add(new Barang("Sewa Sepatu", 20000, 10));
        System.out.println("[INFO] Stok Barang Toko Berhasil Dimuat.");

        System.out.println("\n-----------------------------------------------------");
        boolean hariValid = false;
        while (!hariValid) {
            System.out.print("Masukkan hari ini (Senin-Minggu): ");
            hariIni = scanner.nextLine().trim();

            // Ubah ke huruf kecil semua untuk mempermudah pengecekan
            String cekHari = hariIni.toLowerCase();

            if (cekHari.equals("senin") || cekHari.equals("selasa") ||
                    cekHari.equals("rabu") || cekHari.equals("kamis") ||
                    cekHari.equals("jumat") || cekHari.equals("sabtu") ||
                    cekHari.equals("minggu")) {
                hariValid = true; // Keluar dari loop jika input benar
            } else {
                System.out.println("[ERROR] Input tidak valid! Masukkan nama hari yang benar (Bukan angka).");
            }
        }
        System.out.println("[INFO] Sistem siap digunakan untuk hari " + hariIni + ".");
        System.out.println("-----------------------------------------------------\n");
    }

    private void tampilkanHeader() {
        System.out.println("=====================================================");
        System.out.println("      SISTEM MANAJEMEN GOR SILMA BADMINTON");
        System.out.println("          Lokasi: Bekasi, West Java");
        if (!hariIni.isEmpty()) {
            System.out.println("          Hari  : " + hariIni);
        }
        System.out.println("=====================================================");
        System.out.println("1. Cek Ketersediaan Lapangan");
        System.out.println("2. Transaksi Sewa / Booking (Pelanggan Biasa)");
        System.out.println("3. Pendaftaran / Penggunaan Member");
        System.out.println("4. Kasir Toko Perlengkapan");
        System.out.println("5. Laporan Keuangan & Riwayat");
        System.out.println("6. Keluar");
        System.out.println("=====================================================");
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void pause() {
        System.out.println("\nTekan [ENTER] untuk kembali ke menu utama...");
        scanner.nextLine();
    }

    private int inputInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Input tidak valid! Harus berupa angka: ");
            scanner.next();
        }
        int hasil = scanner.nextInt();
        scanner.nextLine();
        return hasil;
    }

    private String tanggalHariIni() {
        return dateFormat.format(new Date());
    }

    private void refreshCheckInHarian() {
        String sekarang = tanggalHariIni();
        if (!sekarang.equals(tanggalCheckInAktif)) {
            memberCheckedInHariIni.clear();
            tanggalCheckInAktif = sekarang;
        }
    }

    private String generateMemberId() {
        int max = 0;
        for (Member m : databaseMember) {
            String angka = m.getIdMember().replaceAll("\\D", "");
            if (!angka.isEmpty()) {
                try {
                    max = Math.max(max, Integer.parseInt(angka));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("MEM-%03d", max + 1);
    }

    private Member cariMemberById(String id) {
        for (Member mem : databaseMember) {
            if (mem.getIdMember().equalsIgnoreCase(id)) {
                return mem;
            }
        }
        return null;
    }

    private void menuCekLapangan() {
        System.out.println("\n[STATUS LAPANGAN SAAT INI]");
        for (Lapangan lap : daftarLapangan) {
            System.out.print("- Lapangan " + lap.getNomor() + ": ");
            if (lap.getJadwal().isEmpty()) {
                System.out.println("[TERSEDIA]");
            } else {
                for (int i = 0; i < lap.getJadwal().size(); i++) {
                    Pelanggan p = lap.getJadwal().get(i);
                    if (i > 0) {
                        System.out.print("               ");
                    }
                    System.out.printf("[%s] - %s (%02d.00 - %02d.00) | %s%n",
                            p.getStatusTransaksi(), p.getNama(),
                            p.getJamMulai(), p.getJamSelesai(), p.getJenisPelanggan());
                }
            }
        }

        System.out.println("\n-----------------------------------------------------");
        System.out.println("1. Batalkan Booking / Hapus Jadwal");
        System.out.println("2. Kembali");
        System.out.print("Pilih: ");
        int pil = inputInt();

        if (pil == 1) {
            System.out.print("Masukkan Nama yang akan dibatalkan: ");
            String namaBatal = scanner.nextLine();
            boolean found = false;

            for (Lapangan lap : daftarLapangan) {
                for (int i = 0; i < lap.getJadwal().size(); i++) {
                    Pelanggan p = lap.getJadwal().get(i);
                    if (p.getNama().equalsIgnoreCase(namaBatal)) {
                        if (p instanceof PelangganBiasa) {
                            PelangganBiasa pb = (PelangganBiasa) p;
                            if (pb.getIsBooking()) {
                                System.out.printf("\n[INFO] Booking %s di Lapangan %d dibatalkan.%n",
                                        p.getNama(), lap.getNomor());
                                System.out.printf(
                                        "[INFO] DP sebesar Rp %,.0f dapat dikembalikan atau dihanguskan sesuai kebijakan.%n",
                                        pb.hitungDP());
                            } else {
                                System.out.printf("\n[INFO] Jadwal %s (Datang Langsung) di Lapangan %d dihapus.%n",
                                        p.getNama(), lap.getNomor());
                            }
                        } else if (p instanceof Member) {
                            System.out.printf(
                                    "\n[INFO] Member %s di Lapangan %d dibatalkan. Sesi tidak dikembalikan.%n",
                                    p.getNama(), lap.getNomor());
                        }
                        lap.getJadwal().remove(i);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }

            if (!found) {
                System.out.println("[ERROR] Nama tidak ditemukan di jadwal manapun.");
            }
        }
    }

    private void menuTransaksiBiasa() {
        System.out.println("\n--- TRANSAKSI PELANGGAN BIASA ---");
        System.out.print("Nama Penanggung Jawab : ");
        String nama = scanner.nextLine().trim();

        if (nama.isEmpty()) {
            System.out.println("\nNama tidak boleh kosong!");
            return;
        }

        System.out.print("Lama Sewa (Jam)       : ");
        int lama = inputInt();
        if (lama <= 0) {
            System.out.println("\nTRANSAKSI DITOLAK! Lama sewa harus lebih dari 0 jam.");
            return;
        }

        System.out.print("Jenis (1. Booking / 2. Datang Langsung): ");
        int jenis = inputInt();
        if (jenis != 1 && jenis != 2) {
            System.out.println("\nJenis transaksi tidak valid!");
            return;
        }

        boolean isBooking = (jenis == 1);
        boolean jamValidDanTersedia = false;
        int jam = 0;
        ArrayList<Integer> listLapanganTersedia = new ArrayList<>();

        while (!jamValidDanTersedia) {
            System.out.println("\n[INFO] GOR Buka: 07.00 - 22.00 WIB");
            System.out.print("Rencana Main Jam (Format 24 Jam, cth: 19) [Ketik 0 utk Batal]: ");
            jam = inputInt();

            if (jam == 0) {
                System.out.println("\nTRANSAKSI DIBATALKAN oleh pengguna.");
                return;
            }

            if ((jam + lama) > JAM_TUTUP) {
                System.out.printf("\n[DITOLAK] Sewa %d jam dari pukul %02d.00 akan selesai pukul %02d.00.%n",
                        lama, jam, jam + lama);
                System.out.println("GOR sudah tutup pukul 22.00 WIB. Silakan atur ulang waktu main.");
                continue;
            } else if (jam < JAM_BUKA) {
                System.out.printf("\n[DITOLAK] Rencana main pukul %02d.00 WIB, GOR buka mulai pukul 07.00 WIB.%n", jam);
                continue;
            }

            listLapanganTersedia.clear();
            for (Lapangan lap : daftarLapangan) {
                if (lap.isTersedia(jam, lama)) {
                    listLapanganTersedia.add(lap.getNomor());
                }
            }

            if (listLapanganTersedia.isEmpty()) {
                System.out.printf("\n[MOHON MAAF] Semua lapangan sudah penuh pada jam %02d.00 - %02d.00 WIB.%n",
                        jam, jam + lama);
                System.out.println("Silakan masukkan jam main yang berbeda.");
            } else {
                jamValidDanTersedia = true;
            }
        }

        PelangganBiasa pBiasa = new PelangganBiasa(nama, jam, lama, isBooking);

        System.out.println("\n[PROSES POLIMORFISME & ENKAPSULASI]");
        System.out.printf("Waktu Main  : %02d.00 - %02d.00 WIB (%d Jam)%n",
                pBiasa.getJamMulai(), pBiasa.getJamSelesai(), lama);
        System.out.printf("Total Biaya : Rp %,.0f (30rb/jam)%n", pBiasa.hitungBiaya());

        if (isBooking) {
            System.out.println("Status      : BOOKING (Wajib DP 50%)");
            System.out.printf("Tagihan DP  : Rp %,.0f%n", pBiasa.hitungDP());
        } else {
            System.out.println("Status      : DATANG LANGSUNG (Lunas)");
        }

        boolean suksesDialokasikan = false;
        while (!suksesDialokasikan) {
            System.out.print("\nLapangan yang TERSEDIA: " + listLapanganTersedia);
            System.out.print("\nPilih Nomor Lapangan (Atau ketik 0 untuk Batal): ");
            int noLap = inputInt();

            if (noLap == 0) {
                System.out.println("\nTRANSAKSI DIBATALKAN oleh pengguna.");
                return;
            }

            if (listLapanganTersedia.contains(noLap)) {
                Lapangan lapDipilih = daftarLapangan.get(noLap - 1);
                lapDipilih.tambahJadwal(pBiasa);

                double bayar = isBooking ? pBiasa.hitungDP() : pBiasa.hitungBiaya();
                String ket = isBooking ? "DP Booking 50%" : "Lunas Datang Langsung";

                if (isBooking) {
                    System.out.printf("\n[INFO] Pembayaran DP Rp %,.0f diterima.%n", bayar);
                    System.out.printf("[INFO] Lapangan %d Berhasil di-booking atas nama %s.%n", noLap, nama);
                } else {
                    System.out.printf("\n[INFO] Pembayaran Lunas Rp %,.0f diterima.%n", bayar);
                    System.out.printf("[INFO] Lapangan %d Berhasil disewa atas nama %s.%n", noLap, nama);
                }

                String jadwal = String.format("%02d.00 - %02d.00 WIB", jam, jam + lama);
                Transaksi trx = new Transaksi(nama, "Pelanggan Biasa", noLap, jadwal, bayar, ket);
                riwayatTransaksi.add(trx);
                trx.tampilkanStruk();

                suksesDialokasikan = true;
            } else {
                System.out.println("\n[ERROR] Nomor lapangan tidak valid atau sedang dipakai!");
                System.out.println("Harap hanya memilih dari list lapangan yang tersedia.");
            }
        }
    }

    private void menuMember() {
        System.out.println("\n--- MENU MEMBER GOR ---");
        System.out.println("1. Daftar Member Baru (Rp 300.000/bulan)");
        System.out.println("2. Gunakan Sesi Main (Check-in)");
        System.out.println("3. Lihat Daftar Member");
        System.out.println("4. Hapus Member");
        System.out.println("5. Reset Sesi Bulanan (Admin Only)");
        System.out.print("Pilih: ");
        int pil = inputInt();

        switch (pil) {
            case 1:
                daftarMemberBaru();
                break;
            case 2:
                menuCheckInMember();
                break;
            case 3:
                tampilkanSemuaMember();
                break;
            case 4:
                hapusMember();
                break;
            case 5:
                resetSesiMember();
                break;
            default:
                System.out.println("Pilihan tidak valid.");
        }
    }

    private void daftarMemberBaru() {
        System.out.println("\n--- PENDAFTARAN MEMBER ---");
        System.out.print("Nama Member     : ");
        String nama = scanner.nextLine().trim();

        if (nama.isEmpty()) {
            System.out.println("\nNama member tidak boleh kosong!");
            return;
        }

        System.out.print("Hari Main Tetap : ");
        String hari = scanner.nextLine().trim();
        if (hari.isEmpty()) {
            System.out.println("\nHari main tetap tidak boleh kosong!");
            return;
        }

        System.out.print("Jam Main Tetap (Mulai, cth: 15)  : ");
        int jam = inputInt();

        if (jam < JAM_BUKA || (jam + 3) > JAM_TUTUP) {
            System.out.println("\nPENDAFTARAN GAGAL! Jadwal main (3 Jam) melewati jam operasional (07.00 - 22.00).");
            return;
        }

        System.out.println("\n--- PILIH LAPANGAN TETAP ---");
        System.out.printf("Cek ketersediaan lapangan untuk hari %s pukul %02d.00 - %02d.00 WIB:%n",
                hari, jam, jam + 3);

        ArrayList<Integer> lapanganTersedia = new ArrayList<>();
        for (Lapangan lap : daftarLapangan) {
            if (lap.isTersedia(jam, 3)) {
                lapanganTersedia.add(lap.getNomor());
                System.out.println("  [TERSEDIA] Lapangan " + lap.getNomor());
            } else {
                System.out.println("  [PENUH]    Lapangan " + lap.getNomor());
            }
        }

        if (lapanganTersedia.isEmpty()) {
            System.out.println("\n[GAGAL] Tidak ada lapangan yang tersedia untuk jadwal tersebut.");
            System.out.println("Silakan pilih hari atau jam yang berbeda.");
            return;
        }

        int noLap;
        while (true) {
            System.out.print("\nPilih Nomor Lapangan Tetap " + lapanganTersedia + " (0 = Batal): ");
            noLap = inputInt();
            if (noLap == 0) {
                System.out.println("\nPendaftaran dibatalkan.");
                return;
            }
            if (lapanganTersedia.contains(noLap)) {
                break;
            }
            System.out.println("[ERROR] Lapangan tidak tersedia! Pilih dari daftar yang ada.");
        }

        String idBaru = generateMemberId();
        Member memberBaru = new Member(nama, idBaru, hari, jam, noLap);
        databaseMember.add(memberBaru);
        memberStore.save(databaseMember);

        System.out.println("\n[INFO] Member Berhasil Terdaftar!");
        System.out.println("ID Member    : " + memberBaru.getIdMember());
        System.out.println("Lapangan     : Lapangan " + memberBaru.getNomorLapangan() + " (Tetap)");
        System.out.printf("Jadwal       : Setiap %s pukul %02d.00 - %02d.00 WIB%n",
                hari, jam, jam + 3);
        System.out.println("Sisa Sesi    : " + memberBaru.getSisaSesi() + " Sesi (Berlaku 1 bulan)");
        System.out.printf("Biaya Flat   : Rp %,.0f (Lunas)%n", memberBaru.hitungBiaya());
    }

    private void menuCheckInMember() {
        refreshCheckInHarian();

        System.out.println("\n--- CHECK-IN MEMBER ---");
        System.out.print("Masukkan ID Member: ");
        String searchId = scanner.nextLine().trim();

        Member m = cariMemberById(searchId);
        if (m == null) {
            System.out.println("[ERROR] ID Member tidak ditemukan.");
            return;
        }

        System.out.println("\n[INFO MEMBER]");
        System.out.println("Nama         : " + m.getNama());
        System.out.println("Hari Tetap   : " + m.getHariTetap());
        System.out.println("Lapangan     : Lapangan " + m.getNomorLapangan() + " (Tetap)");
        System.out.printf("Jadwal       : %02d.00 - %02d.00 WIB%n", m.getJamMulai(), m.getJamSelesai());
        System.out.println("Hari Ini     : " + hariIni);
        System.out.println("Sisa Sesi    : " + m.getSisaSesi() + " Sesi");

        if (!m.getHariTetap().equalsIgnoreCase(hariIni)) {
            System.out.println("\n[ACCESS DENIED] Hari ini adalah " + hariIni +
                    ". Member ini hanya boleh bermain di hari " + m.getHariTetap() + ".");
            return;
        }

        if (memberCheckedInHariIni.contains(m.getIdMember())) {
            System.out.println("\n[ACCESS DENIED] Member sudah melakukan check-in hari ini.");
            return;
        }

        if (m.getSisaSesi() <= 0) {
            System.out.println("\n[ACCESS DENIED] Sesi Anda sudah habis bulan ini. Silakan perpanjang membership.");
            return;
        }

        int noLap = m.getNomorLapangan();
        Lapangan lapMember = daftarLapangan.get(noLap - 1);
        if (!lapMember.isTersedia(m.getJamMulai(), m.getLamaMain())) {
            System.out.printf("\n[WARNING] Lapangan %d sedang tidak tersedia pada jam %02d.00 - %02d.00 WIB.%n",
                    noLap, m.getJamMulai(), m.getJamSelesai());
            System.out.println("Hubungi admin untuk penanganan lebih lanjut.");
            return;
        }

        System.out.println("\n[PROSES CHECK-IN]");
        System.out.printf("Lapangan %d dialokasikan secara otomatis sesuai jadwal tetap.%n", noLap);

        m.gunakanSesi();
        memberStore.save(databaseMember);

        lapMember.tambahJadwal(m);
        memberCheckedInHariIni.add(m.getIdMember());

        System.out.printf("[INFO] Check-in berhasil! Lapangan %d siap digunakan.%n", noLap);
        System.out.println("[INFO] Sisa sesi main Anda bulan ini: " + m.getSisaSesi() + " Sesi.");

        String jadwal = String.format("%02d.00 - %02d.00 WIB", m.getJamMulai(), m.getJamSelesai());
        Transaksi trx = new Transaksi(
                m.getNama(),
                "Member (" + m.getIdMember() + ")",
                noLap,
                jadwal,
                0,
                "Sisa Sesi: " + m.getSisaSesi());
        riwayatTransaksi.add(trx);
        trx.tampilkanStruk();
    }

    private void tampilkanSemuaMember() {
        System.out.println("\n--- DAFTAR SEMUA MEMBER ---");
        if (databaseMember.isEmpty()) {
            System.out.println("Belum ada member terdaftar.");
            return;
        }

        System.out.println("+----------+-----------------+----------------+----------+-----------+-----------+");
        System.out.println("| ID       | Nama            | Hari Tetap     | Jam      | Lapangan  | Sisa Sesi |");
        System.out.println("+----------+-----------------+----------------+----------+-----------+-----------+");
        for (Member m : databaseMember) {
            System.out.printf("| %-8s | %-15s | %-14s | %02d.00    | %-9s | %-9d |%n",
                    m.getIdMember(), m.getNama(), m.getHariTetap(),
                    m.getJamMulai(), "Lap " + m.getNomorLapangan(), m.getSisaSesi());
        }
        System.out.println("+----------+-----------------+----------------+----------+-----------+-----------+");
    }

    private void hapusMember() {
        System.out.println("\n--- HAPUS MEMBER ---");
        System.out.print("Masukkan ID Member yang akan dihapus: ");
        String id = scanner.nextLine().trim();

        Member target = cariMemberById(id);
        if (target != null) {
            System.out.println("Nama     : " + target.getNama());
            System.out.println("Hari     : " + target.getHariTetap());
            System.out.println("Lapangan : Lapangan " + target.getNomorLapangan());
            System.out.print("Yakin hapus? (1. Ya / 2. Batal): ");
            int konfirm = inputInt();
            if (konfirm == 1) {
                databaseMember.remove(target);
                memberCheckedInHariIni.remove(target.getIdMember());
                for (Lapangan lap : daftarLapangan) {
                    lap.hapusJadwalMemberById(target.getIdMember());
                }
                memberStore.save(databaseMember);
                System.out.println("[INFO] Member " + target.getIdMember() + " berhasil dihapus.");
            } else {
                System.out.println("[INFO] Penghapusan dibatalkan.");
            }
        } else {
            System.out.println("[ERROR] ID Member tidak ditemukan.");
        }
    }

    private void resetSesiMember() {
        System.out.println("\n--- RESET SESI BULANAN MEMBER ---");
        System.out.println("Perhatian: Fitur ini akan mengembalikan sisa sesi semua member menjadi 4.");
        System.out.print("Lanjutkan? (1. Ya / 2. Batal): ");
        int konfirm = inputInt();
        if (konfirm == 1) {
            for (Member m : databaseMember) {
                m.resetSesi();
            }
            memberStore.save(databaseMember);
            System.out.println("[INFO] Sesi semua member berhasil direset menjadi 4 sesi/bulan.");
        } else {
            System.out.println("[INFO] Reset sesi dibatalkan.");
        }
    }

    private void menuToko() {
        System.out.println("\n--- KASIR TOKO BADMINTON ---");
        System.out.println("Siapa yang melakukan pembelian barang?");
        System.out.println("[Daftar Pelanggan Aktif Saat Ini]");

        ArrayList<PelangganAktif> aktifList = new ArrayList<>();
        for (Lapangan lap : daftarLapangan) {
            for (Pelanggan p : lap.getJadwal()) {
                aktifList.add(new PelangganAktif(p.getNama(), lap.getNomor()));
            }
        }

        for (int i = 0; i < aktifList.size(); i++) {
            System.out.println((i + 1) + ". " + aktifList.get(i).label());
        }
        int indexUmum = aktifList.size() + 1;
        System.out.println(indexUmum + ". Pembeli Umum (Bukan Penyewa)");

        System.out.print("Pilih (1-" + indexUmum + "): ");
        int pilPembeli = inputInt();

        if (pilPembeli < 1 || pilPembeli > indexUmum) {
            System.out.println("\nPilihan pembeli tidak valid!");
            return;
        }

        String namaPembeli = (pilPembeli == indexUmum) ? "Pembeli Umum" : aktifList.get(pilPembeli - 1).nama;

        System.out.println("\nPembeli Terpilih: " + namaPembeli);
        System.out.println("\nDaftar Barang Tersedia:");
        for (int i = 0; i < daftarBarang.size(); i++) {
            Barang b = daftarBarang.get(i);
            System.out.printf("%d. %s (Rp %,.0f/pcs) - Stok: %d%n",
                    (i + 1), b.getNama(), b.getHarga(), b.getStok());
        }

        System.out.print("\nPilih Barang (1-" + daftarBarang.size() + "): ");
        int pilBarang = inputInt();
        if (pilBarang < 1 || pilBarang > daftarBarang.size()) {
            System.out.println("\nBarang tidak ditemukan!");
            return;
        }

        System.out.print("Jumlah Beli : ");
        int qty = inputInt();
        if (qty <= 0) {
            System.out.println("\nJumlah beli harus lebih dari 0!");
            return;
        }

        Barang brg = daftarBarang.get(pilBarang - 1);
        if (brg.getStok() >= qty) {
            double total = brg.getHarga() * qty;
            System.out.println("\n[PROSES TRANSAKSI TOKO]");
            System.out.printf("Total Belanja : Rp %,.0f (%d x Rp %,.0f)%n", total, qty, brg.getHarga());
            brg.kurangiStok(qty);
            System.out.printf("[INFO] Stok %s tersisa: %d.%n", brg.getNama(), brg.getStok());

            Transaksi trx = new Transaksi(namaPembeli, "Toko", 0,
                    brg.getNama() + " x" + qty, total, "Pembelian Barang");
            riwayatTransaksi.add(trx);
        } else {
            System.out.printf("\nMaaf, stok tidak mencukupi (Sisa stok: %d).%n", brg.getStok());
        }
    }

    private void menuLaporanKeuangan() {
        System.out.println("\n--- LAPORAN KEUANGAN & RIWAYAT ---");
        System.out.println("1. Riwayat Transaksi Hari Ini");
        System.out.println("2. Rekap Pendapatan");
        System.out.print("Pilih: ");
        int pil = inputInt();

        if (pil == 1) {
            System.out.println("\n--- RIWAYAT TRANSAKSI ---");
            if (riwayatTransaksi.isEmpty()) {
                System.out.println("Belum ada transaksi hari ini.");
                return;
            }
            System.out.println("+----------+-----------------+----------------+--------+");
            System.out.println("| ID       | Nama            | Jenis          | Total  |");
            System.out.println("+----------+-----------------+----------------+--------+");
            for (Transaksi t : riwayatTransaksi) {
                System.out.printf("| %-8s | %-15s | %-14s | %6s |%n",
                        t.getIdTransaksi(), t.getNamaPelanggan(), t.getJenis(),
                        t.getTotalBayar() == 0 ? "FREE" : String.format("Rp %,.0f", t.getTotalBayar()));
            }
            System.out.println("+----------+-----------------+----------------+--------+");
        } else if (pil == 2) {
            double totalSewa = 0, totalToko = 0;
            int countSewa = 0, countToko = 0, countMember = 0;

            for (Transaksi t : riwayatTransaksi) {
                if (t.getJenis().equals("Toko")) {
                    totalToko += t.getTotalBayar();
                    countToko++;
                } else if (t.getJenis().contains("Member")) {
                    countMember++;
                } else {
                    totalSewa += t.getTotalBayar();
                    countSewa++;
                }
            }

            System.out.println("\n--- REKAP PENDAPATAN ---");
            System.out.println("+-----------------------------------------------+");
            String fmtSewa = String.format("%,.0f", totalSewa);
            String fmtToko = String.format("%,.0f", totalToko);
            String fmtTotal = String.format("%,.0f", totalSewa + totalToko);
            System.out.printf("| %-30s : Rp %10s |%n", "Pendapatan Sewa Lapangan", fmtSewa);
            System.out.printf("| %-30s : Rp %10s |%n", "Pendapatan Toko", fmtToko);
            System.out.println("+-----------------------------------------------+");
            System.out.printf("| %-30s : Rp %10s |%n", "TOTAL PENDAPATAN", fmtTotal);
            System.out.println("+-----------------------------------------------+");
            System.out.printf("| Transaksi Sewa : %-3d | Toko: %-3d | Member: %-3d |%n",
                    countSewa, countToko, countMember);
            System.out.println("+-----------------------------------------------+");
        } else {
            System.out.println("Pilihan tidak valid.");
        }
    }
}
