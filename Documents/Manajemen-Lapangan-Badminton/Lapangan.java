// ============================================================================
// 4. CLASS LAPANGAN (ENCAPSULATION & OBJECT ASSOCIATION)
// ============================================================================

import java.util.ArrayList;

public class Lapangan {
    private int nomorLapangan;
    private ArrayList<Pelanggan> jadwalBooking;

    public Lapangan(int nomorLapangan) {
        this.nomorLapangan = nomorLapangan;
        this.jadwalBooking = new ArrayList<>();
    }

    public int getNomor() { return nomorLapangan; }
    
    public ArrayList<Pelanggan> getJadwal() { return jadwalBooking; }

    // Mengecek apakah jam yang direquest bentrok dengan jadwal yang sudah ada
    public boolean isTersedia(int jamRequest, int lamaRequest) {
        int selesaiRequest = jamRequest + lamaRequest;
        for (Pelanggan p : jadwalBooking) {
            if ((jamRequest >= p.getJamMulai() && jamRequest < p.getJamSelesai()) ||
                (selesaiRequest > p.getJamMulai() && selesaiRequest <= p.getJamSelesai()) ||
                (jamRequest <= p.getJamMulai() && selesaiRequest >= p.getJamSelesai())) {
                return false; // Bentrok
            }
        }
        return true;
    }

    public void tambahJadwal(Pelanggan p) {
        jadwalBooking.add(p);
    }
}