package pl.ib.beauty.model.dao;

import lombok.Getter;

@Getter
public enum Category {
    BARBER("barber"),
    HAIRDRESSER("hairdresser"),
    MAKEUP_ARTIST("makeup_artist"),
    NAIL_TECHNICIAN("nail_technician"),
    ESTHETICIAN("esthetician"),
    MASSAGE_THERAPIST("massage_therapist"),
    SKIN_CARE_SPECIALIST("skin care specialist"),
    LASH_TECHNICIAN("lash_technician"),
    BROW_ARTIST("brow_artist"),
    SPA_THERAPIST("spa_therapist"),
    COSMETOLOGIST("cosmetologist"),
    PERMANENT_MAKEUP_ARTIST("permanent_makeup_artist"),
    HAIR_COLORIST("hair_colorist"),
    WAX_SPECIALIST("wax_specialist");

    private final String label;

    Category(String label) {
        this.label = label;
    }

}
