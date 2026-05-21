import java.util.ArrayList;
import java.util.Iterator;

public class Lapangan {
    private final int nomorLapangan;
    private final ArrayList<Pelanggan> jadwalBooking;

    public Lapangan(int nomorLapangan) {
        this.nomorLapangan = nomorLapangan;
        this.jadwalBooking = new ArrayList<>();
    }

    public int getNomor() {
        return nomorLapangan;
    }

    public ArrayList<Pelanggan> getJadwal() {
        return jadwalBooking;
    }

    public boolean isTersedia(int jamRequest, int lamaRequest) {
        int selesaiRequest = jamRequest + lamaRequest;
        for (Pelanggan p : jadwalBooking) {
            if ((jamRequest >= p.getJamMulai() && jamRequest < p.getJamSelesai())
                    || (selesaiRequest > p.getJamMulai() && selesaiRequest <= p.getJamSelesai())
                    || (jamRequest <= p.getJamMulai() && selesaiRequest >= p.getJamSelesai())) {
                return false;
            }
        }
        return true;
    }

    public void tambahJadwal(Pelanggan p) {
        jadwalBooking.add(p);
    }

    public boolean hapusJadwalByNama(String nama) {
        for (int i = 0; i < jadwalBooking.size(); i++) {
            if (jadwalBooking.get(i).getNama().equalsIgnoreCase(nama)) {
                jadwalBooking.remove(i);
                return true;
            }
        }
        return false;
    }

    public int hapusJadwalMemberById(String idMember) {
        int removed = 0;
        for (Iterator<Pelanggan> it = jadwalBooking.iterator(); it.hasNext();) {
            Pelanggan p = it.next();
            if (p instanceof Member) {
                Member m = (Member) p;
                if (m.getIdMember().equalsIgnoreCase(idMember)) {
                    it.remove();
                    removed++;
                }
            }
        }
        return removed;
    }
}
