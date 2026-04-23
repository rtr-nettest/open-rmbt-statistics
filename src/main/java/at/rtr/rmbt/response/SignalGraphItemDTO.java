package at.rtr.rmbt.response;

import at.rtr.rmbt.utils.BandCalculationUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.ALWAYS)
@EqualsAndHashCode
@ToString
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

    @JsonProperty("lte_snr")
    private Integer lteSnr;

    @JsonProperty("nr_rsrp")
    private Integer nrRsrp;

    @JsonProperty("nr_rsrq")
    private Integer nrRsrq;

    @JsonProperty("nr_snr")
    private Integer nrSnr;

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

    @JsonProperty("cell_info_5G")
    private CellInfo5G cellInfo5G;


    public SignalGraphItemDTO(long timeElapsed, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, Integer lteSnr, String catTechnology,
                              Long locationId, Long areaCode, Integer primaryScramblingCode, Integer channelNumber, Integer timingAdvance) {
        this.timeElapsed = timeElapsed;
        this.networkType = networkType;
        this.signalStrength = signalStrength;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.lteSnr = lteSnr;
        this.catTechnology = catTechnology;
        this.timingAdvance = timingAdvance;

        switch (catTechnology) {
            case "2G":
                cellInfo2G = new CellInfo2G(locationId == null ? null: locationId.intValue(), (areaCode == null ? null : areaCode.intValue()), primaryScramblingCode, channelNumber);
                break;
            case "3G":
                cellInfo3G = new CellInfo3G(locationId == null ? null: locationId.intValue(), (areaCode == null ? null : areaCode.intValue()), primaryScramblingCode, channelNumber);
                break;
            case "4G":
                cellInfo4G = new CellInfo4G(locationId == null ? null: locationId.intValue(), (areaCode == null ? null : areaCode.intValue()), primaryScramblingCode, channelNumber);
                break;
            case "5G":
                cellInfo5G = new CellInfo5G(locationId, (areaCode == null ? null : areaCode.intValue()), primaryScramblingCode, channelNumber);
                //in case of 5G, signal strength is nr signal strength
                this.lteRsrp = null;
                this.lteRsrq = null;
                this.lteSnr = null;
                this.nrRsrp = lteRsrp;
                this.nrRsrq = lteRsrq;
                this.nrSnr = lteSnr;
            default:
                break;
        }
    }

    public SignalGraphItemDTO(long timeElapsed, String networkType, Integer signalStrength, Integer lteRsrp, Integer lteRsrq, String catTechnology) {
        this.timeElapsed = timeElapsed;
        this.networkType = networkType;
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
    @EqualsAndHashCode
    @ToString
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
    @EqualsAndHashCode
    @ToString
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
    @EqualsAndHashCode
    @ToString
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
            setCi(locationId);
            setTac(areaCode);
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

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class CellInfo5G extends CellInfo {
        @JsonProperty("nci")
        @Schema(description = "New Radio Cell Identity, 36 bit")
        private Long nci;

        @JsonProperty("pci")
        @Schema(description = "physical cell id, [0, 1007]")
        private Integer pci;

        @JsonProperty("tac")
        @Schema(description = "tracking area code. 24 bit")
        private Integer tac;

        @JsonProperty("nrarfcn")
        @Schema(description = "New Radio Absolute Radio Frequency Channel Number")
        private Integer nrarfcn;

        public CellInfo5G(Long locationId, Integer areaCode, Integer primaryScramblingCode, Integer channelNumber) {
            setNci(locationId);
            setTac(areaCode);
            setPci(primaryScramblingCode);
            setNrArfcn(channelNumber);
        }

        public void setNrArfcn(Integer nrarfcn) {
            if (nrarfcn != null) {
                setFi(BandCalculationUtil.getBandFromNrarfcn(nrarfcn));
            }
            this.nrarfcn = nrarfcn;
        }
    }
}
