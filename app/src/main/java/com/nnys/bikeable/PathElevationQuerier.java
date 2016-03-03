package com.nnys.bikeable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;


public class PathElevationQuerier {

    ArrayList<com.google.maps.model.LatLng> path;
    EncodedPolyline route;

    public PathElevationQuerier(EncodedPolyline route) {
        this.route = route;
    }


    public static int calcNumOfSamplesForXmetersIntervals(long pathDistance, int x, int max_elevation_samples) {
        int result;
        long numberOfSamples = (long)(pathDistance / x);
        if ( numberOfSamples >= Integer.MAX_VALUE ) {
            return Integer.MAX_VALUE;
        }
        result = (int)numberOfSamples;
        if ( result > max_elevation_samples) {
            return max_elevation_samples;
        }
        if (result < 2) {
            return 2;
        }

        return result;
    }

    public static int getDistanceBetweenSamples(long pathDistance){
        int resultDistance;
        int numOfSamples = calcNumOfSamplesForXmetersIntervals(pathDistance, BikeableRoute.GRAPH_X_INTERVAL, BikeableRoute.MAX_GRAPH_SAMPLES);
        // TODO: handle zero division
        resultDistance = (int)pathDistance/numOfSamples;
        return resultDistance;
    }

    public ElevationResult[] getElevationSamples(int numOfSamples) {

        ElevationResult[] elevations = null;
        GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBCRcbSVolZ34CkIlUfwtcAld4uYXitR50");

        try {
            elevations = ElevationApi.getByPath(context, numOfSamples, route).await();
        } catch (Exception e) {
            System.out.println("Failed getting elevation info");
            e.printStackTrace();
        }
        return elevations;
    }


    public static ArrayList<com.google.maps.model.LatLng> createMockPathNoGms() {
        com.google.maps.model.LatLng SIDNY = new com.google.maps.model.LatLng(-33.867487, 151.206990);
        com.google.maps.model.LatLng MELBORNE = new com.google.maps.model.LatLng(-37.814107, 144.963280);
        ArrayList<com.google.maps.model.LatLng> path = new ArrayList<com.google.maps.model.LatLng>();
        path.add(SIDNY);
        path.add(MELBORNE);
        return path;
    }

    public static ArrayList<LatLng> createMockPathWithGms() {
        LatLng SIDNY = new LatLng(-33.867487, 151.206990);
        LatLng MELBORNE = new LatLng(-37.814107, 144.963280);
        ArrayList<LatLng> path = new ArrayList<LatLng>();
        path.add(SIDNY);
        path.add(MELBORNE);
        return path;
    }

    public static EncodedPolyline createMockRoute() {
        EncodedPolyline SYD_MELB_ROUTE = new EncodedPolyline(
                "rvumEis{y[`NsfA~tAbF`bEj^h{@{KlfA~eA~`AbmEghAt~D|e@jlRpO~yH_\\v}LjbBh~FdvCxu@`nCplDbcBf_B|w"
                        + "BhIfhCnqEb~D~jCn_EngApdEtoBbfClf@t_CzcCpoEr_Gz_DxmAphDjjBxqCviEf}B|pEvsEzbE~qGfpExjBlqCx}"
                        + "BvmLb`FbrQdpEvkAbjDllD|uDldDj`Ef|AzcEx_Gtm@vuI~xArwD`dArlFnhEzmHjtC~eDluAfkC|eAdhGpJh}N_m"
                        + "ArrDlr@h|HzjDbsAvy@~~EdTxpJje@jlEltBboDjJdvKyZpzExrAxpHfg@pmJg[tgJuqBnlIarAh}DbN`hCeOf_Ib"
                        + "xA~uFt|A|xEt_ArmBcN|sB|h@b_DjOzbJ{RlxCcfAp~AahAbqG~Gr}AerA`dCwlCbaFo]twKt{@bsG|}A~fDlvBvz"
                        + "@tw@rpD_r@rqB{PvbHek@vsHlh@ptNtm@fkD[~xFeEbyKnjDdyDbbBtuA|~Br|Gx_AfxCt}CjnHv`Ew\\lnBdrBfq"
                        + "BraD|{BldBxpG|]jqC`mArcBv]rdAxgBzdEb{InaBzyC}AzaEaIvrCzcAzsCtfD~qGoPfeEh]h`BxiB`e@`kBxfAv"
                        + "^pyA`}BhkCdoCtrC~bCxhCbgEplKrk@tiAteBwAxbCwuAnnCc]b{FjrDdjGhhGzfCrlDruBzSrnGhvDhcFzw@n{@z"
                        + "xAf}Fd{IzaDnbDjoAjqJjfDlbIlzAraBxrB}K~`GpuD~`BjmDhkBp{@r_AxCrnAjrCx`AzrBj{B|r@~qBbdAjtDnv"
                        + "CtNzpHxeApyC|GlfM`fHtMvqLjuEtlDvoFbnCt|@xmAvqBkGreFm~@hlHw|AltC}NtkGvhBfaJ|~@riAxuC~gErwC"
                        + "ttCzjAdmGuF`iFv`AxsJftD|nDr_QtbMz_DheAf~Buy@rlC`i@d_CljC`gBr|H|nAf_Fh{G|mE~kAhgKviEpaQnu@"
                        + "zwAlrA`G~gFnvItz@j{Cng@j{D{]`tEftCdcIsPz{DddE~}PlnE|dJnzG`eG`mF|aJdqDvoAwWjzHv`H`wOtjGzeX"
                        + "hhBlxErfCf{BtsCjpEjtD|}Aja@xnAbdDt|ErMrdFh{CzgAnlCnr@`wEM~mE`bA`uD|MlwKxmBvuFlhB|sN`_@fvB"
                        + "p`CxhCt_@loDsS|eDlmChgFlqCbjCxk@vbGxmCjbMba@rpBaoClcCk_DhgEzYdzBl\\vsA_JfGztAbShkGtEhlDzh"
                        + "C~w@hnB{e@yF}`D`_Ayx@~vGqn@l}CafC");
        return SYD_MELB_ROUTE;
    }
}
