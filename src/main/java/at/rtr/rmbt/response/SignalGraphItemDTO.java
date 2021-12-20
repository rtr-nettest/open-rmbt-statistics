package at.rtr.rmbt.response;

import at.rtr.rmbt.utils.BandCalculationUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class SignalGraphItemDTO {

    @JsonProperty("time_elapsed")
    private long timeElapsed;

    @JsonProperty("network_type")
    private String networkType;

    @JsonProperty("signal_strength")
    private Integer signalStrength;

    @JsonProperty("lte_rsrp")
    private Integer lteRsrp;

    @JsonProperty("lte_rsrq")
    private Integer lteRsrq;

    @JsonProperty("cat_technology")
    private String catTechnology;

    @JsonProperty("timing_advance")
    private Integer timingAdvance;

    @JsonProperty("cell_info_2G")
    private CellInfo2G cellInfo2G;

    @JsonProperty("cell_info_3G")
    private CellInfo3G cellInfo3G;

    @JsonProperty("cell_info_4G")
    private CellInfo4G cellInfo4G;


    public SignalGraphItemDTO(long timeElapsed, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, String catTechnology,
                              Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber, Integer timingAdvance) {
        this.timeElapsed = timeElapsed;
        this.networkType = networkType;
        locationId = (locationId == 0) ? null : locationId;
        areaCode = (areaCode == 0) ? null : areaCode;
        channelNumber = (channelNumber == 0) ? null : channelNumber;
        primaryScramblingCode = (primaryScramblingCode == 0) ? null : primaryScramblingCode;

        this.signalStrength = signalStrength;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.catTechnology = catTechnology;
        this.timingAdvance = timingAdvance;

        switch (catTechnology) {
            case "2G":
                cellInfo2G = new CellInfo2G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            case "3G":
                cellInfo3G = new CellInfo3G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            case "4G":
                cellInfo4G = new CellInfo4G(locationId, areaCode, primaryScramblingCode, channelNumber);
                break;
            default:
                break;
        }
    }

    public SignalGraphItemDTO(long timeElapsed, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, String catTechnology) {
        this.timeElapsed = timeElapsed;
        this.networkType = networkType;
        signalStrength = (signalStrength == 0) ? null : signalStrength;
        lteRsrp = (lteRsrp == 0) ? null : lteRsrp;
        lteRsrq = (lteRsrq == 0) ? null : lteRsrq;
        this.signalStrength = signalStrength;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.catTechnology = catTechnology;
    }

    public SignalGraphItemDTO() {

    }


    @Getter
    @Setter
    public abstract static class CellInfo {

        @JsonIgnore
        private BandCalculationUtil.FrequencyInformation fi;

        @JsonProperty("frequency_dl")
        public Double getFrequencyDl() {
            return (getFi() == null) ? null : getFi().getFrequencyDL();
        }

        @JsonProperty("band")
        public Integer getBand() {
            return (getFi() == null) ? null : getFi().getBand();
        }
    }

    @Setter
    @Getter
    public static class CellInfo2G extends CellInfo {
        @JsonProperty("lac")
        private Integer lac;

        @JsonProperty("cid")
        private Integer cid;

        @JsonProperty("bsic")
        private Integer bsic;

        @JsonProperty("arfcn")
        private Integer arfcn;

        public CellInfo2G(Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
            setLac(locationId);
            setCid(areaCode);
            setBsic(primaryScramblingCode);
            setArfcn(channelNumber);
        }

        public void setArfcn(Integer arfcn) {
            if (arfcn != null) {
                setFi(BandCalculationUtil.getBandFromArfcn(arfcn));
            }
            this.arfcn = arfcn;
        }
    }

    @Setter
    @Getter
    public static class CellInfo3G extends CellInfo {
        @JsonProperty("lac")
        private Integer lac;

        @JsonProperty("cid")
        private Integer cid;

        @JsonProperty("psc")
        private Integer psc;

        @JsonProperty("uarfcn")
        private Integer uarfcn;

        public CellInfo3G(Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
            setLac(locationId);
            setCid(areaCode);
            setPsc(primaryScramblingCode);
            setUarfcn(channelNumber);
        }

        public void setUarfcn(Integer uarfcn) {
            if (uarfcn != null) {
                setFi(BandCalculationUtil.getBandFromUarfcn(uarfcn));
            }
            this.uarfcn = uarfcn;
        }
    }

    @Getter
    @Setter
    public static class CellInfo4G extends CellInfo {
        @JsonProperty("tac")
        private Integer tac;

        @JsonProperty("ci")
        private Integer ci;

        @JsonProperty("pci")
        private Integer pci;

        @JsonProperty("earfcn")
        private Integer earfcn;

        public CellInfo4G(Integer locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
            setTac(locationId);
            setCi(areaCode);
            setPci(primaryScramblingCode);
            setEarfcn(channelNumber);
        }

        public void setEarfcn(Integer earfcn) {
            if (earfcn != null) {
                setFi(BandCalculationUtil.getBandFromEarfcn(earfcn));
            }
            this.earfcn = earfcn;
        }
    }
}
